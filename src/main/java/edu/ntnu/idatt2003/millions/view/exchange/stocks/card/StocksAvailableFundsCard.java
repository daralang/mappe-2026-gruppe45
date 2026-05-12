package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.SimpleWidgetCard;

/**
 * Widget card displaying the player's available cash balance in NOK.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class StocksAvailableFundsCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksAvailableFundsCard.
     *
     * @param gameService the game service containing player state
     */
    public StocksAvailableFundsCard(GameService gameService) {
        super(gameService,
                "exchange.stocks.available",
                () -> CurrencyFormatter.format(gameService.getPlayer().getMoney()));
    }
}
