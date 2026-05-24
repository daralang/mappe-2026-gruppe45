package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import java.util.List;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.Currency;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the watchlist table.
 *
 * <p>Operates on {@link WatchlistItem} so {@link Stock} data is available for sorting.
 * Column labels are resolved via {@link LanguageManager} on every call so language
 * changes are picked up automatically.</p>
 */
class WatchlistSort extends SortProvider<WatchlistItem, WatchlistSort.SortColumn> {

    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * All columns in the watchlist table.
     * TICKER, COMPANY, CURRENCY, PRICE_ALT, PRICE_NOK, CHANGE_NOK, CHANGE_PCT and
     * HIGH_LOW are sortable; TREND, TRADE, NOTE and REMOVE are non-sortable and
     * exist only as structural keys for
     * {@link edu.ntnu.idatt2003.millions.view.component.table.RowCells}.
     */
    enum SortColumn {
        TICKER, COMPANY, CURRENCY, PRICE_ALT, PRICE_NOK, CHANGE_NOK, CHANGE_PCT, HIGH_LOW,
        TREND, TRADE, NOTE, REMOVE
    }

    private final CurrencyConverter converter;

    /**
     * Creates a watchlist sorter using the given converter for NOK price sorting.
     *
     * @param converter the converter used for NOK price values
     * @throws NullPointerException if converter is null
     */
    WatchlistSort(CurrencyConverter converter) {
        this.converter = Objects.requireNonNull(converter, "Converter cannot be null");
    }

    /**
     * Returns the ordered column definitions for the watchlist table.
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable(
                        "col.ticker", SortColumn.TICKER, 8, HPos.LEFT),
                TableColumnDef.sortable(
                        "col.company", SortColumn.COMPANY, 24, HPos.LEFT),
                TableColumnDef.sortable("col.currency", SortColumn.CURRENCY, 7, HPos.LEFT),
                TableColumnDef.sortable("col.priceNative", SortColumn.PRICE_ALT, 8, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.priceNok", SortColumn.PRICE_NOK, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.changeNok", SortColumn.CHANGE_NOK,
                        "tooltip.shared.changeNok", 11, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.changePct", SortColumn.CHANGE_PCT,
                        "tooltip.shared.weeklyChange", 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.highLow", SortColumn.HIGH_LOW,
                        "tooltip.shared.highLow", 10, HPos.RIGHT),
                TableColumnDef.nonSortable(SortColumn.TREND, "col.trend",
                        "tooltip.shared.trend", 12, HPos.CENTER),
                TableColumnDef.nonSortable(SortColumn.TRADE, "col.trade", 7, HPos.CENTER),
                TableColumnDef.nonSortable(SortColumn.NOTE, "col.note", 7, HPos.CENTER),
                TableColumnDef.spacer(SortColumn.REMOVE, 3, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     * {@link SortColumn#TREND}, {@link SortColumn#TRADE}, {@link SortColumn#NOTE}
     * and {@link SortColumn#REMOVE} are non-sortable structural columns and are
     * excluded from sortable column definitions in {@link #getColumnDefs()}, so
     * they should never reach this method.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     * @throws IllegalStateException if a non-sortable column is encountered
     */
    @Override
    protected Comparator<WatchlistItem> buildComparator(SortColumn column) {
        return switch (column) {
            case TICKER -> Comparator.comparing(i -> i.stock().getSymbol());
            case COMPANY -> Comparator.comparing(i -> i.stock().getCompany());
            case CURRENCY -> Comparator.comparing(i -> i.stock().getCurrency().getCurrencyCode());
            case PRICE_ALT -> Comparator.comparing(i -> i.stock().getSalesPrice());
            case PRICE_NOK -> Comparator.comparing(i -> priceInNok(i.stock(), converter, NOK));
            case CHANGE_NOK -> Comparator.comparing(i -> changeInNok(i.stock(), converter, NOK));
            case CHANGE_PCT -> Comparator.comparing(i -> i.stock().getWeeklyChangePercent());
            case HIGH_LOW -> Comparator.comparing(i -> highLowRange(i.stock(), converter, NOK, HIGH_LOW_WEEKS));
            case TREND, TRADE, NOTE, REMOVE ->
                    throw new IllegalStateException(column + " is not sortable");
        };
    }

}
