package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the stocks table.
 *
 * <p>Provides {@link #getColumnDefs()} so {@link SortColumnTable}
 * can build a header row with the correct labels, widths, alignments and sort keys.
 * Resolves i18n labels on every call so language changes are picked up automatically.</p>
 *
 * <p>Sorting is applied via {@link #applySort(List, SortState)}, which reads the
 * active column and direction from the sort state owned by the table component.
 * This class owns no sort state itself, it is a pure sort-logic provider.</p>
 */
public class StocksSort {

    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Columns that support ascending/descending sort in the stocks table.
     */
    public enum SortColumn {
        TICKER, PRICE_USD, PRICE_NOK, CHANGE_KR, CHANGE_PCT, HIGH_LOW
    }

    private final CurrencyConverter converter;

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
     * Returns the ordered column definitions for the stocks table.
     *
     * <p>Called by {@link SortColumnTable} on every header refresh
     * so that column labels are re-resolved from
     * {@link LanguageManager}.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.ticker"),
                        SortColumn.TICKER, 10, HPos.LEFT),
                TableColumnDef.of(
                        LanguageManager.get("exchange.stocks.col.company"),
                        28, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.priceUSD"),
                        SortColumn.PRICE_USD, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.priceNOK"),
                        SortColumn.PRICE_NOK, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.changeKr"),
                        SortColumn.CHANGE_KR, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.changePct"),
                        SortColumn.CHANGE_PCT, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("exchange.stocks.col.highLow4"),
                        SortColumn.HIGH_LOW, 10, HPos.RIGHT),
                TableColumnDef.of(
                        LanguageManager.get("exchange.stocks.col.trend"),
                        10, HPos.CENTER),
                TableColumnDef.of(
                        LanguageManager.get("exchange.stocks.col.trade"),
                        6, HPos.LEFT)
        );
    }

    /**
     * Sorts the given stock list in-place according to the active column and
     * direction in the provided {@link SortState}. When a secondary sort column
     * is active, ties in the primary comparator are broken by the secondary via
     * {@link Comparator#thenComparing}. No-op when no sort column is active.
     *
     * @param stocks the list to sort in-place
     * @param state  the sort state read from the owning {@link SortColumnTable}
     */
    public void applySort(List<Stock> stocks, SortState<SortColumn> state) {
        if (!state.hasActiveSort()) return;

        Comparator<Stock> comparator = buildComparator(state.getActiveColumn());
        if (!state.isAscending()) comparator = comparator.reversed();

        if (state.hasSecondarySort()) {
            Comparator<Stock> secondary = buildComparator(state.getSecondaryColumn());
            if (!state.isSecondaryAscending()) secondary = secondary.reversed();
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
     * Returns the latest {@value #HIGH_LOW_WEEKS} historical prices for the given stock.
     *
     * @param stock the stock to read prices from
     * @return the latest price entries up to {@value #HIGH_LOW_WEEKS} entries
     */
    private List<BigDecimal> lastPrices(Stock stock) {
        List<BigDecimal> prices = stock.getHistoricalPrices();
        return prices.subList(Math.max(0, prices.size() - HIGH_LOW_WEEKS), prices.size());
    }
}
