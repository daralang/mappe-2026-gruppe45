package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.StartView;
import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controller for the {@link StartView}.
 *
 * <p>Handles user interactions on the start screen, including
 * file selection for stock data and saved games, as well as
 * starting or loading a game session.
 */
public class StartController {

    private final Stage stage;
    private final StartView view;
    private final GameManager gameManager;
    private final Runnable showMainViewAction;

    /**
     * Constructs a new StartController with a default {@link GameManager}.
     *
     * <p>This constructor is used by the application startup flow. It delegates
     * to {@link #StartController(Stage, GameManager)} so alternate startup paths
     * can inject their own game manager.</p>
     *
     * @param stage the primary application stage
     * @throws NullPointerException if stage is null
     */
    public StartController(Stage stage) {
        this(stage, new GameManager());
    }

    /**
     * Constructs a new StartController with the given {@link GameManager} and binds all UI events.
     *
     * <p>Injecting the manager keeps the controller flexible while preserving
     * the normal production flow through {@link #StartController(Stage)}.</p>
     *
     * @param stage       the primary application stage
     * @param gameManager the game manager used to create or load game state
     * @throws NullPointerException if stage or game manager is null
     */
    public StartController(Stage stage, GameManager gameManager) {
        this(stage, gameManager, new StartView(), () -> new MainController(stage, gameManager).show());
    }

    /**
     * Constructs a new StartController with injected dependencies and binds all UI events.
     *
     * <p>This constructor is package-private so controller tests in the same package
     * can inject a controlled {@link StartView} and navigation action while production
     * code uses the public constructors.</p>
     *
     * @param stage              the primary application stage
     * @param gameManager        the game manager used to create or load game state
     * @param view               the start view that exposes user input and controls
     * @param showMainViewAction the action used to navigate to the main view
     * @throws NullPointerException if stage, game manager, view, or navigation action is null
     */
    StartController(Stage stage, GameManager gameManager, StartView view, Runnable showMainViewAction) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        Objects.requireNonNull(gameManager, "GameManager cannot be null");
        Objects.requireNonNull(view, "StartView cannot be null");
        Objects.requireNonNull(showMainViewAction, "Show main view action cannot be null");
        this.stage = stage;
        this.view = view;
        this.gameManager = gameManager;
        this.showMainViewAction = showMainViewAction;
        bindEvents();
    }

    /**
     * Registers event handlers for all interactive controls in the view.
     */
    private void bindEvents() {
        view.getBrowseStockFileButton().setOnAction(e -> handleBrowseStockFile());
        view.getBrowseSaveFileButton().setOnAction(e -> handleBrowseSaveFile());
        view.getStartButton().setOnAction(e -> handleStartGame());
        view.getLoadButton().setOnAction(e -> handleLoadGame());
        view.getDropZone().setOnDragDropped(e -> {
            var db = e.getDragboard();
            if (db.hasFiles()) {
                view.setStockFilePath(db.getFiles().getFirst().getAbsolutePath());
                e.setDropCompleted(true);
            }
            e.consume();
        });
    }

    /**
     * Opens a file chooser for selecting a stock data file (CSV).
     * If a file is chosen, the path is shown in the view.
     */
    private void handleBrowseStockFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select stock file");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Data files", "*.csv"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            view.setStockFilePath(file.getAbsolutePath());
        }
    }

    /**
     * Opens a file chooser for selecting a previously saved game file (JSON).
     * If a file is chosen, the path is shown in the view.
     */
    private void handleBrowseSaveFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select save file");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Save files", "*.json"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            view.setSaveFilePath(file.getAbsolutePath());
        }
    }

    /**
     * Validates input, starts a new game session through {@link GameManager},
     * and shows the main view. A custom stock file is optional; if none is
     * selected, the default stock data is used.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to call the handler without simulating a JavaFX button click.</p>
     */
    void handleStartGame() {
        try {
            String name = view.getName();
            String capital = view.getCapital();
            String stockFilePath = view.getStockFilePath();

            if (name.isBlank()) {
                throw new IllegalArgumentException("Player name cannot be blank");
            }

            BigDecimal parsedCapital = StartInputValidator.parseCapital(capital);
            if (stockFilePath.isBlank()) {
                gameManager.createNewGame(name, parsedCapital);
            } else {
                File stockFile = StartInputValidator.requireCsvFilePath(stockFilePath);
                gameManager.createNewGame(name, parsedCapital, stockFile);
            }
            showMainView();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Validates input, loads an existing saved game through {@link GameManager},
     * and shows the main view.
     *
     * <p>Package-private visibility allows controller tests in this package
     * to call the handler without simulating a JavaFX button click.</p>
     */
    void handleLoadGame() {
        try {
            String saveFilePath = view.getSaveFilePath();
            File saveFile = StartInputValidator.requireFilePath(saveFilePath, "Save file must be selected");
            gameManager.loadGame(saveFile);
            showMainView();
        } catch (RuntimeException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Shows the main view using the active {@link GameManager}.
     */
    private void showMainView() {
        showMainViewAction.run();
    }

    /**
     * Shows an error dialog with a user-facing message.
     *
     * @param message the error message to show
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could not open game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Displays the start screen on the primary stage.
     */
    public void show() {
        stage.setTitle("Millions");
        stage.setScene(view.getScene());
        stage.show();
    }
}
