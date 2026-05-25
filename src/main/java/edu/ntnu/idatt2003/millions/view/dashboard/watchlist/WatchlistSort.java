package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.StockStatsService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import java.util.List;
import javafx.geometry.HPos;

import java.util.Comparator;
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

    /**
     * All columns in the watchlist table.
     */
    enum SortColumn {
        TICKER, COMPANY, CURRENCY, PRICE_ALT, PRICE_NOK, CHANGE_NOK, CHANGE_PCT, HIGH_LOW,
        TREND, TRADE, NOTE, DETAILS, REMOVE
    }

    private final StockStatsService statsService = new StockStatsService();
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
                TableColumnDef.nonSortable(SortColumn.DETAILS, "col.details", 5, HPos.CENTER),
                TableColumnDef.spacer(SortColumn.REMOVE, 3, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
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
            case PRICE_NOK -> Comparator.comparing(i -> statsService.priceInNok(i.stock(), converter));
            case CHANGE_NOK -> Comparator.comparing(i -> statsService.changeInNok(i.stock(), converter));
            case CHANGE_PCT -> Comparator.comparing(i -> i.stock().getWeeklyChangePercent());
            case HIGH_LOW -> Comparator.comparing(i -> statsService.highLowRangeInNok(i.stock(), converter, HIGH_LOW_WEEKS));
            case TREND, TRADE, NOTE, DETAILS, REMOVE ->
                    throw new IllegalStateException(column + " is not sortable");
        };
    }

}
