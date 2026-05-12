package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.SimpleWidgetCard;

/**
 * Widget card displaying the total amount the player has invested in NOK.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class StocksInvestedCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksInvestedCard.
     *
     * @param gameService the game service containing player and exchange state
     */
    public StocksInvestedCard(GameService gameService) {
        super(gameService,
                "exchange.stocks.invested",
                () -> CurrencyFormatter.format(
                        new PortfolioService().getInvestedAmount(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())));
    }
}
