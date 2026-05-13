package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Manages sort state and header rendering for the stocks table.
 *
 * <p>Owns stock-specific sort columns and comparators. Generic sort state
 * is delegated to {@link SortState}.
 */
public class StocksSort {

    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Columns that support ascending/descending sort.
     */
    public enum SortColumn {
        TICKER, PRICE_USD, PRICE_NOK, CHANGE_KR, CHANGE_PCT, HIGH_LOW
    }

    private final CurrencyConverter converter;
    private final SortState<SortColumn> sortState = new SortState<>();

    /**
     * Creates a stock sorter using the given converter for NOK price sorting.
     *
     * @param converter the converter used for NOK price values
     * @throws NullPointerException if converter is null
     */
    public StocksSort(CurrencyConverter converter) {
        this.converter = Objects.requireNonNull(converter, "Converter cannot be null");
    }

    /**
     * Builds the header row into row 0 of the given grid.
     *
     * <p>Sortable columns render as clickable buttons with a ↑/↓ indicator.
     * Clicking a sort button activates ascending sort, or toggles direction if
     * already active.
     *
     * @param grid      the grid to add the header row into
     * @param onChanged callback invoked after any sort state change so the table
     *                  can re-render
     */
    public void buildHeader(GridPane grid, Runnable onChanged) {
        grid.add(buildSortableHeader(
                "exchange.stocks.col.ticker",
                SortColumn.TICKER, onChanged),
                0,
                0);

        grid.add(buildStaticHeader(
                "exchange.stocks.col.company"),
                1,
                0);
        grid.add(buildSortableHeader(
                "exchange.stocks.col.priceUSD",
                SortColumn.PRICE_USD, onChanged),
                2,
                0);
        grid.add(buildSortableHeader(
                "exchange.stocks.col.priceNOK",
                SortColumn.PRICE_NOK, onChanged),
                3,
                0);
        grid.add(buildSortableHeader(
                "exchange.stocks.col.changeKr",
                SortColumn.CHANGE_KR, onChanged),
                4,
                0);
        grid.add(buildSortableHeader(
                "exchange.stocks.col.changePct",
                SortColumn.CHANGE_PCT, onChanged),
                5,
                0);
        grid.add(buildSortableHeader(
                "exchange.stocks.col.highLow4",
                SortColumn.HIGH_LOW, onChanged),
                6,
                0);
        grid.add(buildStaticHeader("exchange.stocks.col.trend"),
                7,
                0);

        grid.add(buildStaticHeader("exchange.stocks.col.trade"),
                8,
                0);
    }

    /**
     * Sorts the given stock list in-place according to the active sort column
     * and direction. When a secondary sort column is active, ties in the primary
     * comparator are broken by the secondary using {@link Comparator#thenComparing}.
     * No-op when no sort column is active.
     *
     * @param stocks the list to sort
     */
    public void applySort(List<Stock> stocks) {
        if (!sortState.hasActiveSort()) return;

        Comparator<Stock> comparator = buildComparator(sortState.getActiveColumn());
        if (!sortState.isAscending()) comparator = comparator.reversed();

        if (sortState.hasSecondarySort()) {
            Comparator<Stock> secondary = buildComparator(sortState.getSecondaryColumn());
            if (!sortState.isSecondaryAscending()) secondary = secondary.reversed();
            comparator = comparator.thenComparing(secondary);
        }

        stocks.sort(comparator);
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    private Comparator<Stock> buildComparator(SortColumn column) {
        return switch (column) {
            case TICKER -> Comparator.comparing(Stock::getSymbol);
            case PRICE_USD -> Comparator.comparing(Stock::getSalesPrice);
            case PRICE_NOK -> Comparator.comparing(this::priceInNok);
            case CHANGE_KR -> Comparator.comparing(this::changeInNok);
            case CHANGE_PCT -> Comparator.comparing(Stock::getWeeklyChangePercent);
            case HIGH_LOW -> Comparator.comparing(this::highLowRange);
        };
    }

    /**
     * Resets the active sort column and direction to their defaults.
     * After calling this, {@link #applySort(List)} becomes a no-op.
     */
    public void clearSort() {
        sortState.clear();
    }

    /**
     * Returns whether a sort column is currently active.
     *
     * @return {@code true} if a sort column is active, {@code false} otherwise
     */
    public boolean isActive() {
        return sortState.hasActiveSort();
    }

    /**
     * Builds a sortable header button for the given column.
     *
     * <p>When the column is the secondary sort, a {@code ²↓} or {@code ²↑}
     * suffix is appended to the label to indicate its role as a tiebreaker.
     *
     * @param labelKey  the i18n key for the column label
     * @param column    the sort column this header controls
     * @param onChanged callback invoked after the sort state changes
     * @return a styled sort header button
     */
    private Button buildSortableHeader(String labelKey, SortColumn column, Runnable onChanged) {
        String label = LanguageManager.get(labelKey);
        if (sortState.isSecondaryActive(column)) {
            label += sortState.isSecondaryAscending() ? " ²↓" : " ²↑";
        }
        return TableCells.sortHeader(
                label,
                sortState.isActive(column),
                sortState.isAscending(),
                () -> {
                    sortState.toggle(column);
                    onChanged.run();
                });
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

    /**
     * Returns the latest stock price converted to NOK.
     *
     * @param stock the stock to read from
     * @return the latest price in NOK
     */
    private BigDecimal priceInNok(Stock stock) {
        return converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
    }

    /**
     * Returns the latest price change converted to NOK.
     *
     * @param stock the stock to read from
     * @return the latest price change in NOK
     */
    private BigDecimal changeInNok(Stock stock) {
        return converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
    }

    /**
     * Returns the NOK range between the 4-week high and low.
     *
     * @param stock the stock to read from
     * @return the high-low range in NOK
     */
    private BigDecimal highLowRange(Stock stock) {
        List<BigDecimal> prices = lastPrices(stock);
        BigDecimal low = prices.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal high = prices.stream().max(BigDecimal::compareTo).orElseThrow();
        return converter.convert(high.subtract(low), stock.getCurrency(), NOK);
    }

    /**
     * Returns the latest prices used for high-low sorting.
     *
     * @param stock the stock to read from
     * @return the latest price entries
     */
    private List<BigDecimal> lastPrices(Stock stock) {
        List<BigDecimal> prices = stock.getHistoricalPrices();
        return prices.subList(Math.max(0, prices.size() - HIGH_LOW_WEEKS), prices.size());
    }
}
