package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
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
 * <p>Note: {@link #getShare()}{@code .getStock().getSalesPrice()} still returns
 * the current stock price, not the sale-time price. The frozen fields on Sale
 * cover all current uses. If a future feature requires the sale-time stock price
 * as a separate piece of data, add a {@code salesPriceAtCommit} field at that point.
 */
public class Sale extends Transaction {

    private final BigDecimal gross;
    private final BigDecimal commission;
    private final BigDecimal tax;
    private final BigDecimal total;
    private final BigDecimal profit;
    private final BigDecimal profitPercent;

    /**
     * Constructs a new Sale for the specified share and week.
     * All financial values are captured immediately from the current stock price
     * and remain fixed for the lifetime of this object.
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

    /**
     * Commits this sale for the given player.
     * Removes the share from the player's portfolio and records the transaction
     * in the archive. Financial values are already frozen from construction.
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
        if (!player.getPortfolio().contains(getShare())) throw new IllegalStateException("Sale is not in portfolio");

        player.getPortfolio().removeShare(getShare());
        player.getTransactionArchive().add(this);
        markAsCommitted();
    }

    /** Returns the gross sale value (sales price × quantity) in native currency. */
    public BigDecimal getGross() {
        return gross;
    }

    /** Returns the commission paid on this sale (1% of gross) in native currency. */
    public BigDecimal getCommission() {
        return commission;
    }

    /** Returns the tax paid on this sale (30% of profit, or zero if a loss) in native currency. */
    public BigDecimal getTax() {
        return tax;
    }

    /** Returns the net payout of this sale (gross − commission − tax) in native currency. */
    public BigDecimal getTotal() {
        return total;
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
