package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.view.component.chart.SparklineChart;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Abstract base class for row renderers that display {@link Stock} data in a table.
 *
 * <p>Provides shared helper methods for formatting price ranges and building
 * sparkline charts, eliminating duplication across concrete renderers such as
 * StocksRowRenderer and WatchlistRowRenderer.</p>
 *
 * <p>Subclasses implement their own {@code buildRow} method suited to their
 * table's column structure.</p>
 */
public abstract class RowRenderer {

    /**
     * Builds and returns a {@link SparklineChart} populated with the latest
     * prices from the given stock, up to {@code maxWeeks} data points.
     *
     * @param stock    the stock to read price history from
     * @param maxWeeks maximum number of recent weeks to include
     * @return a configured sparkline chart
     */
    protected SparklineChart buildSparkline(Stock stock, int maxWeeks) {
        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - maxWeeks), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);
        return sparkline;
    }

    /**
     * Formats the lowest and highest prices from the latest {@code weeks}
     * historical price entries as {@code "low / high"}.
     * Returns {@code "-"} when the stock has insufficient price history.
     *
     * @param stock the stock to read prices from
     * @param weeks the number of recent weeks to consider
     * @return a formatted high/low string, or {@code "-"} if data is unavailable
     */
    protected String formatHighLow(Stock stock, int weeks) {
        List<BigDecimal> prices = stock.getRecentPrices(weeks);
        if (prices.isEmpty()) return "-";
        BigDecimal low = prices.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal high = prices.stream().max(BigDecimal::compareTo).orElseThrow();
        return formatWhole(low) + " / " + formatWhole(high);
    }

    /**
     * Formats a {@link BigDecimal} as a whole number string with no decimal places.
     *
     * @param value the value to format
     * @return the formatted string
     */
    protected String formatWhole(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
}
