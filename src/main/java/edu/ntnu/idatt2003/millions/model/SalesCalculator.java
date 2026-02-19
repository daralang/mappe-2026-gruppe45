package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

public class SalesCalculator implements TransactionCalculator {
    private final BigDecimal salesPrice;
    private final BigDecimal quantity;
    private final BigDecimal purchaseCosts;

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.01");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.3");

    public SalesCalculator(Share share) {
        this.salesPrice = share.getStock().getSalesPrice();
        this.quantity = share.getQuantity();

        PurchaseCalculator purchaseCalculator = new PurchaseCalculator(share);
        this.purchaseCosts = purchaseCalculator.calculateTotal();
    }

    @Override
    public BigDecimal calculateGross() {
        return this.salesPrice.multiply(this.quantity);
    }

    @Override
    public BigDecimal calculateCommission() {
        return this.calculateGross().multiply(COMMISSION_RATE);
    }

    @Override
    public BigDecimal calculateTax() {
        BigDecimal profit = this.calculateGross().subtract(this.calculateCommission()).subtract(this.purchaseCosts);
        if (profit.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return profit.multiply(TAX_RATE);
    }

    @Override
    public BigDecimal calculateTotal() {
        return this.calculateGross().subtract(this.calculateCommission()).subtract(this.calculateTax());
    }
}
