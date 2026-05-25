package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * A vertically scrollable list of a stock's weekly prices in its own (native) currency.
 *
 * <p>Renders one row per price as «{@code Uke n}» on the left and the formatted price
 * on the right, oldest week first. Purely presentational: it receives a ready price
 * list and renders it. Reuses the shared {@code content-scroll} scroll styling.</p>
 */
public class PriceHistoryList extends VBox {

    /**
     * Constructs a {@code PriceHistoryList} for the given prices.
     *
     * @param prices   the weekly prices in the stock's own currency, oldest first;
     *                 must not be {@code null} or empty
     * @param currency the currency the prices are expressed in; must not be {@code null}
     * @throws NullPointerException     if {@code prices} or {@code currency} is {@code null}
     * @throws IllegalArgumentException if {@code prices} is empty
     */
    public PriceHistoryList(List<BigDecimal> prices, Currency currency) {
        Objects.requireNonNull(prices, "prices must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("prices must not be empty");
        }

        String currencyCode = currency.getCurrencyCode();
        VBox rows = new VBox();
        rows.getStyleClass().add("price-history-rows");
        for (int i = 0; i < prices.size(); i++) {
            rows.getChildren().add(buildRow(i + 1, prices.get(i), currencyCode));
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.getStyleClass().add("content-scroll");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().add(scroll);
    }

    /**
     * Builds a single «week - price» row.
     *
     * @param week         the 1-based week number
     * @param price        the price for that week
     * @param currencyCode the stock's currency code (e.g. {@code USD})
     * @return the row node
     */
    private HBox buildRow(int week, BigDecimal price, String currencyCode) {
        Label weekLabel = StyledText.detailLabel(LanguageManager.get("app.week") + " " + week);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = StyledText.detailValue(
                ChangeFormatter.formatPlain(price) + " " + currencyCode);
        priceLabel.getStyleClass().add("bold");

        HBox row = new HBox(weekLabel, spacer, priceLabel);
        row.getStyleClass().add("price-history-row");
        return row;
    }
}
