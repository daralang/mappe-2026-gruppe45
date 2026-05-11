package edu.ntnu.idatt2003.millions.view.exchange.overview;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ColourChange;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * A table component displaying a ranked list of stocks with their
 * current price and weekly percentage change.
 * Used for both winners and losers in the exchange overview.
 */
public class StockRankingCard extends VBox {

    private final VBox rows;

    private static final double SYMBOL_WIDTH = 60;
    private static final double PRICE_WIDTH  = 70;
    private static final double CHANGE_WIDTH = 70;
    private static final double NAME_WIDTH = 70;

    /**
     * Constructs a StockRankingTable with a title and an initial list of stocks.
     *
     * @param titleKey the i18n key for the table title
     * @param stocks   the initial list of stocks to display
     * @throws NullPointerException if titleKey or stocks is null
     */
    public StockRankingCard(String titleKey, List<Stock> stocks) {
        getStyleClass().add("card");
        setSpacing(12);

        StyledText title = StyledText.widgetValue(LanguageManager.get(titleKey));

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
        Label symbol = StyledText.detailValue(LanguageManager.get("exchange.overview.columnSymbol"));
        Label stock  = StyledText.detailLabel(LanguageManager.get("exchange.overview.columnStock"));
        Label price  = StyledText.detailLabel(LanguageManager.get("exchange.overview.columnPrice"));
        Label change = StyledText.detailLabel(LanguageManager.get("exchange.overview.columnChange"));

        symbol.setMinWidth(SYMBOL_WIDTH);
        stock.setMinWidth(NAME_WIDTH);
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
        Label symbolLabel = StyledText.detailValue(stock.getSymbol());
        Label nameLabel   = StyledText.detailLabel(stock.getCompany());
        Label priceLabel  = StyledText.detailValue(stock.getSalesPrice().toPlainString());
        Label changeLabel = StyledText.detailValue(formatChange(stock));

        nameLabel.setMinWidth(NAME_WIDTH);
        symbolLabel.setMinWidth(SYMBOL_WIDTH);
        priceLabel.setMinWidth(PRICE_WIDTH);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        changeLabel.setMinWidth(CHANGE_WIDTH);
        changeLabel.setAlignment(Pos.CENTER_RIGHT);

        ColourChange.applyChangeStyle(changeLabel, stock.getLatestPriceChange());

        return buildRow(symbolLabel, nameLabel, priceLabel, changeLabel);
    }

    /**
     * Lays out four labels in a fixed-width HBox row.
     *
     * @param left   label for the symbol column
     * @param center label for the company name column
     * @param price  label for the price column
     * @param right  label for the change column
     * @return a configured HBox
     */
    private HBox buildRow(Label left, Label center, Label price, Label right) {
        center.setMinWidth(0);
        center.setMaxWidth(Double.MAX_VALUE);
        center.setTextOverrun(OverrunStyle.ELLIPSIS);
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox row = new HBox(8, left, center, price, right);
        row.setAlignment(Pos.CENTER_LEFT);
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