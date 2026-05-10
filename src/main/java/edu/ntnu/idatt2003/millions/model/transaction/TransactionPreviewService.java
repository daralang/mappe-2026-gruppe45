package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain Service that produces transaction previews without mutating state.
 *
 * <p>Coordinates calculations across {@link Stock}, {@link Share},
 * {@link Player} and the calculator classes to answer questions like
 * "what would this purchase cost?" or "what would I receive from this
 * sale?". Used by controllers to feed dialogs and other view components.</p>
 */
public class TransactionPreviewService {

    /**
     * Computes a preview of buying the given quantity of a stock for
     * the given player, using the stock's current sales price.
     *
     * @param stock    the stock to buy
     * @param quantity the quantity to buy
     * @param player   the player who would perform the purchase
     * @return a preview with gross, commission, total, and resulting balance
     */
    public TransactionPreview previewPurchase(
            Stock stock, BigDecimal quantity, Player player) {

        Share hypothetical = new Share(stock, quantity, stock.getSalesPrice());
        PurchaseCalculator calc = new PurchaseCalculator(hypothetical);

        BigDecimal gross = calc.calculateGross();
        BigDecimal commission = calc.calculateCommission();
        BigDecimal tax = calc.calculateTax();
        BigDecimal total = calc.calculateTotal();
        BigDecimal balanceAfter = player.getMoney().subtract(total);

        return new TransactionPreview(
                gross, commission, tax, total, balanceAfter, null, null);
    }

    /**
     * Computes a preview of selling the given quantity of an owned share
     * for the given player, using the stock's current sales price.
     *
     * <p>All financial calculations (gross, commission, tax, total, profit
     * and profit percent) are delegated to {@link SalesCalculator}. The
     * resulting balance is the player's current cash plus the net payout.</p>
     *
     * @param share    the share to sell from
     * @param quantity the quantity to sell (may be less than the full share)
     * @param player   the player who would perform the sale
     * @return a preview with gross, commission, tax, total received,
     *         resulting balance, and the realized profit or loss
     */
    public TransactionPreview previewSale(
            Share share, BigDecimal quantity, Player player) {

        Share partial = quantity.compareTo(share.getQuantity()) == 0
                ? share
                : new Share(share.getStock(), quantity, share.getPurchasePrice());
        SalesCalculator calc = new SalesCalculator(partial);

        BigDecimal gross = calc.calculateGross();
        BigDecimal commission = calc.calculateCommission();
        BigDecimal tax = calc.calculateTax();
        BigDecimal total = calc.calculateTotal();
        BigDecimal balanceAfter = player.getMoney().add(total);

        BigDecimal profit = calc.calculateProfit();
        BigDecimal profitPercent = calc.calculateProfitPercent();

        return new TransactionPreview(
                gross, commission, tax, total, balanceAfter,
                profit, profitPercent);
    }
}