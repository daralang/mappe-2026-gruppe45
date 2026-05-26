// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Stateless read service for portfolio value queries.
 */
public class PortfolioService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Returns the total market value of the player's portfolio in NOK
     * ({@code salesPrice × quantity} per share, converted to NOK, no fees deducted).
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return total portfolio market value in NOK
     */
    public BigDecimal getValue(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getNetWorth(converter);
    }

    /**
     * Returns the current market value of a share position in NOK
     * ({@code salesPrice × quantity × exchange rate}, no fees deducted).
     *
     * @param share     the share position to evaluate
     * @param converter the currency converter used to translate to NOK
     * @return market value of the position in NOK
     */
    public BigDecimal getShareValueInNok(Share share, CurrencyConverter converter) {
        return converter.convert(share.getCurrentValue(), share.getStock().getCurrency(), NOK);
    }

    /**
     * Returns the unrealized return on a share position in NOK
     * ({@code currentValue − cost}, converted to NOK at the current rate).
     *
     * @param share     the share position to evaluate
     * @param converter the currency converter used to translate to NOK
     * @return unrealized gain or loss in NOK; negative if the position is at a loss
     */
    public BigDecimal getShareReturnInNok(Share share, CurrencyConverter converter) {
        return converter.convert(share.getReturnNative(), share.getStock().getCurrency(), NOK);
    }

    /**
     * Returns the liquidation value of a share position in NOK: the net proceeds
     * after commission and tax if the entire position were sold now.
     *
     * @param share     the share position to evaluate
     * @param converter the currency converter used to translate to NOK
     * @return net sale proceeds in NOK
     */
    public BigDecimal getLiquidationValueInNok(Share share, CurrencyConverter converter) {
        return converter.convert(share.getLiquidationValue(), share.getStock().getCurrency(), NOK);
    }

    /**
     * Returns the total unrealized return across all portfolio positions in NOK.
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return total unrealized gain or loss in NOK; negative if the portfolio is at a loss
     */
    public BigDecimal getTotalReturnInNok(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getTotalReturnInNok(converter);
    }

    /**
     * Returns the total portfolio return as a percentage of total cost in NOK.
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return return percentage; negative if the portfolio is at a loss
     */
    public BigDecimal getTotalReturnPercent(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getTotalReturnPercent(converter);
    }

    /**
     * Returns the total cost basis of all portfolio positions in NOK.
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
     * Returns the total portfolio value change for the current week in NOK.
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return total weekly return in NOK; negative when the portfolio lost value
     */
    public BigDecimal getWeeklyReturnInNok(Player player, CurrencyConverter converter) {
        return player.getPortfolio().getShares().stream()
                .map(share -> converter.convert(
                        share.getStock().getLatestPriceChange().multiply(share.getQuantity()),
                        share.getStock().getCurrency(),
                        NOK))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the portfolio's weekly return as a percentage of the total cost basis in NOK.
     *
     * <p>Returns zero when the portfolio is empty or the invested amount is zero.
     *
     * @param player    the player whose portfolio to inspect
     * @param converter the currency converter used to translate values to NOK
     * @return weekly return percent, negative when the portfolio lost value this week
     */
    public BigDecimal getWeeklyReturnPercent(Player player, CurrencyConverter converter) {
        BigDecimal invested = getInvestedAmount(player, converter);
        if (invested.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getWeeklyReturnInNok(player, converter)
                .divide(invested, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
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
