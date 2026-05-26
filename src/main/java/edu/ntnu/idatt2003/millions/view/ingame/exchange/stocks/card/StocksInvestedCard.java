package edu.ntnu.idatt2003.millions.view.ingame.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.player.PortfolioService;
import edu.ntnu.idatt2003.millions.util.currency.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;

import java.text.MessageFormat;

/**
 * Widget card displaying the total amount the player has invested in NOK.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class StocksInvestedCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksInvestedCard with a dynamic subtitle showing the
     * number of distinct stock positions, formatted using the given i18n key.
     *
     * @param gameService the game service containing player and exchange state
     * @param subtitleKey the i18n key for the subtitle, with {@code {0}} for the position count
     */
    public StocksInvestedCard(GameService gameService, String subtitleKey) {
        super(gameService,
                "exchange.stocks.invested",
                () -> CurrencyFormatter.format(
                        new PortfolioService().getInvestedAmount(
                                gameService.getPlayer(),
                                gameService.getCurrencyConverter())),
                () -> {
                    long positions = new PortfolioService().getPositionCount(
                            gameService.getPlayer());
                    return MessageFormat.format(
                            LanguageManager.get(subtitleKey),
                            positions);
                });
    }
}
