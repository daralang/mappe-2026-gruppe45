package edu.ntnu.idatt2003.millions;

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
     */
    public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
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
