package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

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

    /** Commission rate applied to the gross value of the sale (1%). */
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");

    /** Tax rate applied to the profit of the sale (30%). */
    private static final BigDecimal TAX_RATE = new BigDecimal("0.3");

    /**
     * Constructs a SalesCalculator for the given share.
     *
     * @param share the share being sold, used to retrieve sales price, quantity, and purchase costs
     */
    public SalesCalculator(Share share) {
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();

        PurchaseCalculator purchaseCalculator = new PurchaseCalculator(share);
        this.purchaseCosts = purchaseCalculator.calculateTotal();
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
