package edu.ntnu.idatt2003.millions.view.ingame.dashboard.watchlist.card;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockStatsService;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.chart.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.dashboard.watchlist.dialog.WatchlistNoteDialog;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

/**
 * A styled info card displaying key price data for a single stock.
 *
 * <p>Renders a horizontal meta row inside a {@code modal-summary}-styled container with:
 * <ul>
 *   <li>Current price in the stock's native currency</li>
 *   <li>Current price converted to NOK - omitted when the stock is already priced in NOK</li>
 *   <li>Weekly price change as a signed, colour-coded percentage</li>
 *   <li>A {@link SparklineChart} showing the recent price trend</li>
 * </ul>
 *
 * <p>Intended for use inside {@link WatchlistNoteDialog}.
 */
public class StockInfoCard extends VBox {

    private static final int SPARKLINE_WEEKS = 8;
    private static final Currency NOK = Currency.getInstance("NOK");
    private final StockStatsService statsService = new StockStatsService();

    /**
     * Constructs a new {@link StockInfoCard} for the given stock.
     *
     * <p>When the stock's currency is NOK the NOK price cell is omitted from the
     * meta row because it would duplicate the native price cell.</p>
     *
     * @param stock     the stock whose price data is displayed
     * @param converter the currency converter used to derive the NOK price
     */
    public StockInfoCard(Stock stock, CurrencyConverter converter) {
        setSpacing(12);

        boolean isNok = stock.getCurrency().equals(NOK);
        String currencyCode = stock.getCurrency().getCurrencyCode();
        BigDecimal price = stock.getSalesPrice();
        BigDecimal priceNok = isNok ? null : statsService.priceInNok(stock, converter);
        BigDecimal changePercent = stock.getWeeklyChangePercent();

        getChildren().addAll(
                buildStockSection(stock),
                buildMetaRow(stock, currencyCode, price, priceNok, changePercent, isNok)
        );
    }

    /**
     * Builds the stock header: "Aksje" label above "TICKER, Company name".
     *
     * @param stock the stock to display
     * @return a {@link VBox} with the label and company name
     */
    private VBox buildStockSection(Stock stock) {
        StyledText label = StyledText.detailLabel(LanguageManager.get("receipt.stock.label"));
        Label value = new Label(stock.getSymbol() + ", " + stock.getCompany());
        value.getStyleClass().add("modal-section-value");
        return new VBox(4, label, value);
    }

    /**
     * Builds the horizontal row of meta cells.
     *
     * <p>For foreign-currency stocks the row contains four cells:
     * native price, NOK price, weekly change percentage, and sparkline trend.
     * When the stock is already priced in NOK the NOK price cell is omitted,
     * leaving three cells: NOK price, weekly change percentage, and sparkline.</p>
     *
     * @param stock         the stock for sparkline data
     * @param currencyCode  the stock's currency code
     * @param price         the current price in native currency
     * @param priceNok      the current price converted to NOK; {@code null} when {@code isNok} is true
     * @param changePercent the weekly change percentage
     * @param isNok         {@code true} when the stock's currency is NOK
     * @return an {@link HBox} containing three or four vertical cells
     */
    private HBox buildMetaRow(Stock stock, String currencyCode,
                              BigDecimal price, BigDecimal priceNok,
                              BigDecimal changePercent, boolean isNok) {
        VBox changeCell = buildChangeCell(changePercent);
        VBox trendCell = buildTrendCell(stock);

        HBox row;
        if (isNok) {
            VBox priceCell = buildTextCell(
                    LanguageManager.get("col.priceNok"),
                    ChangeFormatter.formatPlain(price) + " NOK");
            row = new HBox(24, priceCell, changeCell, trendCell);
        } else {
            VBox priceCell = buildTextCell(
                    LanguageManager.get("col.priceNative"),
                    ChangeFormatter.formatPlain(price) + " " + currencyCode);
            VBox priceNokCell = buildTextCell(
                    LanguageManager.get("col.priceNok"),
                    ChangeFormatter.formatPlain(priceNok) + " NOK");
            row = new HBox(24, priceCell, priceNokCell, changeCell, trendCell);
        }

        row.setAlignment(Pos.BOTTOM_LEFT);
        return row;
    }

    /**
     * Builds a plain vertical cell with a label on top and a value below.
     *
     * @param labelText the cell label
     * @param valueText the cell value
     * @return a {@link VBox} with label and value
     */
    private VBox buildTextCell(String labelText, String valueText) {
        StyledText label = StyledText.detailLabel(labelText);
        Label value = new Label(valueText);
        value.getStyleClass().add("modal-section-value");
        return new VBox(4, label, value);
    }

    /**
     * Builds a vertical cell for the weekly change percentage, colour-coded
     * green for positive and red for negative values.
     *
     * @param changePercent the weekly change percentage
     * @return a {@link VBox} with label and coloured value
     */
    private VBox buildChangeCell(BigDecimal changePercent) {
        StyledText label = StyledText.detailLabel(LanguageManager.get("details.row.weeklyChange"));
        Label value = ChangeFormatter.styledPercent(changePercent, "modal-section-value");
        return new VBox(4, label, value);
    }

    /**
     * Builds a vertical cell containing a {@link SparklineChart} below the trend label.
     *
     * @param stock the stock whose price history is plotted
     * @return a {@link VBox} with label and sparkline
     */
    private VBox buildTrendCell(Stock stock) {
        StyledText label = StyledText.detailLabel(LanguageManager.get("watchlist.note.info.trend"));

        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - SPARKLINE_WEEKS), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);

        return new VBox(4, label, sparkline);
    }
}
