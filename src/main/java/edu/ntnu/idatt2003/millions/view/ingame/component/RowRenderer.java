package edu.ntnu.idatt2003.millions.view.ingame.component;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.ingame.component.chart.SparklineChart;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javafx.scene.control.Label;

/**
 * Abstract base class for row renderers that display {@link Stock} data in a table.
 *
 * <p>Provides shared helper methods for building common table cells (NOK price,
 * weekly change in NOK and percent) and for formatting price ranges and building
 * sparkline charts.</p>
 *
 * <p>Subclasses implement their own {@code buildRow} method suited to their
 * table's column structure.</p>
 */
public abstract class RowRenderer {

    /**
     * Shared stateless service for computing NOK-converted stock figures.
     * Accessible to all subclasses without repeated instantiation.
     */
    protected final StockStatsService statsService = new StockStatsService();

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
     * Builds a plain {@link Label} showing the stock's latest price converted to NOK,
     * formatted without sign.
     *
     * @param stock     the stock to read from
     * @param converter the converter used for the NOK conversion
     * @return a table-cell label with the formatted NOK price
     */
    protected Label priceNokLabel(Stock stock, CurrencyConverter converter) {
        return TableCells.data(ChangeFormatter.formatPlain(statsService.priceInNok(stock, converter)));
    }

    /**
     * Builds a colour-coded {@link Label} showing the stock's latest week-over-week
     * price change in NOK.
     *
     * @param stock     the stock to read from
     * @param converter the converter used for the NOK conversion
     * @return a styled amount label with {@code "table-cell"} style class
     */
    protected Label changeNokLabel(Stock stock, CurrencyConverter converter) {
        return ChangeFormatter.styledAmount(statsService.changeInNok(stock, converter), "table-cell");
    }

    /**
     * Builds a colour-coded {@link Label} showing the stock's latest weekly change
     * as a percentage.
     *
     * @param stock the stock to read from
     * @return a styled percent label with {@code "table-cell"} style class
     */
    protected Label changePctLabel(Stock stock) {
        return ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "table-cell");
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
