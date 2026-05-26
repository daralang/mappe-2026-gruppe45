// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dialog.GameOverModal;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Controller for the game-over flow.
 */
public class GameOverController {

    private final GameService gameService;
    private final Stage ownerStage;
    private final EndGameActions actions;

    /**
     * Constructs a new GameOverController.
     *
     * @param gameService the game service used to read end-of-game state
     * @param ownerStage  the stage that owns the game-over modal
     * @param actions     the four end-of-game action callbacks; see {@link EndGameActions}
     */
    public GameOverController(GameService gameService, Stage ownerStage, EndGameActions actions) {
        this.gameService = gameService;
        this.ownerStage = ownerStage;
        this.actions = actions;
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
                actions.onNewGame(), actions.saveAction(), actions.sellAllAction(), actions.noSaveAction()).show();
    }
}
