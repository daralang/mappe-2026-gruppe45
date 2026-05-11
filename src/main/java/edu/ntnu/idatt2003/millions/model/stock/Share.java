package edu.ntnu.idatt2003.millions.model.stock;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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
     * Returns the net amount the player would receive if the entire position
     * were sold at the current price, after commission and tax.
     * Delegates to {@link SalesCalculator} so the business rules for fees
     * remain in the domain model.
     *
     * @return liquidation value in the stock's native currency
     */
    public BigDecimal getLiquidationValue() {
        return new SalesCalculator(this).calculateTotal();
    }
}