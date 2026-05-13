package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import java.math.BigDecimal;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.card.SimpleWidgetCard;

/**
 * Widget card displaying the player's total unrealized return in NOK combined
 * with the total return as a signed percentage since purchase, e.g.
 * {@code -4,87 NOK (-5.4%)}. Colour styling is driven by the total
 * unrealized return. A static subtitle is resolved from an i18n key.
 * Delegates rendering and observer registration to {@link SimpleWidgetCard}.
 * Updates automatically on each game state change via GameObserver.
 */
public class StocksUnrealizedReturnCard extends SimpleWidgetCard {

    /**
     * Constructs a new StocksUnrealizedReturnCard.
     * The value label combines the total unrealized NOK return with the
     * total return percentage since purchase, formatted as
     * {@code ↗ +X NOK (+Y%)} or {@code ↘ -X NOK (-Y%)}.
     *
     * @param gameService the game service containing player and exchange state
     * @param subtitleKey the i18n key for the static subtitle label
     */
    public StocksUnrealizedReturnCard(GameService gameService, String subtitleKey) {
        super(gameService,
                "exchange.stocks.unrealized",
                () -> {
                    PortfolioService ps = new PortfolioService();
                    BigDecimal totalNok = ps.getTotalReturnInNok(
                            gameService.getPlayer(),
                            gameService.getCurrencyConverter());
                    String prefix = totalNok.signum() >= 0 ? "↗ +" : "↘ ";
                    String nok = prefix + CurrencyFormatter.format(totalNok);
                    String pct = ChangeFormatter.formatSignedPercent(
                            ps.getTotalReturnPercent(
                                    gameService.getPlayer(),
                                    gameService.getCurrencyConverter()));
                    return nok + " (" + pct + ")";
                },
                () -> LanguageManager.get(subtitleKey),
                () -> new PortfolioService().getTotalReturnInNok(
                        gameService.getPlayer(),
                        gameService.getCurrencyConverter()));
    }
}
