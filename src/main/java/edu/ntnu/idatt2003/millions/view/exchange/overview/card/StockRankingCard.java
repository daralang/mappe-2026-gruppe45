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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * A table component displaying a ranked list of stocks with their
 * current price and weekly percentage change.
 * Used for both winners and losers in the exchange overview.
 */
public class StockRankingCard extends VBox {

    private final VBox rows;
    private final String titleKey;
    private final StyledText titleLabel;

    private final Label symbolHeader;
    private final Label stockHeader;
    private final Label priceHeader;
    private final Label changeHeader;

    private static final double SYMBOL_WIDTH = 60;
    private static final double PRICE_WIDTH  = 70;
    private static final double CHANGE_WIDTH = 70;
    private static final double NAME_WIDTH   = 70;

    private static final DecimalFormat PRICE_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        PRICE_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    /**
     * Constructs a StockRankingCard with a title and an initial list of stocks.
     * Registers a language observer so that the title and column headers
     * are refreshed automatically when the language changes.
     *
     * @param titleKey the i18n key for the table title
     * @param stocks   the initial list of stocks to display
     * @throws NullPointerException if titleKey or stocks is null
     */
    public StockRankingCard(String titleKey, List<Stock> stocks) {
        this.titleKey = titleKey;
        getStyleClass().add("card");
        setSpacing(12);

        titleLabel = StyledText.widgetValue(LanguageManager.get(titleKey));
        symbolHeader = addHeaderLabel(LanguageManager.get("exchange.overview.columnSymbol"));
        stockHeader = addHeaderLabel(LanguageManager.get("exchange.overview.columnStock"));
        priceHeader = addHeaderLabel(LanguageManager.get("exchange.overview.columnPrice"));
        changeHeader = addHeaderLabel(LanguageManager.get("exchange.overview.columnChange"));

        applyColumnConstraints(symbolHeader, stockHeader, priceHeader, changeHeader);
        HBox header = addRow(symbolHeader, stockHeader, priceHeader, changeHeader);

        rows = new VBox(4);
        getChildren().addAll(titleLabel, header, rows);

        LanguageManager.addObserver(this::refreshLabels);
        update(stocks);
    }

    /**
     * Updates the title and all column header labels to reflect the current language.
     * Called automatically when the active language changes.
     */
    private void refreshLabels() {
        titleLabel.setText(LanguageManager.get(titleKey));
        symbolHeader.setText(LanguageManager.get("exchange.overview.columnSymbol"));
        stockHeader.setText(LanguageManager.get("exchange.overview.columnStock"));
        priceHeader.setText(LanguageManager.get("exchange.overview.columnPrice"));
        changeHeader.setText(LanguageManager.get("exchange.overview.columnChange"));
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
     * The price is formatted with Norwegian locale and the stock's native currency code.
     * Uses {@link ChangeFormatter#styledPercent} for a coloured weekly change label.
     *
     * @param stock the stock to display
     * @return an HBox representing one table row
     */
    private HBox addRow(Stock stock) {
        Label symbolLabel = StyledText.detailValue(stock.getSymbol());
        Label nameLabel   = StyledText.detailValue(stock.getCompany());
        String formattedPrice = PRICE_FORMAT.format(stock.getSalesPrice())
                + " " + stock.getCurrency().getCurrencyCode();
        Label priceLabel  = StyledText.detailValue(formattedPrice);
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