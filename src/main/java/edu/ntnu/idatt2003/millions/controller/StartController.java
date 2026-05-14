package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.file.game.GameSaveCorruptException;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.CsvStockFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.InvalidStockDataException;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.StartScreenInputs;
import edu.ntnu.idatt2003.millions.view.StartView;
import edu.ntnu.idatt2003.millions.view.titlebar.TitleBarFactory;
import java.io.File;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the {@link StartView}.
 *
 * <p>Handles user interactions on the start screen, including
 * file selection for stock data and saved games, as well as
 * starting or loading a game session.</p>
 *
 * <p>UI-input validation (name, capital, file extension) is delegated to
 * {@link StartInputValidator}. Both stock CSV files and save JSON files are
 * validated immediately on selection: the controller attempts a full parse via
 * {@link CsvStockFileHandler} or {@link JsonGameFileHandler} and clears the file
 * path if the file is invalid, preventing the user from proceeding with a bad file.</p>
 *
 * <p>Errors from the start flow are translated to user-facing messages by
 * a shared error-handling helper that catches the concrete exception types
 * the flow can legitimately produce (input validation, missing game state,
 * and file I/O), while letting programming errors surface as crashes.</p>
 */
public class StartController {

    private final Stage stage;
    private final StartView view;
    private final StartScreenInputs inputs;
    private final GameService gameService;
    private final Runnable showMainViewAction;
    private final Consumer<String> errorSink;

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
     * <p>Injecting the manager keeps the controller flexible while preserving
     * the normal production flow through {@link #StartController(Stage)}.
     * Event bindings are deferred to {@link #show()}.</p>
     *
     * @param stage       the primary application stage
     * @param gameService the game manager used to create or load game state
     * @throws NullPointerException if stage or game manager is null
     */
    public StartController(Stage stage, GameService gameService) {
        this(stage, gameService,
                new StartView(TitleBarFactory.createForStartScreen(stage).getNode()),
                () -> new MainController(stage, gameService).show(),
                StartController::showAlert);
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
     * @throws NullPointerException if any argument is null
     */
    StartController(Stage stage,
                    GameService gameService,
                    StartView view,
                    Runnable showMainViewAction,
                    Consumer<String> errorSink) {
        this.stage = Objects.requireNonNull(stage, "Stage cannot be null");
        this.gameService = Objects.requireNonNull(gameService, "GameService cannot be null");
        this.view = Objects.requireNonNull(view, "StartView cannot be null");
        this.inputs = view;
        this.showMainViewAction = Objects.requireNonNull(showMainViewAction, "Show main view action cannot be null");
        this.errorSink = Objects.requireNonNull(errorSink, "Error sink cannot be null");
    }

    /**
     * Test-only seam that constructs the controller without requiring a
     * {@link Stage} or a fully built {@link StartView}. The controller
     * reads user input, reports errors to {@code errorSink} and navigates
     * through {@code showMainViewAction}.
     * {@link #show()} and the file-chooser handlers are not safe to call
     * on instances created through this constructor.
     *
     * @param gameService        the game manager used to create or load game state
     * @param inputs             the input seam the start flow reads from
     * @param showMainViewAction the action used to navigate to the main view
     * @param errorSink          the consumer that receives user-facing error messages
     * @throws NullPointerException if any argument is null
     */
    StartController(GameService gameService,
                    StartScreenInputs inputs,
                    Runnable showMainViewAction,
                    Consumer<String> errorSink) {
        this.stage = null;
        this.view = null;
        this.gameService = Objects.requireNonNull(gameService, "GameService cannot be null");
        this.inputs = Objects.requireNonNull(inputs, "Inputs cannot be null");
        this.showMainViewAction = Objects.requireNonNull(showMainViewAction, "Show main view action cannot be null");
        this.errorSink = Objects.requireNonNull(errorSink, "Error sink cannot be null");
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
        chooser.setTitle("Select stock file");
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
        chooser.setTitle("Select save file");
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
     * <p>The currency currently selected in the view is used for parsing.
     * If no currency is selected yet, USD is used as a fallback so that
     * structural errors (wrong field count, blank fields, invalid price) are
     * still caught regardless of the currency choice.</p>
     *
     * @param file the CSV file to validate and register
     */
    private void validateAndSetStockFile(File file) {
        inputs.setStockFilePath(file.getAbsolutePath());
        Currency currency = Optional.ofNullable(inputs.getSelectedCurrency())
                .orElse(Currency.getInstance("USD"));
        try {
            new CsvStockFileHandler().readStocks(file.toPath(), currency);
        } catch (InvalidStockDataException | UncheckedIOException e) {
            inputs.setStockFilePath("");
            errorSink.accept(e.getMessage());
        }
    }

    /**
     * Validates the given save file by attempting to parse it immediately.
     * If parsing succeeds, the file path is stored in the view. If parsing
     * fails, the file path is cleared and the error is shown to the user
     * via the {@link #errorSink} so they cannot proceed with a corrupt save file.
     *
     * @param file the JSON save file to validate and register
     */
    private void validateAndSetSaveFile(File file) {
        inputs.setSaveFilePath(file.getAbsolutePath());
        try {
            new JsonGameFileHandler().loadGame(file);
        } catch (GameSaveCorruptException | UncheckedIOException e) {
            inputs.setSaveFilePath("");
            errorSink.accept(e.getMessage());
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
        runOrShowError(() -> {
            String name = StartInputValidator.requireName(inputs.getName());
            BigDecimal parsedCapital = StartInputValidator.parseCapital(inputs.getCapital());
            String stockFilePath = inputs.getStockFilePath();

            if (stockFilePath.isBlank()) {
                gameService.createNewGame(name, parsedCapital);
            } else {
                File stockFile = StartInputValidator.requireCsvFilePath(stockFilePath);
                Currency currency = StartInputValidator.requireCurrency(inputs.getSelectedCurrency());
                gameService.createNewGame(name, parsedCapital, stockFile, currency);
            }
            showMainView();
        });
    }

    /**
     * Validates input, loads an existing saved game through {@link GameService},
     * and shows the main view.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to call the handler without simulating a JavaFX button click.</p>
     */
    void handleLoadGame() {
        runOrShowError(() -> {
            String saveGameFilePath = inputs.getSaveFilePath();
            File saveGameFile = StartInputValidator.requireFilePath(saveGameFilePath, "Save file must be selected");
            gameService.loadGame(saveGameFile);
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
            errorSink.accept(exception.getMessage());
        }
    }

    /**
     * Shows the main view using the active {@link GameService}.
     */
    private void showMainView() {
        showMainViewAction.run();
    }

    /**
     * Default error sink used in production. Displays the message in a JavaFX
     * {@link Alert} dialog. Tests inject a different consumer to avoid
     * starting the JavaFX toolkit.
     *
     * @param message the error message to show
     */
    private static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could not open game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays the start screen on the primary stage and binds UI events.
     */
    public void show() {
        bindEvents();
        stage.setTitle("Millions");
        stage.setScene(view.getScene());
        stage.show();
    }
}
