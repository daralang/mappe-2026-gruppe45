package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A table component displaying a ranked list of stocks with their
 * current price and weekly percentage change.
 * Used for both winners and losers in the exchange overview.
 *
 * <p>Rendered as a {@link SortColumnTable} with four non-sortable columns so that
 * cell height and padding are consistent with all other dashboard tables.
 * The price column header is formatted lazily with the active currency code
 * from {@link CurrencyManager} and refreshes automatically on language and
 * currency changes.</p>
 */
public class StockRankingCard extends VBox {

    private static final double COL_TICKER  = 18;
    private static final double COL_COMPANY = 42;
    private static final double COL_PRICE   = 22;
    private static final double COL_CHANGE  = 18;

    private final String titleKey;
    private final StyledText titleLabel;
    private final SortColumnTable<Void> table;

    /** The most recently displayed stock list; used to re-render on language/currency change. */
    private List<Stock> lastStocks = List.of();

    /**
     * Strong reference to the currency change listener.
     * Required so the {@link WeakChangeListener} registered on
     * {@link CurrencyManager#currencyProperty()} is not immediately garbage-collected.
     */
    private final ChangeListener<Currency> currencyListener = (obs, old, val) -> refreshLabels();

    /**
     * Constructs a StockRankingCard with a title and an initial list of stocks.
     * Registers observers so that title and column headers refresh automatically
     * on language and currency changes.
     *
     * @param titleKey the i18n key for the table title
     * @param stocks   the initial list of stocks to display
     * @throws NullPointerException if titleKey or stocks is null
     */
    public StockRankingCard(String titleKey, List<Stock> stocks) {
        this.titleKey = Objects.requireNonNull(titleKey, "titleKey cannot be null");
        getStyleClass().add("card");
        setSpacing(12);

        titleLabel = StyledText.widgetValue(LanguageManager.get(titleKey));
        table = new SortColumnTable<>(this::columnDefs);

        getChildren().addAll(titleLabel, table.asNode());

        LanguageManager.addObserver(this::refreshLabels);
        CurrencyManager.currencyProperty().addListener(new WeakChangeListener<>(currencyListener));
        update(stocks);
    }

    /**
     * Updates the table with a new list of stocks.
     * Shows an empty-state message when the list has no entries.
     *
     * @param stocks the new list of stocks to display
     * @throws NullPointerException if stocks is null
     */
    public void update(List<Stock> stocks) {
        this.lastStocks = List.copyOf(Objects.requireNonNull(stocks, "stocks cannot be null"));
        table.clearRows();
        table.refreshHeader(() -> {});
        if (stocks.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("exchange.overview.noWeeklyData"));
        } else {
            int row = 1;
            for (Stock stock : stocks) {
                addDataRow(row++, stock);
            }
        }
    }

    /**
     * Refreshes the title and re-renders all rows to reflect the current
     * language and currency. Called automatically by the registered observers.
     */
    private void refreshLabels() {
        titleLabel.setText(LanguageManager.get(titleKey));
        update(lastStocks);
    }

    /**
     * Builds and inserts one data row for the given stock at the specified row index.
     *
     * @param rowIndex the grid row to write to (row 0 is reserved for the header)
     * @param stock    the stock to display
     */
    private void addDataRow(int rowIndex, Stock stock) {
        String formattedPrice = ChangeFormatter.formatPlain(stock.getSalesPrice())
                + " " + CurrencyFormatter.symbol(stock.getCurrency());
        Label changeLabel = ChangeFormatter.styledPercent(
                stock.getWeeklyChangePercent(), "holdings-cell");
        table.addRow(rowIndex,
                TableCells.data(stock.getSymbol()),
                TableCells.data(stock.getCompany()),
                TableCells.data(formattedPrice),
                changeLabel);
    }

    /**
     * Returns the column definitions for this table.
     * Called by {@link SortColumnTable} on every header refresh so that labels
     * reflect the active language and currency.
     *
     * @return a list of four {@link TableColumnDef} in display order
     */
    private List<TableColumnDef<Void>> columnDefs() {
        return List.of(
                TableColumnDef.of("col.ticker",  COL_TICKER,  HPos.LEFT),
                TableColumnDef.of("col.stock",   COL_COMPANY, HPos.LEFT),
                new TableColumnDef<>(
                        () -> MessageFormat.format(
                                LanguageManager.get("col.priceNative"),
                                CurrencyManager.get().getCurrencyCode()),
                        null, null, COL_PRICE, HPos.RIGHT),
                TableColumnDef.of("col.change",  COL_CHANGE,  HPos.RIGHT)
        );
    }
}
