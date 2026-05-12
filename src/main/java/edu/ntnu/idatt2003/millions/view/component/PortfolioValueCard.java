package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;

/**
 * Widget card displaying the total market value of the player's portfolio in NOK.
 * Accepts a title key so it can be reused in different views with different labels.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class PortfolioValueCard extends SimpleWidgetCard {

    /**
     * Constructs a new PortfolioValueCard.
     *
     * @param gameService the game service containing player and exchange state
     * @param titleKey    the i18n key for the card title
     */
    public PortfolioValueCard(GameService gameService, String titleKey) {
        super(gameService,
                titleKey,
                () -> CurrencyFormatter.format(
                        new PortfolioService().getValue(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())));
    }
}
