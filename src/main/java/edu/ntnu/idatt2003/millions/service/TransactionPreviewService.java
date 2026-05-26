// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.transaction.PurchaseCalculator;
import edu.ntnu.idatt2003.millions.model.transaction.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Produces {@link TransactionPreview} snapshots for a prospective purchase or sale
 * without mutating any model state.
 */
public class TransactionPreviewService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Returns a preview of buying the given quantity of a stock for the given player.
     * Gross, commission, and total are in the stock's native currency;
     * {@code balanceAfter} is in NOK.
     *
     * @param stock     the stock to buy
     * @param quantity  the quantity to buy
     * @param player    the player who would perform the purchase
     * @param converter used to convert the native-currency total to NOK
     * @return a preview with gross/commission/total in native currency and balanceAfter in NOK
     * @throws NullPointerException if any argument is null
     */
    public TransactionPreview previewPurchase(
            Stock stock, BigDecimal quantity, Player player, CurrencyConverter converter) {
        Objects.requireNonNull(stock, "Stock cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        Share hypothetical = new Share(stock, quantity, stock.getSalesPrice());
        PurchaseCalculator calc = new PurchaseCalculator(hypothetical);

        BigDecimal gross = calc.calculateGross();
        BigDecimal commission = calc.calculateCommission();
        BigDecimal tax = calc.calculateTax();
        BigDecimal total = calc.calculateTotal();
        BigDecimal totalInNok = converter.convert(total, stock.getCurrency(), NOK);
        BigDecimal balanceAfter = player.getCash().subtract(totalInNok);

        return new TransactionPreview(
                gross, commission, tax, total, totalInNok, balanceAfter, null, null, null);
    }

    /**
     * Returns a preview of selling the given quantity of an owned share for the given player.
     * Gross, commission, tax, total, and profit are in the stock's native currency;
     * {@code balanceAfter} is in NOK.
     *
     * @param share     the share to sell from
     * @param quantity  the quantity to sell; may be less than the full position
     * @param player    the player who would perform the sale
     * @param converter used to convert the native-currency payout to NOK
     * @return a preview with gross/commission/tax/total/profit in native currency
     *         and balanceAfter in NOK
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if {@code quantity} exceeds the position held in {@code share}
     */
    public TransactionPreview previewSale(
            Share share, BigDecimal quantity, Player player, CurrencyConverter converter) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(player, "Player cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");

        if (quantity.compareTo(share.getQuantity()) > 0) {
            throw new IllegalArgumentException(
                    "Cannot preview selling " + quantity + " shares — position only holds " + share.getQuantity());
        }
        Share partial = quantity.compareTo(share.getQuantity()) == 0
                ? share
                : new Share(share.getStock(), quantity, share.getPurchasePrice());
        SalesCalculator calc = new SalesCalculator(partial);

        BigDecimal gross = calc.calculateGross();
        BigDecimal commission = calc.calculateCommission();
        BigDecimal tax = calc.calculateTax();
        BigDecimal total = calc.calculateTotal();
        BigDecimal totalInNok = converter.convert(total, share.getStock().getCurrency(), NOK);
        BigDecimal balanceAfter = player.getCash().add(totalInNok);

        BigDecimal profit = calc.calculateProfit();
        BigDecimal profitPercent = calc.calculateProfitPercent();
        Currency currency = share.getStock().getCurrency();
        BigDecimal profitInNok = currency.equals(NOK)
                ? null
                : converter.convert(profit, currency, NOK);

        return new TransactionPreview(
                gross, commission, tax, total, totalInNok, balanceAfter,
                profit, profitPercent, profitInNok);
    }
}