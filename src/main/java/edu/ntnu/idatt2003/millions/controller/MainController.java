package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.keyboard.KeyboardNavigationService;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.MainView;
import edu.ntnu.idatt2003.millions.view.component.toast.ToastType;
import edu.ntnu.idatt2003.millions.view.dialog.EndGameDialog;
import edu.ntnu.idatt2003.millions.view.titlebar.TitleBar;
import edu.ntnu.idatt2003.millions.view.titlebar.TitleBarFactory;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Controller for the main view of the application.
 * Handles save, exit, advance-week actions and global keyboard shortcuts.
 * Delegates game state changes to {@code GameService}.
 */
public class MainController {

    private final Stage stage;
    private final MainView view;
    private final GameService gameService;
    private final ToastService toastService;
    private final ForcedSaleController forcedSaleController;
    private final GameOverController gameOverController;
    private final KeyboardNavigationService keyboardService = new KeyboardNavigationService();

    /**
     * Constructs a new MainController and creates the main view.
     *
     * @param stage       the primary stage
     * @param gameService the game manager containing player and exchange
     */
    public MainController(Stage stage, GameService gameService, ToastService toastService) {
        this.stage = stage;
        this.gameService = gameService;
        this.toastService = toastService;
        this.forcedSaleController = new ForcedSaleController(gameService);
        this.gameOverController = new GameOverController(gameService, stage,
                () -> new StartController(stage, gameService).show());
        TradeController tradeController = new TradeController(gameService);
        LoanController loanController = new LoanController(gameService);
        TitleBar titleBar = TitleBarFactory.create(stage, gameService);
        titleBar.setOnNewGame(this::handleNewGame);
        titleBar.setOnSave(this::handleSaveGame);
        titleBar.setOnExit(this::handleExitGame);
        gameService.addObserver(titleBar::onGameUpdated);
        this.view = new MainView(
                stage,
                gameService,
                tradeController,
                loanController,
                titleBar,
                this::handleAdvanceWeek,
                toastService
        );
    }

    /**
     * Advances the game by one week. If the player cannot cover all obligations
     * (interest + any maturing loan principals) from cash, opens the forced-sale
     * dialog instead of advancing directly.
     */
    private void handleAdvanceWeek() {
        int nextWeek = gameService.getExchange().getWeek() + 1;
        if (gameService.getPlayer().canCoverObligationsThisWeek(nextWeek)) {
            gameService.advanceWeek();
        } else if (gameService.getPlayer().canCoverWithFullLiquidation(
                nextWeek, gameService.getCurrencyConverter())) {
            forcedSaleController.open(nextWeek);
        } else {
            gameService.declareGameOver();
            gameOverController.open(nextWeek);
        }
    }

    /**
     * Saves the current game. If a save location is already known from this
     * session (earlier save or loaded file), writes there directly. Otherwise
     * opens a file-chooser dialog so the player can pick a location.
     *
     * @return true if the game was saved, false if the player cancelled or the save failed
     */
    private boolean handleSaveGame() {
        Optional<File> existing = gameService.getCurrentSaveFile();
        if (existing.isPresent()) {
            return saveToFile(existing.get());
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(LanguageManager.get("nav.saveGame"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON", "*.json")
        );
        fileChooser.setInitialFileName("savegame.json");
        File chosen = fileChooser.showSaveDialog(stage);
        if (chosen == null) return false;
        return saveToFile(chosen);
    }

    /**
     * Writes the game to {@code file} and shows a success toast.
     * On failure clears the stored path and shows an error toast so the
     * player can pick a new location on the next attempt.
     *
     * @return true on success, false on failure
     */
    private boolean saveToFile(File file) {
        try {
            gameService.saveGame(file);
            toastService.show(LanguageManager.get("toast.gameSaved"), ToastType.SUCCESS);
            return true;
        } catch (RuntimeException e) {
            gameService.clearCurrentSaveFile();
            toastService.show(LanguageManager.get("toast.gameSaveFailed"), ToastType.ERROR);
            return false;
        }
    }

    /**
     * Shows the "start new game" confirmation dialog. On success navigates
     * back to the start screen; on save failure keeps the dialog open.
     */
    private void handleNewGame() {
        new EndGameDialog(
                "nav.newGame",
                "newGame.confirmHeader",
                "newGame.confirmContent",
                "newGame.saveAndStartNew",
                "newGame.sellAllAndStartNew",
                "newGame.startNewWithoutSaving",
                this::handleSaveGame,
                gameService::sellAllAndExit,
                gameService::recordLeaderboardEntry,
                this::showStartView
        ).show();
    }

    private void handleExitGame() {
        new EndGameDialog(
                "nav.exitGame",
                "exit.confirmHeader",
                "exit.confirmContent",
                "exit.saveAndExit",
                "exit.sellAllAndExit",
                "exit.exitWithoutSaving",
                this::handleSaveGame,
                gameService::sellAllAndExit,
                gameService::recordLeaderboardEntry,
                stage::close
        ).show();
    }

    /**
     * Detaches the keyboard service from the current scene and navigates to
     * the start screen. Detaching first ensures the event filter is removed
     * before {@link StartController#show()} replaces the scene on the stage.
     */
    private void showStartView() {
        keyboardService.detach();
        new StartController(stage, gameService).show();
    }

    /**
     * Shows the main view on the stage and registers global keyboard shortcuts.
     */
    public void show() {
        stage.getScene().setRoot(view.getRoot());
        stage.setTitle(LanguageManager.get("app.title"));
        registerShortcuts();
        stage.show();
    }

    /**
     * Registers application-wide keyboard shortcuts on the app-lifetime scene.
     *
     * <p>Top-level navigation uses {@link KeyCombination#SHORTCUT_DOWN} (Cmd on macOS,
     * Ctrl on Windows/Linux). Dashboard tab shortcuts use {@link KeyCombination#SHIFT_DOWN}+1–4
     * and jump directly to the respective sub-view, switching to the dashboard first
     * if another view is active.</p>
     *
     * <p>Cmd/Ctrl+F delegates to {@link MainView#focusActiveSearch()}, which
     * routes focus to whichever search field belongs to the currently visible
     * view — keeping shortcut registration out of the view layer.</p>
     */
    private void registerShortcuts() {
        keyboardService.attach(stage.getScene());
        var reg = keyboardService.globalShortcuts();
        reg.register(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN), view::showDashboard);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN), view::showExchange);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN), view::showLeaderboard);
        reg.register(new KeyCodeCombination(KeyCode.S,      KeyCombination.SHORTCUT_DOWN), () -> handleSaveGame());
        reg.register(new KeyCodeCombination(KeyCode.ENTER,  KeyCombination.SHORTCUT_DOWN), this::handleAdvanceWeek);
        reg.register(new KeyCodeCombination(KeyCode.F,      KeyCombination.SHORTCUT_DOWN), view::focusActiveSearch);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHIFT_DOWN), view::showDashboardPortfolio);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHIFT_DOWN), view::showDashboardTransactions);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHIFT_DOWN), view::showDashboardWatchlist);
        reg.register(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHIFT_DOWN), view::showDashboardLoans);
    }
}