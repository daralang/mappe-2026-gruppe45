package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.SimpleWidgetCard;

/**
 * Widget card displaying the total market value of the player's portfolio in NOK.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver
 */
public class StocksPortfolioValueCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksPortfolioValueCard.
     *
     * @param gameService the game service containing player and exchange state
     */
    public StocksPortfolioValueCard(GameService gameService) {
        super(gameService,
                "exchange.stocks.portfolio",
                () -> CurrencyFormatter.format(
                        new PortfolioService().getValue(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())));
    }
}
