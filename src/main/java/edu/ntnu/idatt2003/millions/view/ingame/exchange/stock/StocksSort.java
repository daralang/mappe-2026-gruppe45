package edu.ntnu.idatt2003.millions.view.ingame.exchange.stock;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.stock.StockStatsService;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.TableColumnDef;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.geometry.HPos;

/**
 * Defines sortable columns and comparators for the stocks table.
 */
public class StocksSort extends SortProvider<Stock, StocksSort.SortColumn> {

    /**
     * All columns in the stocks table.
     * Sortable columns are WATCHLIST, TICKER, PRICE_NOK,
     * CHANGE_KR and CHANGE_PCT. The remaining columns
     * COMPANY, TREND, TRADE, DETAILS are non-sortable and exist as structural keys for {@link RowCells}.
     */
    public enum SortColumn {
        WATCHLIST, TICKER, COMPANY, PRICE_NOK, CHANGE_KR, CHANGE_PCT, TREND, TRADE, DETAILS
    }

    private final StockStatsService statsService = new StockStatsService();
    private final CurrencyConverter converter;
    private final Predicate<String> isWatched;

    /**
     * Creates a stock sorter using the given converter and watchlist predicate.
     *
     * @param converter the converter used for NOK price values
     * @param isWatched predicate that returns {@code true} if the given stock symbol
     *                  is on the player's watchlist
     * @throws NullPointerException if either argument is null
     */
    public StocksSort(CurrencyConverter converter, Predicate<String> isWatched) {
        this.converter = Objects.requireNonNull(converter, "Converter cannot be null");
        this.isWatched = Objects.requireNonNull(isWatched, "isWatched cannot be null");
    }

    /**
     * Returns the ordered column definitions for the stocks table.
     *
     * <p>Called by {@link SortColumnTable} on every header refresh so that labels are
     * re-resolved from the active language automatically.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable("col.watchlist", SortColumn.WATCHLIST,
                        "tooltip.stocks.watchlist", 6, HPos.CENTER),
                TableColumnDef.sortable("col.ticker", SortColumn.TICKER, 10, HPos.LEFT),
                TableColumnDef.nonSortable(SortColumn.COMPANY, "col.company", 26, HPos.LEFT),
                TableColumnDef.sortable("col.priceNok", SortColumn.PRICE_NOK, 12, HPos.RIGHT),
                TableColumnDef.sortable("col.changeNok", SortColumn.CHANGE_KR,
                        "tooltip.shared.changeNok", 12, HPos.RIGHT),
                TableColumnDef.sortable("col.changePct", SortColumn.CHANGE_PCT,
                        "tooltip.shared.weeklyChange", 12, HPos.RIGHT),
                TableColumnDef.nonSortable(SortColumn.TREND, "col.trend",
                        "tooltip.shared.trend", 12, HPos.CENTER),
                TableColumnDef.nonSortable(SortColumn.TRADE, "col.trade", 5, HPos.CENTER),
                TableColumnDef.nonSortable(SortColumn.DETAILS, "col.details", 5, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     * {@link SortColumn#DETAILS} is non-sortable and should never reach this method;
     * it is excluded from sortable column definitions in {@link #getColumnDefs()}.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     * @throws IllegalStateException if an unexpected sort column is encountered
     */
    @Override
    protected Comparator<Stock> buildComparator(SortColumn column) {
        return switch (column) {
            case WATCHLIST -> Comparator.comparing(s -> !isWatched.test(s.getSymbol()));
            case TICKER -> Comparator.comparing(Stock::getSymbol);
            case PRICE_NOK -> Comparator.comparing(s -> statsService.priceInNok(s, converter));
            case CHANGE_KR -> Comparator.comparing(s -> statsService.changeInNok(s, converter));
            case CHANGE_PCT -> Comparator.comparing(Stock::getWeeklyChangePercent);
            case COMPANY, TREND, TRADE, DETAILS ->
                    throw new IllegalStateException(column + " is not sortable");
        };
    }

}
