package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Calculator for the financial components of a stock purchase transaction.
 *
 * <p>Calculates gross value, commission, tax, and total cost based on
 * the purchase price and quantity of a given {@link Share}.</p>
 */
public class PurchaseCalculator implements TransactionCalculator {
    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;

    /** Commission rate applied to the gross value of the purchase (0.5%). */
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

    /**
     * Constructs a PurchaseCalculator for the given share.
     *
     * @param share the share being purchased, used to retrieve purchase price and quantity
     */
    public PurchaseCalculator(Share share) {
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
    }

    /**
     * Calculates the gross value of the purchase.
     * Gross value is defined as purchase price multiplied by quantity.
     *
     * @return the gross value as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateGross() {
        return this.purchasePrice.multiply(this.quantity);
    }

    /**
     * Calculates the commission fee for the purchase.
     * Commission is 0.5% of the gross value.
     *
     * @return the commission amount as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateCommission() {
        return this.calculateGross().multiply(COMMISSION_RATE);
    }

    /**
     * Returns the tax for a purchase transaction.
     * No tax is applied on purchases, so this always returns zero.
     *
     * @return {@link BigDecimal#ZERO}
     */
    @Override
    public BigDecimal calculateTax() {
        return BigDecimal.ZERO;
    }

    /**
     * Calculates the total cost of the purchase.
     * Total is defined as gross value plus commission plus tax.
     *
     * @return the total cost as a {@link BigDecimal}
     */
    @Override
    public BigDecimal calculateTotal() {
        return calculateGross().add(calculateCommission()).add(calculateTax());
    }
}