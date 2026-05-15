package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;

/**
 * A styled info card displaying key price data for a single stock.
 *
 * <p>Renders four rows inside a {@code modal-summary}-styled container:
 * <ul>
 *   <li>Current price in the stock's native currency</li>
 *   <li>Current price converted to NOK</li>
 *   <li>Weekly price change as a signed, colour-coded percentage</li>
 *   <li>A {@link SparklineChart} showing the recent price trend</li>
 * </ul>
 *
 * <p>Intended for use inside {@link WatchlistNoteDialog}.
 */
class StockInfoCard extends VBox {

    private static final int SPARKLINE_WEEKS = 8;
    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Constructs a new {@link StockInfoCard} for the given stock.
     *
     * @param stock     the stock whose price data is displayed
     * @param converter the currency converter used to derive the NOK price
     */
    StockInfoCard(Stock stock, CurrencyConverter converter) {
        setSpacing(12);

        String currencyCode = stock.getCurrency().getCurrencyCode();
        BigDecimal price = stock.getSalesPrice();
        BigDecimal priceNok = converter.convert(price, stock.getCurrency(), NOK);
        BigDecimal changePercent = stock.getWeeklyChangePercent();

        getChildren().addAll(
                buildStockSection(stock),
                buildMetaRow(stock, currencyCode, price, priceNok, changePercent)
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
     * Builds the horizontal row of four vertical meta cells.
     *
     * @param stock         the stock for sparkline data
     * @param currencyCode  the stock's currency code
     * @param price         the current price in native currency
     * @param priceNok      the current price converted to NOK
     * @param changePercent the weekly change percentage
     * @return an {@link HBox} containing the four vertical cells
     */
    private HBox buildMetaRow(Stock stock, String currencyCode,
                              BigDecimal price, BigDecimal priceNok,
                              BigDecimal changePercent) {
        String priceLabelText = MessageFormat.format(
                LanguageManager.get("col.priceNative"), currencyCode);

        VBox priceCell = buildTextCell(
                priceLabelText,
                ChangeFormatter.formatPlain(price) + " " + currencyCode);

        VBox priceNokCell = buildTextCell(
                LanguageManager.get("col.priceNok"),
                ChangeFormatter.formatPlain(priceNok) + " NOK");

        VBox changeCell = buildChangeCell(changePercent);
        VBox trendCell = buildTrendCell(stock);

        HBox row = new HBox(24, priceCell, priceNokCell, changeCell, trendCell);
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
