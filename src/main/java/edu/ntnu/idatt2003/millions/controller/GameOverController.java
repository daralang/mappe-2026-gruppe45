package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dialog.GameOverModal;
import javafx.stage.Stage;

import java.math.BigDecimal;

/**
 * Controller for the game-over flow. Collects the end-of-game context
 * (week, obligations, liquidation value) and delegates rendering to
 * {@link GameOverModal}.
 */
public class GameOverController {

    private final GameService gameService;
    private final Stage ownerStage;
    private final Runnable onNewGame;

    public GameOverController(GameService gameService, Stage ownerStage, Runnable onNewGame) {
        this.gameService = gameService;
        this.ownerStage = ownerStage;
        this.onNewGame = onNewGame;
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

        new GameOverModal(failedWeek, totalObligations, totalLiquidationValue, ownerStage, onNewGame).show();
    }
}
