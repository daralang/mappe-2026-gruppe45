package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.view.StartView;
import java.util.Objects;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class StartController {

    private final Stage stage;
    private final StartView view;

    public StartController(Stage stage) {
        Objects.requireNonNull(stage, "Stage cannot be null");
        this.stage = stage;
        this.view = new StartView();
        bindEvents();
    }

    private void bindEvents() {
        view.getBrowseStockFileButton().setOnAction(e -> handleBrowseStockFile());
        view.getBrowseSaveFileButton().setOnAction(e -> handleBrowseSaveFile());
        view.getStartButton().setOnAction(e -> handleStartGame());
        view.getLoadButton().setOnAction(e -> handleLoadGame());
    }

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

    private void handleLoadGame() {
        String saveFilePath = view.getSaveFilePath();

        if (saveFilePath.isBlank()) {
            throw new IllegalArgumentException("Save file must be selected");
        }

        // TODO: load GameState, create MainController from state
    }

    public void show() {
        stage.setTitle("Millions");
        stage.setScene(view.getScene());
        stage.show();
    }


}