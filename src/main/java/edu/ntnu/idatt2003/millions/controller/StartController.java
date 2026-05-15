package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.file.game.GameSaveCorruptException;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.CsvStockFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.InvalidStockDataException;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.StartScreenInputs;
import edu.ntnu.idatt2003.millions.view.StartView;
import edu.ntnu.idatt2003.millions.view.titlebar.TitleBarFactory;
import java.io.File;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the {@link StartView}.
 *
 * <p>Handles user interactions on the start screen, including
 * file selection for stock data and saved games, as well as
 * starting or loading a game session.</p>
 *
 * <p>UI-input validation is delegated to {@link StartInputValidator}. File
 * validation on selection is delegated to {@link edu.ntnu.idatt2003.millions.service.GameService},
 * which performs a trial parse without mutating game state.</p>
 *
 * <p>Errors are surfaced via {@code errorSink} and successes via {@code successSink},
 * both injected as {@link java.util.function.Supplier} consumers for i18n support.</p>
 */
public class StartController {

    private final Stage stage;
    private final StartView view;
    private final StartScreenInputs inputs;
    private final GameService gameService;
    private final Runnable showMainViewAction;
    private final Consumer<Supplier<String>> errorSink;
    private final Consumer<Supplier<String>> successSink;

    /**
     * Constructs a new StartController with a default {@link GameService}.
     *
     * <p>This constructor is used by the application startup flow. It delegates
     * to {@link #StartController(Stage, GameService)} so alternate startup paths
     * can inject their own game manager.</p>
     *
     * @param stage the primary application stage
     * @throws NullPointerException if stage is null
     */
    public StartController(Stage stage) {
        this(stage, new GameService());
    }

    /**
     * Constructs a new StartController with the given {@link GameService}.
     *
     * @param stage       the primary application stage
     * @param gameService the game manager used to create or load game state
     * @throws NullPointerException if stage or game manager is null
     */
    public StartController(Stage stage, GameService gameService) {
        this(stage, gameService, new StartView(TitleBarFactory.createForStartScreen(stage).getNode()));
    }

    /**
     * Intermediate constructor that resolves the {@link StartView} before delegating
     * to the full DI constructor.
     *
     * @param stage       the primary application stage
     * @param gameService the game manager used to create or load game state
     * @param startView   the already-constructed start view
     */
    private StartController(Stage stage, GameService gameService, StartView startView) {
        this(stage, gameService,
                startView,
                () -> new MainController(stage, gameService).show(),
                startView::showError,
                startView::showSuccess);
    }

