package edu.ntnu.idatt2003.millions.model.calculator;

import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Calculator for the financial components of a stock sale transaction.
 *
 * <p>Calculates gross value, commission, tax, and total net payout based on
 * the sales price, quantity, and purchase costs of a given {@link Share}.</p>
 */
public class SalesCalculator implements TransactionCalculator {
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;
    private final BigDecimal purchaseCosts;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.3");

    /**
     * Constructs a SalesCalculator for the given share.
     * The sales price is fetched directly from the stock and may be in a foreign currency.
     * Use {@link #SalesCalculator(Share, BigDecimal)} when the price has already been
     * converted to NOK.
     *
     * @param share the share being sold
     * @throws NullPointerException if the share is null
     */
    public SalesCalculator(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();
        this.purchaseCosts = new PurchaseCalculator(share).calculateTotal();
    }

    /**
     * Constructs a SalesCalculator for the given share with a pre-converted sales price.
     * Use this constructor when selling via
     * {@link edu.ntnu.idatt2003.millions.model.exchange.Exchange},
     * which converts the stock price to NOK before creating the transaction.
     *
     * @param share        the share being sold
     * @param salesPrice   the current sales price already converted to NOK
     * @throws NullPointerException if the share or salesPrice is null
     */
    public SalesCalculator(Share share, BigDecimal salesPrice) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(salesPrice, "Sales price cannot be null");
        this.salesPrice = salesPrice;
        this.quantity = share.getQuantity();
        this.purchaseCosts = new PurchaseCalculator(share).calculateTotal();
    }

    /**
     * Calculates the gross value of the sale.
     * Gross value is defined as sales price multiplied by quantity.
     *
     * @return the gross value as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateGross() {
        return this.salesPrice.multiply(this.quantity);
    }

    /**
     * Calculates the commission fee for the sale.
     * Commission is 1% of the gross value.
     *
     * @return the commission amount as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateCommission() {
        return this.calculateGross().multiply(COMMISSION_RATE);
    }

    /**
     * Calculates the tax for the sale.
     * Tax is 30% of the profit, where profit is gross value minus commission minus purchase costs.
     * If the profit is zero or negative (i.e. a loss), zero tax is returned.
     *
     * @return the tax amount as a {@link BigDecimal}, or {@link BigDecimal#ZERO} if there is no profit
     */
    @Override
    public BigDecimal calculateTax() {
        BigDecimal profit = this.calculateGross().subtract(this.calculateCommission()).subtract(this.purchaseCosts);
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(TAX_RATE);
    }

    /**
     * Calculates the total net payout of the sale.
     * Total is defined as gross value minus commission minus tax.
     *
     * @return the total net payout as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateTotal() {
        return this.calculateGross().subtract(this.calculateCommission()).subtract(this.calculateTax());
    }
}