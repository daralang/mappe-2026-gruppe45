// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.stock;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a player's ownership of a specific quantity of a stock.
 * A share records the stock owned, the quantity held, and the purchase price paid.
 * When the same stock is bought more than once, the positions are consolidated by
 * {@link edu.ntnu.idatt2003.millions.model.player.Portfolio} into a single Share
 * whose {@code purchasePrice} is the weighted-average of all individual purchase
 * prices (GAV = total cost / total quantity).
 * Shares are held in a player's portfolio and can be sold on an exchange.
 */
// Not a record: Stock is mutable (prices grow over time), so Share cannot carry value-type semantics.
// Portfolio also relies on reference equality for contains() / remove().
@SuppressWarnings("ClassCanBeRecord")
public class Share {
    private final Stock stock;
    private final BigDecimal quantity;
    private final BigDecimal purchasePrice;

    /**
     * Constructs a new Share with the specified stock, quantity, and purchase price.
     *
     * @param stock         the stock that was purchased
     * @param quantity      the number of shares purchased
     * @param purchasePrice the price per share at time of purchase
     * @throws NullPointerException     if the stock, quantity, or purchase price is null
     * @throws IllegalArgumentException if the quantity is negative
     * @throws IllegalArgumentException if the purchase price is negative
     */
    public Share(Stock stock, BigDecimal quantity, BigDecimal purchasePrice) {
        Objects.requireNonNull(stock, "Stock cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(purchasePrice, "Purchase price cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (purchasePrice.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Purchase price cannot be negative");

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

    /**
     * Returns the weighted-average purchase price (GAV) per share.
     * For a position consolidated from multiple purchases this is the
     * weighted-average of all individual purchase prices; for a single
     * purchase it equals the original price paid.
     *
     * @return the weighted-average price paid per share (GAV)
     */
    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    /**
     * Returns the total amount paid to acquire this position (purchasePrice × quantity).
     *
     * @return the total cost basis
     */
    public BigDecimal getCost() {
        return purchasePrice.multiply(quantity);
    }

    /**
     * Returns the current market value of this position (salesPrice × quantity).
     *
     * @return the current value at the stock's latest price
     */
    public BigDecimal getCurrentValue() {
        return stock.getSalesPrice().multiply(quantity);
    }

    /**
     * Returns the absolute return for this position (currentValue − cost)
     * in the stock's native currency.
     *
     * @return profit or loss in the stock's native currency
     */
    public BigDecimal getReturnNative() {
        return getCurrentValue().subtract(getCost());
    }

    /**
     * Returns the return for this position as a percentage of cost.
     * Returns zero if the cost basis is zero.
     *
     * @return return as a percentage, e.g. 12.50 means +12.50%
     */
    public BigDecimal getReturnPercent() {
        BigDecimal cost = getCost();
        if (cost.signum() == 0) return BigDecimal.ZERO;
        return getReturnNative().multiply(BigDecimal.valueOf(100))
                .divide(cost, 2, RoundingMode.HALF_UP);
    }

    /**
     * Returns a new Share that consolidates this position with {@code other} using
     * a weighted-average purchase price (GAV = total cost / total quantity).
     * The returned Share uses this position's stock reference.
     *
     * @param other the position to merge into this one
     * @return a new Share with summed quantity and weighted-average purchase price
     * @throws NullPointerException     if other is null
     * @throws IllegalArgumentException if other belongs to a different stock symbol
     */
    public Share mergedWith(Share other) {
        Objects.requireNonNull(other, "Other share cannot be null");
        if (!stock.getSymbol().equals(other.stock.getSymbol())) {
            throw new IllegalArgumentException(
                    "Cannot merge shares of different stocks: " + stock.getSymbol()
                    + " vs " + other.stock.getSymbol());
        }
        BigDecimal newQuantity = quantity.add(other.quantity);
        BigDecimal newCost = getCost().add(other.getCost());
        BigDecimal newGav = newCost.divide(newQuantity, 4, RoundingMode.HALF_UP);
        return new Share(stock, newQuantity, newGav);
    }

    /**
     * Returns the net amount the player would receive if the entire position
     * were sold at the current price, after commission and tax.
     *
     * @return liquidation value in the stock's native currency
     */
    public BigDecimal getLiquidationValue() {
        return new SalesCalculator(this).calculateTotal();
    }
}