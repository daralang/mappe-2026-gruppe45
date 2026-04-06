package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.MainView;
import javafx.stage.Stage;

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

    private void bindEvents() {
        view.getHeader().getDashboardButton().setOnAction(e -> view.showDashboard());
        view.getHeader().getExchangeButton().setOnAction(e -> view.showExchange());
        view.getHeader().getSaveButton().setOnAction(e -> handleSaveGame());
        view.getHeader().getExitButton().setOnAction(e -> handleExitGame());

        view.getWeekBar().getAdvanceButton().setOnAction(e -> handleAdvanceWeek());
    }

    private void handleAdvanceWeek() {
        gameManager.advanceWeek();
        view.getWeekBar().setWeek(gameManager.getExchange().getWeek());
    }

    private void handleSaveGame() {
        // TODO: åpne filvelger og lagre spill
    }

    private void handleExitGame() {
        // TODO: avslutt (trengs bekreftelse og evt. en mulighet til å lagre herfra også)
        stage.close();
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