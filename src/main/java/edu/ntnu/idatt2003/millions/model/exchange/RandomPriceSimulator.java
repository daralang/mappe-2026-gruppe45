package edu.ntnu.idatt2003.millions.model.exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

/**
 * A {@link PriceSimulator} that updates stock prices using a random walk model.
 * Owns the random instance.
 *
 * <p>Each call applies a uniformly distributed random percentage change in the range
 * {@code [-MAX_WEEKLY_CHANGE, +MAX_WEEKLY_CHANGE]} to the current price.
 * The result is rounded to two decimal places and floored
 * at {@code MIN_PRICE} so that no stock price reaches zero.</p>
 */
public class RandomPriceSimulator implements PriceSimulator {

    /** The lowest price a stock can have after a weekly update. */
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    /** The maximum percentage a stock price can change per week (10%). */
    private static final BigDecimal MAX_WEEKLY_CHANGE = new BigDecimal("0.10");

    private final Random random;

    /**
     * Creates a new {@code RandomPriceSimulator} with an unseeded {@link Random}.
     */
    public RandomPriceSimulator() {
        this.random = new Random();
    }

    /**
     * Computes the next price by applying a random percentage change to the current price.
     * The change is uniformly distributed in {@code [-10%, +10%]}.
     * The result is rounded to two decimal places and is never below {@link #MIN_PRICE}.
     *
     * @param currentPrice the stock's current sales price
     * @return the new sales price after the random walk step
     */
    @Override
    public BigDecimal nextPrice(BigDecimal currentPrice) {
        double randomFraction = (random.nextDouble() * 2.0) - 1.0;
        BigDecimal percentChange = MAX_WEEKLY_CHANGE.multiply(BigDecimal.valueOf(randomFraction));

        BigDecimal newPrice = currentPrice
                .multiply(BigDecimal.ONE.add(percentChange))
                .setScale(2, RoundingMode.HALF_UP);

        return newPrice.compareTo(MIN_PRICE) < 0 ? MIN_PRICE : newPrice;
    }
}
