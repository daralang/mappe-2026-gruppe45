package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.MainView;
import edu.ntnu.idatt2003.millions.view.dialog.ExitDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller for the main view of the application.
 * Handles save, exit and advance-week actions and delegates them to
 * {@link GameService}. Navigation between Dashboard and Exchange is
 * purely visual state and handled inside the view.
 */
public class MainController {

    private final Stage stage;
    private final MainView view;
    private final GameService gameService;
    private final ForcedSaleController forcedSaleController;

    /**
     * Constructs a new MainController and creates the main view.
     *
     * @param stage       the primary stage
     * @param gameService the game manager containing player and exchange
     */
    public MainController(Stage stage, GameService gameService) {
        this.stage = stage;
        this.gameService = gameService;
        this.forcedSaleController = new ForcedSaleController(gameService);
        PortfolioController portfolioController = new PortfolioController(gameService);
        LoanController loanController = new LoanController(gameService);
        this.view = new MainView(
                stage,
                gameService,
                portfolioController,
                loanController,
                this::handleSaveGame,
                this::handleExitGame,
                this::handleAdvanceWeek
        );
    }

    /**
     * Advances the game by one week. If the player cannot cover weekly interest
     * from cash, opens the forced-sale dialog instead of advancing directly.
     */
    private void handleAdvanceWeek() {
        if (gameService.getPlayer().canCoverInterestThisWeek()) {
            gameService.advanceWeek();
        } else {
            forcedSaleController.open(gameService.getPlayer().getWeeklyInterestDue());
        }
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
            gameService.saveGame(file);
            return true;
        }
        return false;
    }

    /**
     * Shows the exit confirmation dialog. Saving or exiting is handled
     * by the callbacks passed to {@link ExitDialog}.
     */
    private void handleExitGame() {
        new ExitDialog(this::handleSaveAndExit, stage::close).show();
    }

    private void handleSaveAndExit() {
        if (handleSaveGame()) {
            stage.close();
        }
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