    /**
     * Full dependency injection constructor used by production wiring and by
     * controller tests that need access to the concrete {@link StartView}.
     * Event bindings are deferred to {@link #show()}.
     *
     * @param stage              the primary application stage
     * @param gameService        the game manager used to create or load game state
     * @param view               the start view that exposes user input and controls
     * @param showMainViewAction the action used to navigate to the main view
     * @param errorSink          the consumer that displays user-facing error messages
     * @param successSink        the consumer that displays user-facing success messages
     * @throws NullPointerException if any argument is null
     */
    StartController(Stage stage,
                    GameService gameService,
                    StartView view,
                    Runnable showMainViewAction,
                    Consumer<Supplier<String>> errorSink,
                    Consumer<Supplier<String>> successSink) {
        this.stage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "GameService cannot be null");
        this.view = Objects.requireNonNull(view, "StartView cannot be null");
        this.inputs = view;
        this.showMainViewAction = Objects.requireNonNull(showMainViewAction, "Show main view action cannot be null");
        this.errorSink = Objects.requireNonNull(errorSink, "Error sink cannot be null");
        this.successSink = Objects.requireNonNull(successSink, "Success sink cannot be null");
    }

    /**
     * Test-only seam that constructs the controller without requiring a
     * {@link Stage} or a fully built {@link StartView}.
     *
     * @param gameService        the game manager used to create or load game state
     * @param inputs             the input seam the start flow reads from
     * @param showMainViewAction the action used to navigate to the main view
     * @param errorSink          the consumer that receives user-facing error messages
     * @param successSink        the consumer that receives user-facing success messages
     * @throws NullPointerException if any argument is null
     */
    StartController(GameService gameService,
                    StartScreenInputs inputs,
                    Runnable showMainViewAction,
                    Consumer<Supplier<String>> errorSink,
                    Consumer<Supplier<String>> successSink) {
        this.stage = null;
        this.view = null;
        this.gameService = Objects.requireNonNull(gameService, "GameService cannot be null");
        this.inputs = Objects.requireNonNull(inputs, "Inputs cannot be null");
        this.showMainViewAction = Objects.requireNonNull(showMainViewAction, "Show main view action cannot be null");
        this.errorSink = Objects.requireNonNull(errorSink, "Error sink cannot be null");
        this.successSink = Objects.requireNonNull(successSink, "Success sink cannot be null");
    }

    /**
     * Injects callbacks into the view for all interactive controls.
     * The view wires these callbacks to its own controls internally,
     * so the controller never accesses individual UI components directly.
     */
    private void bindEvents() {
        view.setOnStartGame(this::handleStartGame);
        view.setOnLoadGame(this::handleLoadGame);
        view.setOnBrowseStockFile(this::handleBrowseStockFile);
        view.setOnBrowseSaveFile(this::handleBrowseSaveFile);
        view.setOnStockFileDrop(this::validateAndSetStockFile);
        view.setOnSaveFileDrop(this::validateAndSetSaveFile);
    }

    /**
     * Opens a file chooser for selecting a stock data file (CSV).
     * If a file is chosen, it is validated immediately via
     * {@link #validateAndSetStockFile(File)}. The file path is only
     * stored if the file parses without errors.
     */
    private void handleBrowseStockFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(LanguageManager.get("start.chooser.stock.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Data files", "*.csv"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            validateAndSetStockFile(file);
        }
    }

    /**
     * Opens a file chooser for selecting a previously saved game file (JSON).
     * If a file is chosen, it is validated immediately via
     * {@link #validateAndSetSaveFile(File)}. The file path is only
     * stored if the file parses without errors.
     */
    private void handleBrowseSaveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(LanguageManager.get("start.chooser.save.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Save files", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            validateAndSetSaveFile(file);
        }
    }

    /**
     * Validates the given stock file by attempting to parse it immediately.
     * If parsing succeeds, the file path is stored in the view. If parsing
     * fails, the file path is cleared and the error is shown to the user
     * via the {@link #errorSink} so they cannot proceed with an invalid file.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to invoke the handler directly without simulating a drag-and-drop event.</p>
     *
     * @param file the CSV file to validate and register
     */
    void validateAndSetStockFile(File file) {
        inputs.setStockFilePath(file.getAbsolutePath());
        Currency currency = Optional.ofNullable(inputs.getSelectedCurrency())
                .orElse(Currency.getInstance("USD"));
        try {
            new CsvStockFileHandler().readStocks(file.toPath(), currency);
            successSink.accept(() -> LanguageManager.get("start.file.uploadSuccess"));
        } catch (InvalidStockDataException | UncheckedIOException e) {
            inputs.setStockFilePath("");
            errorSink.accept(e::getMessage);
        }
    }

    /**
     * Validates the given save file by attempting to parse it immediately.
     * If parsing succeeds, the file path is stored in the view. If parsing
     * fails, the file path is cleared and the error is shown to the user
     * via the {@link #errorSink} so they cannot proceed with a corrupt save file.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to invoke the handler directly without simulating a drag-and-drop event.</p>
     *
     * @param file the JSON save file to validate and register
     */
    void validateAndSetSaveFile(File file) {
        inputs.setSaveFilePath(file.getAbsolutePath());
        try {
            new JsonGameFileHandler().loadGame(file);
            successSink.accept(() -> LanguageManager.get("start.file.uploadSuccess"));
        } catch (GameSaveCorruptException | UncheckedIOException e) {
            inputs.setSaveFilePath("");
            errorSink.accept(e::getMessage);
        }
    }

    /**
     * Validates input, starts a new game session through {@link GameService},
     * and shows the main view. A custom stock file is optional; if none is
     * selected, the default stock data is used.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to call the handler without simulating a JavaFX button click.</p>
     */
    void handleStartGame() {
        String name = inputs.getName();
        String capital = inputs.getCapital();
        boolean hasFile = !inputs.getStockFilePath().isBlank();
        Currency currency = inputs.getSelectedCurrency();

        // Collect format errors for non-blank fields (without early return) so both
        // name and capital problems can surface together when both are invalid.
        List<String> validationErrorKeys = new ArrayList<>();
        StartInputValidator.validateNameFormat(name).ifPresent(validationErrorKeys::add);
        StartInputValidator.validateCapitalFormat(capital).ifPresent(validationErrorKeys::add);

        // Collect missing fields separately so they can be combined into a single
        // "missing X and Y" sentence instead of being listed line by line.
        List<String> missingKeys = new ArrayList<>();
        if (name.isBlank()) missingKeys.add("field.name");
        if (capital.isBlank()) missingKeys.add("field.capital");
        if (hasFile && currency == null) missingKeys.add("field.currency");

        if (!validationErrorKeys.isEmpty() || !missingKeys.isEmpty()) {
            errorSink.accept(() -> {
                List<String> lines = new ArrayList<>();
                validationErrorKeys.forEach(key -> lines.add(LanguageManager.get(key)));
                if (!missingKeys.isEmpty()) {
                    lines.add(buildMissingMessage(
                            missingKeys.stream().map(LanguageManager::get).toList()));
                }
                return String.join("\n", lines);
            });
            return;
        }

        // All inputs valid — delegate to service
        runOrShowError(() -> {
            String validatedName     = StartInputValidator.requireName(name);
            BigDecimal parsedCapital = StartInputValidator.parseCapital(capital);
            if (hasFile) {
                Currency validatedCurrency = StartInputValidator.requireCurrency(currency);
                File stockFile = StartInputValidator.requireCsvFilePath(inputs.getStockFilePath());
                gameService.createNewGame(validatedName, parsedCapital, stockFile, validatedCurrency);
            } else {
                gameService.createNewGame(validatedName, parsedCapital);
            }
            showMainView();
        });
    }

    /**
     * Builds a combined missing-fields message from a list of field names.
     * Single field: "Navn mangler". Multiple: "Navn og Startkapital mangler".
     *
     * @param fields the localised field names that are missing
     * @return a formatted error string
     */
    private String buildMissingMessage(List<String> fields) {
        String suffix = " " + LanguageManager.get("error.missing.suffix");
        List<String> lower = fields.stream().map(String::toLowerCase).toList();
        String message = lower.size() == 1
                ? lower.get(0) + suffix
                : String.join(", ", lower.subList(0, lower.size() - 1))
                  + " " + LanguageManager.get("error.and") + " "
                  + lower.get(lower.size() - 1) + suffix;
        return Character.toUpperCase(message.charAt(0)) + message.substring(1);
    }

    /**
     * Validates input, loads an existing saved game through {@link GameService},
     * and shows the main view.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to call the handler without simulating a JavaFX button click.</p>
     */
    void handleLoadGame() {
        String saveGameFilePath = inputs.getSaveFilePath();
        if (saveGameFilePath.isBlank()) {
            errorSink.accept(() -> LanguageManager.get("error.save.file.missing"));
            return;
        }
        runOrShowError(() -> {
            gameService.loadGame(new File(saveGameFilePath));
            showMainView();
        });
    }

    /**
     * Functional interface for start-flow actions that may throw checked
     * file-parsing exceptions.
     */
    @FunctionalInterface
    private interface GameAction {
        void execute() throws InvalidStockDataException, GameSaveCorruptException;
    }

    /**
     * Runs the given action and translates expected game errors to a user-facing
     * error dialog. Catches the specific exception types that the start flow can
     * legitimately produce: input validation failures, missing game state, and
     * file I/O errors. Programming errors such as {@link NullPointerException}
     * are intentionally not caught so they surface during development.
     *
     * @param action the start-flow action to execute
     */
    private void runOrShowError(GameAction action) {
        try {
            action.execute();
        } catch (GameSaveCorruptException | InvalidStockDataException | IllegalArgumentException
                 | IllegalStateException | UncheckedIOException exception) {
            errorSink.accept(exception::getMessage);
        }
    }

    /**
     * Shows the main view using the active {@link GameService}.
     */
    private void showMainView() {
        showMainViewAction.run();
    }

    /**
     * Displays the start screen on the primary stage and binds UI events.
     */
    public void show() {
        bindEvents();
        stage.setTitle("Millions");
        stage.setScene(view.getScene());
        stage.show();
        stage.setMaximized(true);
        Platform.runLater(stage::centerOnScreen);
    }
}
