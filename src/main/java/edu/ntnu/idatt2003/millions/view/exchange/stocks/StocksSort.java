package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.Comparator;
import java.util.List;

/**
 * Manages sort state and header rendering for the stocks table.
 *
 * <p>Owns the active {@link SortColumn} and sort direction. Builds a header row
 * into a provided {@link GridPane} with clickable sort buttons and a clear-sort
 * button when a column is active. Sorts a stock list in-place via
 * {@link #applySort(List)}.
 *
 * <p>Callers supply a {@code Runnable onChanged} to {@link #buildHeader(GridPane, Runnable)}
 * so the table can re-render when the sort state changes.
 */
public class StocksSort {

    /**
     * Columns that support ascending/descending sort.
     */
    public enum SortColumn {
        TICKER, PRICE, CHANGE_KR, CHANGE_PCT
    }

    private SortColumn activeSortColumn = null;
    private boolean sortAscending = true;

    /**
     * Builds the header row into row 0 of the given grid.
     *
     * <p>Sortable columns render as clickable buttons with a ↑/↓ indicator.
     * A "clear sort" button appears in the trade column when a sort is active.
     * Clicking a sort button activates ascending sort, or toggles direction if
     * already active. Clicking clear sort resets state via {@link #clearSort()}.
     *
     * @param grid      the grid to add the header row into
     * @param onChanged callback invoked after any sort state change so the table
     *                  can re-render
     */
    public void buildHeader(GridPane grid, Runnable onChanged) {
        grid.add(buildSortableHeader("exchange.stocks.col.ticker", SortColumn.TICKER, onChanged), 0, 0);
        grid.add(buildStaticHeader("exchange.stocks.col.company"), 1, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.priceUSD", SortColumn.PRICE, onChanged),2, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.changeKr", SortColumn.CHANGE_KR, onChanged), 3, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.changePct", SortColumn.CHANGE_PCT, onChanged), 4, 0);
        grid.add(buildStaticHeader("exchange.stocks.col.trend"),                                           5, 0);

        if (activeSortColumn != null) {
            Button clearButton = new Button(LanguageManager.get("exchange.stocks.sort.clear"));
            clearButton.getStyleClass().add("holdings-header");
            clearButton.setOnAction(e -> {
                clearSort();
                onChanged.run();
            });
            grid.add(clearButton, 6, 0);
        } else {
            grid.add(buildStaticHeader("exchange.stocks.col.trade"), 6, 0);
        }
    }

    /**
     * Sorts the given stock list in-place according to the active sort column
     * and direction. No-op when no sort column is active.
     *
     * @param stocks the list to sort
     */
    public void applySort(List<Stock> stocks) {
        if (activeSortColumn == null) return;

        Comparator<Stock> comparator = switch (activeSortColumn) {
            case TICKER -> Comparator.comparing(Stock::getSymbol);
            case PRICE -> Comparator.comparing(Stock::getSalesPrice);
            case CHANGE_KR -> Comparator.comparing(Stock::getLatestPriceChange);
            case CHANGE_PCT -> Comparator.comparing(Stock::getWeeklyChangePercent);
        };

        if (!sortAscending) comparator = comparator.reversed();
        stocks.sort(comparator);
    }

    /**
     * Resets the active sort column and direction to their defaults.
     * After calling this, {@link #applySort(List)} becomes a no-op.
     */
    public void clearSort() {
        activeSortColumn = null;
        sortAscending = true;
    }

    /**
     * Returns whether a sort column is currently active.
     *
     * @return {@code true} if a sort column is active, {@code false} otherwise
     */
    public boolean isActive() {
        return activeSortColumn != null;
    }

    /**
     * Builds a sortable header button for the given column.
     * Appends ↑ or ↓ when this column is the active sort column.
     *
     * @param labelKey  the i18n key for the column label
     * @param column    the sort column this header controls
     * @param onChanged callback invoked after the sort state changes
     * @return a styled {@link Button} acting as the column header
     */
    private Button buildSortableHeader(String labelKey, SortColumn column, Runnable onChanged) {
        String indicator = activeSortColumn == column ? (sortAscending ? " ↓ " : "  ↑") : " ↓↑";
        Button header = new Button(LanguageManager.get(labelKey) + indicator);
        header.getStyleClass().add("holdings-header");
        header.setOnAction(e -> {
            if (activeSortColumn == column) {
                sortAscending = !sortAscending;
            } else {
                activeSortColumn = column;
                sortAscending = true;
            }
            onChanged.run();
        });
        return header;
    }

    /**
     * Builds a non-sortable header label for the given column.
     *
     * @param labelKey the i18n key for the column label
     * @return a styled {@link Label} via {@link TableCells#header(String)}
     */
    private Label buildStaticHeader(String labelKey) {
        return TableCells.header(LanguageManager.get(labelKey));
    }
}
