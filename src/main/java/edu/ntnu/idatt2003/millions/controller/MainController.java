package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.MainView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller for the main view of the application.
 * Handles navigation between the different views and connects
 * the header buttons to the correct actions.
 */
public class MainController {

    private final Stage stage;
    private final MainView view;
    private final GameManager gameManager;

    /**
     * Constructs a new MainController and sets up navigation.
     *
     * @param stage       the primary stage
     * @param gameManager the game manager containing player and exchange
     */
    public MainController(Stage stage, GameManager gameManager) {
        this.stage = stage;
        this.gameManager = gameManager;
        this.view = new MainView(gameManager);

        bindEvents();
    }

    /**
     * Binds UI events to their corresponding handler methods.
     */
    private void bindEvents() {
        view.getHeader().getDashboardButton().setOnAction(e -> view.showDashboard());
        view.getHeader().getExchangeButton().setOnAction(e -> view.showExchange());
        view.getHeader().getSaveButton().setOnAction(e -> handleSaveGame());
        view.getHeader().getExitButton().setOnAction(e -> handleExitGame());

        view.getWeekBar().getAdvanceButton().setOnAction(e -> handleAdvanceWeek());
    }

    /**
     * Advances the game by one week.
     * Registered {@link GameObserver}s are notified automatically by {@link GameManager}.
     */
    private void handleAdvanceWeek() {
        gameManager.advanceWeek();
    }

    /**
     * Opens a file chooser dialog and saves the current game state to a JSON file.
     *
     * @return true if the game was saved, false if the user cancelled the dialog
     */
    private boolean handleSaveGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageManager.get("nav.saveGame"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json")
        );
        fileChooser.setInitialFileName("savegame.json");

        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            gameManager.saveGame(file);
            return true;
        }
        return false;
    }

    /**
     * Shows a confirmation dialog when the user wants to exit the game.
     * The user can choose to save before exiting, exit without saving,
     * or cancel by closing the dialog with the X button.
     */
    private void handleExitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.get("nav.exitGame"));
        alert.setHeaderText(LanguageManager.get("exit.confirmHeader"));
        alert.setContentText(LanguageManager.get("exit.confirmContent"));

        ButtonType saveAndExit = new ButtonType(
                LanguageManager.get("exit.saveAndExit")
        );
        ButtonType exitWithoutSaving = new ButtonType(
                LanguageManager.get("exit.exitWithoutSaving")
        );
        ButtonType cancel = new ButtonType("", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveAndExit, exitWithoutSaving, cancel);

        alert.getDialogPane().lookup(".button-bar")
                .setStyle("-fx-alignment: center;");
        alert.getDialogPane().lookupButton(cancel).setVisible(false);
        alert.getDialogPane().lookupButton(cancel).setManaged(false);

        alert.showAndWait().ifPresent(response -> {
            if (response == saveAndExit) {
                boolean saved = handleSaveGame();
                if (saved) {
                    stage.close();
                }
            } else if (response == exitWithoutSaving) {
                stage.close();
            }
        });
    }

    /**
     * Shows the main view on the stage.
     */
    public void show() {
        stage.getScene().setRoot(view.getRoot());
        stage.setTitle(LanguageManager.get("app.title"));
        stage.show();
    }
}