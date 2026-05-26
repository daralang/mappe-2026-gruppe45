// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;

/**
 * Stateless read service for realized-return queries (gains, losses, tax, commission).
 */
public class RealizedReturnsService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Returns the total realized gain across all profitable sales, converted to NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to translate values to NOK
     * @return total realized gain in NOK; zero if no profitable sales exist
     */
    public BigDecimal getGainsInNok(Player player, CurrencyConverter converter) {
        return sumToNok(player.getTransactionArchive().getRealizedGainsByCurrency(), converter);
    }

    /**
     * Returns the total realized loss across all losing sales, converted to NOK.
     * Losses are returned as a positive number for display purposes.
     *
     * @param player    the active player
     * @param converter the currency converter used to translate values to NOK
     * @return total realized loss in NOK as a non-negative value; zero if no losing sales exist
     */
    public BigDecimal getLossesInNok(Player player, CurrencyConverter converter) {
        return sumToNok(player.getTransactionArchive().getRealizedLossesByCurrency(), converter);
    }

    /**
     * Returns the net realized result: gains minus losses, in NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to translate values to NOK
     * @return net realized result in NOK; negative if losses exceed gains
     */
    public BigDecimal getNetRealizedInNok(Player player, CurrencyConverter converter) {
        return getGainsInNok(player, converter).subtract(getLossesInNok(player, converter));
    }

    /**
     * Returns the total tax paid across all sales, converted to NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to translate values to NOK
     * @return total tax paid in NOK; zero if no taxable sales exist
     */
    public BigDecimal getTotalTaxPaidInNok(Player player, CurrencyConverter converter) {
        return sumToNok(player.getTransactionArchive().getTotalSaleTaxByCurrency(), converter);
    }

    /**
     * Returns the total commission paid across all sales, converted to NOK.
     *
     * @param player    the active player
     * @param converter the currency converter used to translate values to NOK
     * @return total commission paid in NOK; zero if no sales have been made
     */
    public BigDecimal getTotalSaleCommissionInNok(Player player, CurrencyConverter converter) {
        return sumToNok(player.getTransactionArchive().getTotalSaleCommissionByCurrency(), converter);
    }

    /**
     * Returns the total number of completed sales.
     *
     * @param player the active player
     * @return number of sales recorded in the transaction archive
     */
    public int getSalesCount(Player player) {
        return player.getTransactionArchive().getSalesCount();
    }

    private BigDecimal sumToNok(Map<Currency, BigDecimal> amounts, CurrencyConverter converter) {
        return amounts.entrySet().stream()
                .map(e -> converter.convert(e.getValue(), e.getKey(), NOK))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
