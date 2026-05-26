package edu.ntnu.idatt2003.millions.view.ingame.component.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.player.PortfolioService;
import edu.ntnu.idatt2003.millions.util.currency.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.exchange.stock.card.SimpleWidgetCard;

/**
 * Widget card displaying the total market value of the player's portfolio in NOK.
 * Accepts a title key and an optional subtitle key so it can be reused in different
 * views with different labels. Delegates rendering and observer registration to
 * {@link SimpleWidgetCard}. Updates automatically on each game state change via GameObserver.
 */
public class PortfolioValueCard extends SimpleWidgetCard {

    /**
     * Constructs a new PortfolioValueCard without a subtitle.
     *
     * @param gameService the game service containing player and exchange state
     * @param titleKey    the i18n key for the card title
     */
    public PortfolioValueCard(GameService gameService, String titleKey) {
        this(gameService, titleKey, null);
    }

    /**
     * Constructs a new PortfolioValueCard with a static subtitle resolved from an i18n key.
     *
     * @param gameService the game service containing player and exchange state
     * @param titleKey    the i18n key for the card title
     * @param subtitleKey the i18n key for the card subtitle, or {@code null} for no subtitle
     */
    public PortfolioValueCard(GameService gameService, String titleKey, String subtitleKey) {
        super(gameService,
                titleKey,
                () -> CurrencyFormatter.format(
                        new PortfolioService().getValue(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())),
                subtitleKey != null ? () -> LanguageManager.get(subtitleKey) : null);
    }
}
