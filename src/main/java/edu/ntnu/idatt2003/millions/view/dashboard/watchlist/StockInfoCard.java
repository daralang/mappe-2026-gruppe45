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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;

/**
 * A styled info box displaying key price data for a single stock.
 *
 * <p>Renders four rows inside a {@code modal-summary}-styled container:
 * <ul>
 *   <li>Current price in the stock's native currency</li>
 *   <li>Current price converted to NOK</li>
 *   <li>Weekly price change as a signed, colour-coded percentage</li>
 *   <li>A {@link SparklineChart} showing the recent price trend</li>
 * </ul>
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
        getStyleClass().add("modal-summary");

        String currencyCode = stock.getCurrency().getCurrencyCode();
        BigDecimal price = stock.getSalesPrice();
        BigDecimal priceNok = converter.convert(price, stock.getCurrency(), NOK);
        BigDecimal changePercent = stock.getWeeklyChangePercent();

        String priceLabelText = MessageFormat.format(
                LanguageManager.get("watchlist.note.info.price"), currencyCode);

        getChildren().addAll(
                buildTextRow(priceLabelText,
                        ChangeFormatter.formatPlain(price) + " " + currencyCode),
                buildTextRow(LanguageManager.get("watchlist.note.info.priceNok"),
                        ChangeFormatter.formatPlain(priceNok) + " NOK"),
                buildChangeRow(changePercent),
                buildSparklineRow(stock)
        );
    }

    /**
     * Builds a plain label–value row.
     *
     * @param labelText the left-hand label
     * @param valueText the right-hand value string
     * @return a styled {@link HBox} row
     */
    private HBox buildTextRow(String labelText, String valueText) {
        Label label = StyledText.detailLabel(labelText);
        Label value = StyledText.detailValue(valueText);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(label, spacer, value);
        row.getStyleClass().add("modal-summary-row");
        return row;
    }

    /**
     * Builds a colour-coded change percentage row.
     *
     * @param changePercent the weekly price change percentage
     * @return a styled {@link HBox} row with a coloured value label
     */
    private HBox buildChangeRow(BigDecimal changePercent) {
        Label label = StyledText.detailLabel(LanguageManager.get("watchlist.note.info.change"));
        Label value = ChangeFormatter.styledPercent(changePercent, "detail-value");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(label, spacer, value);
        row.getStyleClass().add("modal-summary-row");
        return row;
    }

    /**
     * Builds a trend row containing a {@link SparklineChart} on the right.
     *
     * @param stock the stock whose price history is plotted
     * @return a styled {@link HBox} row with the sparkline on the right
     */
    private HBox buildSparklineRow(Stock stock) {
        Label label = StyledText.detailLabel(LanguageManager.get("watchlist.note.info.trend"));

        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - SPARKLINE_WEEKS), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(label, spacer, sparkline);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("modal-summary-row");
        return row;
    }
}
