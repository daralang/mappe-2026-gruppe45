package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Stateless read service for portfolio value queries.
 * All methods accept domain objects as parameters so this service holds no state.
 * Provides both per-share and aggregate queries used by portfolio and stocks views.
 */
public class PortfolioService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Returns the total market value of the player's portfolio in NOK
     * ({@code salesPrice × quantity} per share, converted to NOK, no fees deducted).
     */
    public BigDecimal getValue(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getNetWorth(converter);
    }

    /**
     * Returns the current market value of a share position in NOK
     * (salesPrice × quantity × exchange rate, no fees deducted).
     */
    public BigDecimal getShareValueInNok(Share share, CurrencyConverter converter) {
        return converter.convert(share.getCurrentValue(), share.getStock().getCurrency(), NOK);
    }

    /**
     * Returns the unrealized return on a share position in NOK
     * (currentValue − cost, converted to NOK at the current rate).
     */
    public BigDecimal getShareReturnInNok(Share share, CurrencyConverter converter) {
        return converter.convert(share.getReturnNative(), share.getStock().getCurrency(), NOK);
    }

    /** Returns the total unrealized return across all portfolio positions in NOK. */
    public BigDecimal getTotalReturnInNok(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getTotalReturnInNok(converter);
    }

    /** Returns the total portfolio return as a percentage of total cost in NOK. */
    public BigDecimal getTotalReturnPercent(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getTotalReturnPercent(converter);
    }

    /**
     * Returns the total cost basis of all portfolio positions in NOK.
     * Computed as current market value minus total unrealized return.
     * Returns zero when the portfolio is empty.
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return total amount invested in NOK
     */
    public BigDecimal getInvestedAmount(Player player, CurrencyConverter converter) {
        return getValue(player, converter).subtract(getTotalReturnInNok(player, converter));
    }

    /**
     * Returns the number of distinct stock positions in the player's portfolio.
     *
     * @param player the player whose portfolio to inspect
     * @return number of unique stocks held
     */
    public long getPositionCount(Player player) {
        return player.getPortfolio().getShares().stream()
                .map(share -> share.getStock().getSymbol())
                .distinct()
                .count();
    }
}
