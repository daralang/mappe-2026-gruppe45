package edu.ntnu.idatt2003.millions;

import java.math.BigDecimal;

public class Share {
    private final Stock stock;
    private final BigDecimal quantity;
    private final BigDecimal purchasePrice;

    public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
        this.stock = stock;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    public Stock getStock() {
        return stock;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
}
