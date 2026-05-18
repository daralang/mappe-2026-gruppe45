package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

/**
 * Domain Service that produces transaction previews without mutating state.
 *
 * <p>Coordinates calculations across {@link Stock}, {@link Share},
 * {@link Player} and the calculator classes to answer questions like
 * "what would this purchase cost?" or "what would I receive from this
 * sale?". Used by controllers to feed dialogs and other view components.</p>
 */
public class TransactionPreviewService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Computes a preview of buying the given quantity of a stock for the given
     * player. Gross, commission, and total are in the stock's native currency.
     * {@code balanceAfter} is in NOK — the converter is used to translate the
     * total cost before subtracting from the player's NOK balance.
     *
     * @param stock     the stock to buy
     * @param quantity  the quantity to buy
     * @param player    the player who would perform the purchase
     * @param converter used to convert the native-currency total to NOK
     * @return a preview with gross/commission/total in native currency and balanceAfter in NOK
     */
    public TransactionPreview previewPurchase(
            Stock stock, BigDecimal quantity, Player player, CurrencyConverter converter) {

        Share hypothetical = new Share(stock, quantity, stock.getSalesPrice());
        PurchaseCalculator calc = new PurchaseCalculator(hypothetical);

        BigDecimal gross = calc.calculateGross();
        BigDecimal commission = calc.calculateCommission();
        BigDecimal tax = calc.calculateTax();
        BigDecimal total = calc.calculateTotal();
        BigDecimal totalInNok = converter.convert(total, stock.getCurrency(), NOK);
        BigDecimal balanceAfter = player.getMoney().subtract(totalInNok);

        return new TransactionPreview(
                gross, commission, tax, total, totalInNok, balanceAfter, null, null, null);
    }

    /**
     * Computes a preview of selling the given quantity of an owned share for the
     * given player. Gross, commission, tax, total, and profit are in the stock's
     * native currency. {@code balanceAfter} is in NOK — the converter is used to
     * translate the net payout before adding it to the player's NOK balance.
     *
     * @param share     the share to sell from
     * @param quantity  the quantity to sell (may be less than the full share)
     * @param player    the player who would perform the sale
     * @param converter used to convert the native-currency payout to NOK
     * @return a preview with gross/commission/tax/total/profit in native currency
     *         and balanceAfter in NOK
     */
    public TransactionPreview previewSale(
            Share share, BigDecimal quantity, Player player, CurrencyConverter converter) {

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
        BigDecimal balanceAfter = player.getMoney().add(totalInNok);

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