package edu.ntnu.idatt2003.millions.model.exchange;

import java.math.BigDecimal;

/**
 * Strategy interface for computing the next sales price of a stock.
 *
 * <p>Implementations define a pricing model (for example a random walk or a
 * trend-based model, without coupling the model to {@link Exchange}).
 * The interface is FunctionalInterface-annotated, so implementations
 * may be supplied as lambdas where deterministic behaviour is needed (e.g. in tests)</p>
 *
 */
@FunctionalInterface
public interface PriceSimulator {

    /**
     * Computes the next sales price for a stock given its current price.
     *
     * @param currentPrice the stock's current sales price; never {@code null}
     * @return the new sales price to apply; must not be {@code null}
     */
    BigDecimal nextPrice(BigDecimal currentPrice);
}
