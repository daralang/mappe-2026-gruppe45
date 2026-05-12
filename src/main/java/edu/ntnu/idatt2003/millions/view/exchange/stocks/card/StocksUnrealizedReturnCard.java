package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.SimpleWidgetCard;

/**
 * Widget card displaying the player's total unrealized return in NOK,
 * with a signed percentage subtitle and green/red colour styling.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class StocksUnrealizedReturnCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksUnrealizedReturnCard.
     *
     * @param gameService the game service containing player and exchange state
     */
    public StocksUnrealizedReturnCard(GameService gameService) {
        super(gameService,
                "exchange.stocks.unrealized",
                () -> {
                    PortfolioService ps = new PortfolioService();
                    return CurrencyFormatter.format(
                            ps.getTotalReturnInNok(
                                    gameService.getPlayer(),
                                    gameService.getCurrencyConverter()));
                },
                () -> ChangeFormatter.formatSignedPercent(
                        new PortfolioService().getTotalReturnPercent(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())),
                () -> new PortfolioService().getTotalReturnInNok(
                        gameService.getPlayer(),
                        gameService.getCurrencyConverter()));
    }
}
