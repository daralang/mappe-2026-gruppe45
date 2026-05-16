package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.watchlist.WatchlistEntry;
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
 * <p>Operates on {@link WatchlistItem} so both {@link Stock} data and {@link WatchlistEntry} metadata
 * are available for sorting. Column labels are resolved via {@link LanguageManager}
 * on every call so language changes are picked up automatically.</p>
 */
class WatchlistSort extends SortProvider<WatchlistItem, WatchlistSort.SortColumn> {

    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Columns that support ascending/descending sort in the watchlist table.
     */
    enum SortColumn {
        TICKER, COMPANY, PRICE_ALT, PRICE_NOK, CHANGE_NOK, CHANGE_PCT, HIGH_LOW, ADDED_WEEK
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
     * <p>The alternative-currency column header is formatted lazily with the
     * currently active currency code from {@link CurrencyManager} so it updates
     * automatically on every header refresh.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable(
                        "col.ticker", SortColumn.TICKER, 10, HPos.LEFT),
                TableColumnDef.sortable(
                        "col.company", SortColumn.COMPANY, 20, HPos.LEFT),
                TableColumnDef.sortable(
                        "col.priceNok", SortColumn.PRICE_NOK, 15, HPos.RIGHT),
                TableColumnDef.sortable("col.priceNative", SortColumn.PRICE_ALT, 15, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.changeNok", SortColumn.CHANGE_NOK,
                        "tooltip.shared.changeNok", 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.changePct", SortColumn.CHANGE_PCT,
                        "tooltip.shared.weeklyChange", 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.highLow", SortColumn.HIGH_LOW,
                        "tooltip.shared.highLow", 10, HPos.RIGHT),
                TableColumnDef.of("col.trend", "tooltip.shared.trend", 12, HPos.CENTER),
                TableColumnDef.sortable(
                        "col.addedWeek", SortColumn.ADDED_WEEK, 10, HPos.CENTER),
                TableColumnDef.of("col.trade", 10, HPos.CENTER),
                TableColumnDef.of("col.note", 7, HPos.CENTER),
                TableColumnDef.spacer(5, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    @Override
    protected Comparator<WatchlistItem> buildComparator(SortColumn column) {
        return switch (column) {
            case TICKER -> Comparator.comparing(i -> i.stock().getSymbol());
            case COMPANY -> Comparator.comparing(i -> i.stock().getCompany());
            case PRICE_NOK -> Comparator.comparing(i -> priceInNok(i.stock(), converter, NOK));
            case PRICE_ALT -> Comparator.comparing(i -> i.stock().getSalesPrice());
            case CHANGE_NOK -> Comparator.comparing(i -> changeInNok(i.stock(), converter, NOK));
            case CHANGE_PCT -> Comparator.comparing(i -> i.stock().getWeeklyChangePercent());
            case HIGH_LOW -> Comparator.comparing(i -> highLowRange(i.stock(), converter, NOK, HIGH_LOW_WEEKS));
            case ADDED_WEEK -> Comparator.comparingInt(i -> i.entry().addedAtWeek());
        };
    }

}
