package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import java.util.List;
import javafx.geometry.HPos;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the stocks table.
 *
 * <p>Provides {@link #getColumnDefs()} so {@link SortColumnTable}
 * can build a header row with the correct labels, widths, alignments and sort keys.
 * Resolves i18n labels on every call so language changes are picked up automatically.</p>
 */
public class StocksSort extends SortProvider<Stock, StocksSort.SortColumn> {

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
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    @Override
    protected Comparator<Stock> buildComparator(SortColumn column) {
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
     * Returns the NOK range between the {@value #HIGH_LOW_WEEKS}-week high and low.
     *
     * @param stock the stock to read from
     * @return the high-low range in NOK
     */
    private BigDecimal highLowRange(Stock stock) {
        List<BigDecimal> prices = stock.getRecentPrices(HIGH_LOW_WEEKS);
        BigDecimal low = prices.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal high = prices.stream().max(BigDecimal::compareTo).orElseThrow();
        return converter.convert(high.subtract(low), stock.getCurrency(), NOK);
    }
}
