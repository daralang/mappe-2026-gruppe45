package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

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

        HBox header = addHeader();

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
        stocks.forEach(stock -> rows.getChildren().add(addRow(stock)));
    }

    /**
     * Builds the column header row using the holdings-header style.
     *
     * @return an HBox containing the header labels
     */
    private HBox addHeader() {
        Label symbol = addHeaderLabel(LanguageManager.get("exchange.overview.columnSymbol"));
        Label stock  = addHeaderLabel(LanguageManager.get("exchange.overview.columnStock"));
        Label price  = addHeaderLabel(LanguageManager.get("exchange.overview.columnPrice"));
        Label change = addHeaderLabel(LanguageManager.get("exchange.overview.columnChange"));

        applyColumnConstraints(symbol, stock, price, change);
        return addRow(symbol, stock, price, change);
    }

    /**
     * Creates a header label styled with the {@code holdings-header} CSS class,
     * consistent with {@link edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.HoldingsCard}.
     *
     * @param text the label text
     * @return a styled header label
     */
    private Label addHeaderLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("holdings-header");
        return label;
    }

    /**
     * Applies fixed column widths and right-alignment to the price and change labels.
     *
     * @param symbol the symbol column label
     * @param name   the company name column label
     * @param price  the price column label
     * @param change the change column label
     */
    private void applyColumnConstraints(Label symbol, Label name, Label price, Label change) {
        symbol.setMinWidth(SYMBOL_WIDTH);
        name.setMinWidth(NAME_WIDTH);
        price.setMinWidth(PRICE_WIDTH);
        price.setAlignment(Pos.CENTER_RIGHT);
        change.setMinWidth(CHANGE_WIDTH);
        change.setAlignment(Pos.CENTER_RIGHT);
    }

    /**
     * Builds a data row for the given stock.
     * Uses {@link ChangeFormatter#styledPercent} for a coloured weekly change label.
     *
     * @param stock the stock to display
     * @return an HBox representing one table row
     */
    private HBox addRow(Stock stock) {
        Label symbolLabel = StyledText.detailValue(stock.getSymbol());
        Label nameLabel   = StyledText.detailValue(stock.getCompany());
        Label priceLabel  = StyledText.detailValue(stock.getSalesPrice().toPlainString());
        Label changeLabel = ChangeFormatter.styledPercent(
                stock.getWeeklyChangePercent(), "detail-value");

        applyColumnConstraints(symbolLabel, nameLabel, priceLabel, changeLabel);
        return addRow(symbolLabel, nameLabel, priceLabel, changeLabel);
    }

    /**
     * Lays out four labels in a fixed-width HBox row.
     * The center column absorbs remaining space and truncates with an ellipsis.
     *
     * @param left   label for the symbol column
     * @param center label for the company name column
     * @param price  label for the price column
     * @param right  label for the change column
     * @return a configured HBox
     */
    private HBox addRow(Label left, Label center, Label price, Label right) {
        center.setMinWidth(0);
        center.setMaxWidth(Double.MAX_VALUE);
        center.setTextOverrun(OverrunStyle.ELLIPSIS);
        HBox.setHgrow(center, Priority.ALWAYS);

        HBox row = new HBox(10, left, center, price, right);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}