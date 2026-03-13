package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Represents a player's ownership of a specific quantity of a stock.
 * A share records the stock owned, the quantity held, and the purchase price paid.
 * Shares are held in a player's portfolio and can be sold on an exchange.
 */
public class Share {
    private final Stock stock;
    private final BigDecimal quantity;
    private final BigDecimal purchasePrice;

    /**
     * Constructs a new Share with the specified stock, quantity, and purchase price.
     *
     * @param stock the stock that was purchased
     * @param quantity the number of shares purchased
     * @param purchasePrice the price per share at time of purchase
     * @throws IllegalArgumentException if the stock is null
     * @throws IllegalArgumentException if the quantity is null
     * @throws IllegalArgumentException if the quantity is negative
     * @throws IllegalArgumentException if the purchase price is null
     * @throws IllegalArgumentException if the purchase price is negative
     */
    public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
        if (stock == null) throw new IllegalArgumentException("Stock cannot be null");
        if (quantity == null) throw new IllegalArgumentException("Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (purchasePrice == null) throw new IllegalArgumentException("Purchase Price cannot be null");
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Purchase price cannot be negative");

        this.stock = stock;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
    }

    /**
     * Gets the stock associated with this share.
     *
     * @return the stock
     */
    public Stock getStock() {
        return stock;
    }

    /**
     * Gets the quantity of shares held.
     *
     * @return the number of shares
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Gets the purchase price per share.
     *
     * @return the price paid per share
     */
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
}
