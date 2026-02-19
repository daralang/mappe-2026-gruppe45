package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

public class PurchaseCalculator implements TransactionCalculator {
    private final BigDecimal purchasePrice;
    private final BigDecimal quantity;
    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.005");

    public PurchaseCalculator(Share share) {
        this.purchasePrice = share.getPurchasePrice();
        this.quantity = share.getQuantity();
    }

    @Override
    public BigDecimal calculateGross() {
        return this.purchasePrice.multiply(this.quantity);
    }

    @Override
    public BigDecimal calculateCommission() {
        return this.calculateGross().multiply(COMMISSION_RATE);
    }

    @Override
    public BigDecimal calculateTax() {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateTotal() {
        return calculateGross().add(calculateCommission()).add(calculateTax());
    }
}