// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents an immutable historical record of a stock sale.
 *
 * <p>Financial values (gross, commission, tax, total, profit, profitPercent)
 * are captured at construction in the stock's native currency and never change
 * afterward, even if the underlying stock price moves. This guarantees that
 * realized financial values in the transaction archive reflect the price at
 * sale time, not the current price.
 *
 */
public class Sale extends Transaction {

    private static final int PRICE_SCALE = 4;

    private final BigDecimal gross;
    private final BigDecimal commission;
    private final BigDecimal tax;
    private final BigDecimal total;
    private final BigDecimal profit;
    private final BigDecimal profitPercent;

    /**
     * Constructs a new Sale for the specified share and week.
     *
     * @param share the share being sold
     * @param week  the week in which the sale takes place
     */
    public Sale(Share share, int week) {
        super(share, week, new SalesCalculator(share));
        SalesCalculator calc = (SalesCalculator) getCalculator();
        this.gross        = calc.calculateGross();
        this.commission   = calc.calculateCommission();
        this.tax          = calc.calculateTax();
        this.total        = calc.calculateTotal();
        this.profit       = calc.calculateProfit();
        this.profitPercent = calc.calculateProfitPercent();
    }

    @Override
    public BigDecimal getCommissionNative() {
        return commission;
    }

    @Override
    public BigDecimal getTaxNative() {
        return tax;
    }

    @Override
    public BigDecimal getSignedTotalNative() {
        return total;
    }

    @Override
    public BigDecimal getPricePerShare() {
        return gross.divide(getShare().getQuantity(), PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Commits this sale for the given player.
     * Removes the share from the player's portfolio and records the transaction
     * in the archive.
     *
     * @param player the player executing the sale
     * @throws NullPointerException  if the player is null
     * @throws IllegalStateException if the transaction has already been committed
     * @throws IllegalStateException if the share is not in the player's portfolio
     */
    @Override
    public void commit(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        if (isCommitted()) throw new IllegalStateException("Sale is already committed");
        if (!player.getPortfolio().contains(getShare())) throw new IllegalStateException("Share is not in portfolio");

        player.getPortfolio().removeShare(getShare());
        player.getTransactionArchive().add(this);
        markAsCommitted();
    }

    /** Returns the gross sale value (sales price × quantity) in native currency. */
    public BigDecimal getGross() {
        return gross;
    }

    /** Returns the realized profit or loss on this sale (total − original purchase costs) in native currency. */
    public BigDecimal getProfit() {
        return profit;
    }

    /** Returns the realized profit as a percentage of the original purchase costs. */
    public BigDecimal getProfitPercent() {
        return profitPercent;
    }
}
