package edu.ntnu.idatt2003.millions.view.exchange.overview;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ColourChange;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * A reusable table component displaying a ranked list of stocks with their
 * current price and weekly percentage change.
 * Used for both winners and losers in the exchange overview.
 */
public class StockRankingTable extends VBox {

    private final VBox rows;

    private static final double SYMBOL_WIDTH = 60;
    private static final double PRICE_WIDTH  = 70;
    private static final double CHANGE_WIDTH = 70;

    /**
     * Constructs a StockRankingTable with a title and an initial list of stocks.
     *
     * @param titleKey the i18n key for the table title
     * @param stocks   the initial list of stocks to display
     * @throws NullPointerException if titleKey or stocks is null
     */
    public StockRankingTable(String titleKey, List<Stock> stocks) {
        getStyleClass().add("card");
        setSpacing(12);

        Label title = new Label(LanguageManager.get(titleKey));
        title.getStyleClass().add("card-value");

        HBox header = buildHeader();

        rows = new VBox(4);
        getChildren().addAll(title, header, rows);

        update(stocks);
    }

    /**
     * Updates the table with a new list of stocks.
     * Replaces all existing rows.
     *
     * @param stocks the new list of stocks to display
     * @throws NullPointerException if stocks is null
     */
    public void update(List<Stock> stocks) {
        rows.getChildren().clear();
        stocks.forEach(stock -> rows.getChildren().add(buildRow(stock)));
    }

    /**
     * Builds the column header row.
     *
     * @return an HBox containing the header labels
     */
    private HBox buildHeader() {
        Label symbol = new Label(LanguageManager.get("exchange.overview.columnSymbol"));
        Label stock  = new Label(LanguageManager.get("exchange.overview.columnStock"));
        Label price  = new Label(LanguageManager.get("exchange.overview.columnPrice"));
        Label change = new Label(LanguageManager.get("exchange.overview.columnChange"));

        symbol.getStyleClass().add("card-value");
        stock.getStyleClass().add("card-label");
        price.getStyleClass().add("card-label");
        change.getStyleClass().add("card-label");

        symbol.setMinWidth(SYMBOL_WIDTH);
        price.setMinWidth(PRICE_WIDTH);
        price.setAlignment(Pos.CENTER_RIGHT);
        change.setMinWidth(CHANGE_WIDTH);
        change.setAlignment(Pos.CENTER_RIGHT);

        return buildRow(symbol, stock, price, change);
    }

    /**
     * Builds a data row for the given stock.
     *
     * @param stock the stock to display
     * @return an HBox representing one table row
     */
    private HBox buildRow(Stock stock) {
        Label symbolLabel = new Label(stock.getSymbol());
        Label nameLabel = new Label(stock.getCompany());
        Label priceLabel = new Label(stock.getSalesPrice().toPlainString());
        Label changeLabel = new Label(formatChange(stock));

        symbolLabel.setMinWidth(SYMBOL_WIDTH);
        priceLabel.setMinWidth(PRICE_WIDTH);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        changeLabel.setMinWidth(CHANGE_WIDTH);
        changeLabel.setAlignment(Pos.CENTER_RIGHT);

        ColourChange.applyChangeStyle(changeLabel, stock.getLatestPriceChange());

        return buildRow(symbolLabel, nameLabel, priceLabel, changeLabel);
    }

    /**
     * Lays out three labels in a spaced HBox row.
     *
     * @param left   label for the left column
     * @param center label for the center column
     * @param right  label for the right column
     * @return a configured HBox
     */
    private HBox buildRow(Label left, Label center, Label price, Label right) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(8, left, center, spacer, price, right);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(row.getChildren().get(3), Priority.NEVER);
        right.setMinWidth(60);
        right.setAlignment(Pos.CENTER_RIGHT);
        return row;
    }

    /**
     * Formats the weekly percentage change for a stock.
     * Returns "–" if no previous price exists.
     *
     * @param stock the stock to compute the change for
     * @return a formatted string such as "+6,6%" or "-4,6%"
     */
    private String formatChange(Stock stock) {
        BigDecimal change = stock.getLatestPriceChange();
        List<BigDecimal> prices = stock.getHistoricalPrices();

        if (prices.size() < 2) return "–";

        BigDecimal previous = prices.get(prices.size() - 2);
        if (previous.compareTo(BigDecimal.ZERO) == 0) return "–";

        BigDecimal percentage = change
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        String sign = percentage.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return sign + String.format(Locale.of("no"), "%.1f", percentage) + "%";
    }
}