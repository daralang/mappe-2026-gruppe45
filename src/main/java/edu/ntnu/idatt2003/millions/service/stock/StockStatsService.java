// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service.stock;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Stateless read service computing a stock's current key figures expressed in NOK.
 *
 * <p>Complements {@link StockHistoryService}, which derives time-series values (weekly change
 * rows) across the price history; this service returns single scalar figures instead.</p>
 */
public final class StockStatsService {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final int PERCENT_SCALE = 2;

    /**
     * Returns the stock's latest price converted to NOK.
     *
     * @param stock     the stock to read from; must not be {@code null}
     * @param converter the converter used for the conversion; must not be {@code null}
     * @return the latest price in NOK
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if the stock's currency cannot be converted to NOK
     */
    public BigDecimal priceInNok(Stock stock, CurrencyConverter converter) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");
        return converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
    }

    /**
     * Returns the stock's latest week-over-week price change converted to NOK.
     *
     * @param stock     the stock to read from; must not be {@code null}
     * @param converter the converter used for the conversion; must not be {@code null}
     * @return the latest price change in NOK (zero when only one price exists)
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if the stock's currency cannot be converted to NOK
     */
    public BigDecimal changeInNok(Stock stock, CurrencyConverter converter) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");
        return converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
    }

    /**
     * Returns the all-time percentage change from the stock's first recorded price to its
     * current sales price.
     *
     * @param stock the stock whose price history is read; must not be {@code null}
     * @return the all-time change as a percentage, e.g. {@code 12.34} for +12.34 %
     * @throws NullPointerException if {@code stock} is {@code null}
     */
    public BigDecimal allTimeChangePercent(Stock stock) {
        Objects.requireNonNull(stock, "stock must not be null");
        List<BigDecimal> prices = stock.getHistoricalPrices();
        if (prices.size() < 2) {
            return BigDecimal.ZERO;
        }
        BigDecimal first = prices.get(0);
        if (first.signum() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal current = stock.getSalesPrice();
        return current.subtract(first)
                .multiply(BigDecimal.valueOf(100))
                .divide(first, PERCENT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Returns the high-low price range over the most recent {@code weeks} prices, in NOK.
     *
     * @param stock     the stock to read from; must not be {@code null}
     * @param converter the converter used for the conversion; must not be {@code null}
     * @param weeks     the number of recent weeks to consider; must be greater than zero
     * @return the high-low range in NOK
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if {@code weeks} is not greater than zero, or if the
     *                                  stock's currency cannot be converted to NOK
     */
    public BigDecimal highLowRangeInNok(Stock stock, CurrencyConverter converter, int weeks) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");
        BigDecimal range = stock.getRecentHigh(weeks).subtract(stock.getRecentLow(weeks));
        return converter.convert(range, stock.getCurrency(), NOK);
    }
}
