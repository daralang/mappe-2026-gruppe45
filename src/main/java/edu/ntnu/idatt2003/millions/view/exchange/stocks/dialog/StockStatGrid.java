package edu.ntnu.idatt2003.millions.view.exchange.stocks.dialog;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * A 4×2 grid of key-figure stat cards for a single {@link Stock}.
 */
public class StockStatGrid extends GridPane {

    private static final int COLUMN_COUNT = 4;
    private static final int HGAP = 12;
    private static final int VGAP = 12;
    private static final int CARD_SPACING = 4;

    private final StockStatsService statsService = new StockStatsService();

    /**
     * Constructs a {@code StockStatGrid} and immediately populates it with
     * the eight stat cards for the given stock.
     *
     * @param stock       the stock to display key figures for; must not be {@code null}
     * @param converter   the currency converter used to express prices in NOK;
     *                    must not be {@code null}
     * @param currentWeek the current game week, shown in the «Uke» card
     * @throws NullPointerException if {@code stock} or {@code converter} is {@code null}
     */
    public StockStatGrid(Stock stock, CurrencyConverter converter, int currentWeek) {
        Objects.requireNonNull(stock, "stock must not be null");
        Objects.requireNonNull(converter, "converter must not be null");

        getStyleClass().add("stock-detail-stats");
        setHgap(HGAP);
        setVgap(VGAP);

        for (int i = 0; i < COLUMN_COUNT; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / COLUMN_COUNT);
            col.setHgrow(Priority.ALWAYS);
            getColumnConstraints().add(col);
        }

        String currencyCode = stock.getCurrency().getCurrencyCode();

        Label priceNative = plainValue(ChangeFormatter.formatPlain(stock.getSalesPrice()));
        Label priceNok = plainValue(
                ChangeFormatter.formatPlain(statsService.priceInNok(stock, converter)));
        Label weeklyChange  = ChangeFormatter.styledPercent(
                stock.getWeeklyChangePercent(), "modal-section-value");
        Label week = plainValue(String.valueOf(currentWeek));
        Label high = plainValue(ChangeFormatter.formatPlain(stock.getHighestPrice()) + " " + currencyCode);
        Label low = plainValue(ChangeFormatter.formatPlain(stock.getLowestPrice())  + " " + currencyCode);
        Label trend = buildTrendValue(stock);
        Label allTimeChange = ChangeFormatter.styledPercent(
                statsService.allTimeChangePercent(stock), "modal-section-value");

        // Row 0: current price figures and current week
        add(statCard(MessageFormat.format(
                LanguageManager.get("col.priceNative"), currencyCode), priceNative), 0, 0);
        add(statCard(LanguageManager.get("col.priceNok"),              priceNok),     1, 0);
        add(statCard(LanguageManager.get("details.row.weeklyChange"),  weeklyChange), 2, 0);
        add(statCard(LanguageManager.get("col.week"),                  week),         3, 0);

        // Row 1: historical extremes, trend and all-time return
        add(statCard(LanguageManager.get("stockDetail.high"),          high),          0, 1);
        add(statCard(LanguageManager.get("stockDetail.low"),           low),           1, 1);
        add(statCard(LanguageManager.get("col.trend"),                 trend),         2, 1);
        add(statCard(LanguageManager.get("stockDetail.allTimeChange"), allTimeChange), 3, 1);
    }

    /**
     * Builds one stat card: a small muted label above a bold value node.
     *
     * @param labelText the descriptive caption shown above the value
     * @param valueNode the pre-built value label to display
     * @return a styled {@link VBox} containing the label and value
     */
    private VBox statCard(String labelText, Label valueNode) {
        StyledText label = StyledText.detailLabel(labelText);
        label.setWrapText(true);
        VBox card = new VBox(CARD_SPACING, label, valueNode);
        card.getStyleClass().add("stock-detail-stat");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /**
     * Builds a bold value label styled with the shared {@code modal-section-value} class.
     *
     * @param text the value text to display
     * @return the styled label
     */
    private Label plainValue(String text) {
        Label value = new Label(text);
        value.getStyleClass().add("modal-section-value");
        return value;
    }

    /**
     * Builds the trend value label: an up/down/flat arrow with a colour-coded text,
     * derived from the sign of the stock's latest week-over-week price change.
     *
     * @param stock the stock from which the latest price change is read
     * @return the trend label with {@code positive} or {@code negative} style applied
     */
    private Label buildTrendValue(Stock stock) {
        int sign = stock.getLatestPriceChange().signum();
        String key   = sign > 0 ? "stockDetail.trend.up"
                     : sign < 0 ? "stockDetail.trend.down"
                     : "stockDetail.trend.flat";
        String arrow = sign > 0 ? "▲ " : sign < 0 ? "▼ " : "- ";
        Label value = new Label(arrow + LanguageManager.get(key));
        value.getStyleClass().add("modal-section-value");
        if (sign > 0) {
            value.getStyleClass().add("positive");
        } else if (sign < 0) {
            value.getStyleClass().add("negative");
        }
        return value;
    }
}
