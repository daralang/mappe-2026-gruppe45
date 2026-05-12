package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;

/**
 * Widget card displaying the player's currently available cash balance in NOK.
 * Accepts a title key so it can be reused in different views with different labels.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class AvailableFundsCard extends SimpleWidgetCard {

    /**
     * Constructs a new AvailableFundsCard.
     *
     * @param gameService the game service containing player state
     * @param titleKey    the i18n key for the card title
     */
    public AvailableFundsCard(GameService gameService, String titleKey) {
        super(gameService,
                titleKey,
                () -> CurrencyFormatter.format(gameService.getPlayer().getMoney()));
    }
}
