package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Stateless read service computing derived price-history values for a {@link Stock}.
 *
 * <p>Holds no state and reads freely from the domain without mutating it, in line with
 * the application's read-service layer ({@link PlayerStatsService}, {@link PortfolioService}).
 * Views call it directly and render the returned rows: all numeric derivation lives here
 * rather than in the view, keeping presentation free of business logic.</p>
 */
public final class StockHistoryService {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final int DEFAULT_MAX_ROWS = 4;
    private static final int PERCENT_SCALE = 2;

    /**
     * A single week-over-week price change, from week {@code week - 1} to {@code week}.
     *
     * @param week          the week the change leads into (1-based, matching the price index)
     * @param nativeChange  the price change in the stock's own currency
     * @param nokChange     the price change converted to NOK
     * @param percentChange the change as a percentage of the previous price
     *                      (rounded {@link RoundingMode#HALF_UP} to two decimals)
     */
    public record WeeklyPriceChange(int week, BigDecimal nativeChange,
                                    BigDecimal nokChange, BigDecimal percentChange) {
    }

    /**
     * Returns the most recent weekly price changes, newest first, capped at four rows.
     *
     * @param stock     the stock whose price history is read; must not be {@code null}
     * @param converter the converter used for the NOK column; must not be {@code null}
     * @return up to four {@link WeeklyPriceChange} rows, newest first, or an empty list
     *         when fewer than two prices exist (no change has occurred yet)
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if the stock's currency cannot be converted to NOK
     */
    public List<WeeklyPriceChange> getRecentWeeklyChanges(Stock stock, CurrencyConverter converter) {
        return getRecentWeeklyChanges(stock, converter, DEFAULT_MAX_ROWS);
    }

    /**
     * Returns the most recent weekly price changes, newest first, capped at {@code maxRows}.
     *
     * @param stock     the stock whose price history is read; must not be {@code null}
     * @param converter the converter used for the NOK column; must not be {@code null}
     * @param maxRows   the maximum number of rows to return; must be greater than zero
     * @return up to {@code maxRows} {@link WeeklyPriceChange} rows, newest first
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if {@code maxRows} is not greater than zero, or if the
     *                                  stock's currency cannot be converted to NOK
     */
    public List<WeeklyPriceChange> getRecentWeeklyChanges(Stock stock, CurrencyConverter converter, int maxRows) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");
        if (maxRows <= 0) {
            throw new IllegalArgumentException("maxRows must be greater than zero");
        }
        int priceCount = stock.getHistoricalPrices().size();
        int transitions = priceCount - 1;
        if (transitions <= 0) {
            return List.of();
        }
        int rowCount = Math.min(maxRows, transitions);
        return getWeeklyChanges(stock, converter, priceCount - rowCount + 1, priceCount);
    }

    /**
     * Returns the weekly price changes for the transition weeks within the inclusive
     * range {@code [fromWeek, toWeek]}, newest first.
     *
     * @param stock     the stock whose price history is read; must not be {@code null}
     * @param converter the converter used for the NOK column; must not be {@code null}
     * @param fromWeek  the first week of the range (inclusive)
     * @param toWeek    the last week of the range (inclusive)
     * @return the matching {@link WeeklyPriceChange} rows, newest first (possibly empty)
     * @throws NullPointerException     if {@code stock} or {@code converter} is {@code null}
     * @throws IllegalArgumentException if {@code toWeek < fromWeek}, or if the stock's
     *                                  currency cannot be converted to NOK
     */
    public List<WeeklyPriceChange> getWeeklyChanges(Stock stock, CurrencyConverter converter,
                                                    int fromWeek, int toWeek) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");
        if (toWeek < fromWeek) {
            throw new IllegalArgumentException("toWeek must be greater than or equal to fromWeek");
        }
        List<BigDecimal> prices = stock.getHistoricalPrices();
        int upper = Math.min(toWeek, prices.size());
        int lower = Math.max(fromWeek, 2);
        List<WeeklyPriceChange> rows = new ArrayList<>();
        for (int week = upper; week >= lower; week--) {
            rows.add(weeklyChange(prices, week, stock.getCurrency(), converter));
        }
        return rows;
    }

    /**
     * Builds the {@link WeeklyPriceChange} for the transition into {@code week}
     * (from {@code week - 1} to {@code week}).
     *
     * @param prices    the full price history, oldest first
     * @param week      the week the change leads into ({@code week - 2} must be a valid index)
     * @param currency  the stock's own currency
     * @param converter the converter used for the NOK column
     * @return the computed weekly price change
     */
    private WeeklyPriceChange weeklyChange(List<BigDecimal> prices, int week,
                                           Currency currency, CurrencyConverter converter) {
        BigDecimal current = prices.get(week - 1);
        BigDecimal previous = prices.get(week - 2);
        BigDecimal nativeChange = current.subtract(previous);
        BigDecimal nokChange = converter.convert(nativeChange, currency, NOK);
        BigDecimal percentChange = previous.signum() == 0
                ? BigDecimal.ZERO
                : nativeChange.multiply(BigDecimal.valueOf(100))
                        .divide(previous, PERCENT_SCALE, RoundingMode.HALF_UP);
        return new WeeklyPriceChange(week, nativeChange, nokChange, percentChange);
    }
}
