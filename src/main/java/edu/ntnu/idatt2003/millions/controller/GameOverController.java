// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dialog.GameOverModal;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.function.BooleanSupplier;

/**
 * Controller for the game-over flow.
 */
public class GameOverController {

    private final GameService gameService;
    private final Stage ownerStage;
    private final Runnable onNewGame;
    private final BooleanSupplier saveAction;
    private final Runnable sellAllAction;
    private final Runnable noSaveAction;

    /**
     * Constructs a new GameOverController.
     *
     * @param gameService   the game service used to read end-of-game state
     * @param ownerStage    the stage that owns the game-over modal
     * @param onNewGame     action invoked when the player chooses to start a new game
     * @param saveAction    action invoked to save the game; returns {@code true} on success
     * @param sellAllAction action invoked to liquidate all positions before exiting
     * @param noSaveAction  action invoked when the player exits without saving
     */
    public GameOverController(GameService gameService, Stage ownerStage, Runnable onNewGame,
                              BooleanSupplier saveAction, Runnable sellAllAction, Runnable noSaveAction) {
        this.gameService = gameService;
        this.ownerStage = ownerStage;
        this.onNewGame = onNewGame;
        this.saveAction = saveAction;
        this.sellAllAction = sellAllAction;
        this.noSaveAction = noSaveAction;
    }

    /**
     * Opens the game-over modal. The week passed is the upcoming week the player
     * failed to advance to (obligations were computed for that week).
     *
     * @param failedWeek the week number whose obligations the player could not cover
     */
    public void open(int failedWeek) {
        Player player = gameService.getPlayer();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        BigDecimal totalObligations = player.getTotalObligationsThisWeek(failedWeek);
        BigDecimal totalLiquidationValue = player.getTotalLiquidationValue(converter);

        new GameOverModal(failedWeek, totalObligations, totalLiquidationValue, ownerStage,
                onNewGame, saveAction, sellAllAction, noSaveAction).show();
    }
}
