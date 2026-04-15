package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.view.StartView;
import java.util.Objects;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

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

    /**
     * Constructs a new StartController and binds all UI events.
     *
     * @param stage the primary application stage
     * @throws NullPointerException if stage is null
     */
    public StartController(Stage stage) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        this.stage = stage;
        this.view = new StartView();
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
    }

    /**
     * Opens a file chooser for selecting a stock data file (CSV or JSON).
     * If a file is chosen, the path is set in the view and the currency
     * selector becomes enabled.
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
     * Validates input and starts a new game session.
     *
     * @throws IllegalArgumentException if name, capital, or stock file path is blank
     */
    private void handleStartGame() {
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

        // TODO: parse capital, load exchange from file, create Player, start MainController
    }

    /**
     * Validates input and loads an existing saved game.
     *
     * @throws IllegalArgumentException if no save file has been selected
     */
    private void handleLoadGame() {
        String saveFilePath = view.getSaveFilePath();

        if (saveFilePath.isBlank()) {
            throw new IllegalArgumentException("Save file must be selected");
        }

        // TODO: load GameState, create MainController from state
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