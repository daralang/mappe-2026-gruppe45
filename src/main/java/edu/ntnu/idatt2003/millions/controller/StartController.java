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

    /**
     * Constructs a new StartController with a {@link GameManager} and binds all UI events.
     *
     * @param stage the primary application stage
     * @throws NullPointerException if stage is null
     */
    public StartController(Stage stage) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        this.stage = stage;
        this.view = new StartView();
        this.gameManager = new GameManager();
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
     * Opens a file chooser for selecting a stock data file (CSV or JSON).
     * If a file is chosen, the path is shown in the view.
     */
    private void handleBrowseStockFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select stock file");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Data files", "*.csv", "*.json"));
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
     * and shows the main view.
     */
    private void handleStartGame() {
        try {
            String name = view.getName();
            String capital = view.getCapital();
            String stockFilePath = view.getStockFilePath();

            if (name.isBlank()) {
                throw new IllegalArgumentException("Player name cannot be blank");
            }
            if (capital.isBlank()) {
                throw new IllegalArgumentException("Starting capital cannot be blank");
            }
            if (stockFilePath.isBlank()) {
                throw new IllegalArgumentException("Stock file must be selected");
            }

            BigDecimal parsedCapital = new BigDecimal(capital);
            gameManager.createNewGame(name, parsedCapital, new File(stockFilePath));
            showMainView();
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    /**
     * Validates input, loads an existing saved game through {@link GameManager},
     * and shows the main view.
     *
     * @throws IllegalArgumentException if no save file has been selected
     */
    private void handleLoadGame() {
        String saveFilePath = view.getSaveFilePath();

        if (saveFilePath.isBlank()) {
            throw new IllegalArgumentException("Save file must be selected");
        }

        gameManager.loadGame(new File(saveFilePath));
        showMainView();
    }

    /**
     * Shows the main view using the active {@link GameManager}.
     */
    private void showMainView() {
        new MainController(stage, gameManager).show();
    }

    /**
     * Shows an error dialog with a user-facing message.
     *
     * @param message the error message to show
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Could not start game");
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
