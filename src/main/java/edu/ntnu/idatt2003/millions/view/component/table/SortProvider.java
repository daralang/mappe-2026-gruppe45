package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.SortState;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;

/**
 * Abstract base class for sort-logic providers used with {@link SortColumnTable}.
 *
 * <p>Implements the Template Method pattern: {@link #applySort} contains the
 * general algorithm for primary/secondary sorting, while subclasses supply
 * column-specific comparators via {@link #buildComparator} and column metadata
 * via {@link #getColumnDefs}.</p>
 *
 * @param <T> the type of item being sorted
 * @param <C> the enum type identifying sort columns
 */
public abstract class SortProvider<T, C> {

    /**
     * Sorts the given list in-place according to the active column and direction
     * in the provided {@link SortState}. When a secondary sort column is active,
     * ties in the primary comparator are broken by the secondary via
     * {@link Comparator#thenComparing}. No-op when no sort column is active.
     *
     * @param items the list to sort in-place
     * @param state the sort state read from the owning {@link SortColumnTable}
     */
    public void applySort(List<T> items, SortState<C> state) {
        if (!state.hasActiveSort()) return;

        Comparator<T> comparator = buildComparator(state.getActiveColumn());
        if (!state.isAscending()) comparator = comparator.reversed();

        if (state.hasSecondarySort()) {
            Comparator<T> secondary = buildComparator(state.getSecondaryColumn());
            if (!state.isSecondaryAscending()) secondary = secondary.reversed();
            comparator = comparator.thenComparing(secondary);
        }

        items.sort(comparator);
    }

    /**
     * Returns the ordered column definitions for the table.
     *
     * <p>Called by {@link SortColumnTable} on every header refresh so that
     * column labels are re-resolved from the active language automatically.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public abstract List<TableColumnDef<C>> getColumnDefs();

    /**
     * Builds a comparator for the given sort column.
     * Implemented by each subclass to define column-specific ordering.
     *
     * @param column the column to sort by
     * @return a comparator for the given column
     */
    protected abstract Comparator<T> buildComparator(C column);

    /**
     * Returns the latest price of the given stock converted to NOK.
     *
     * @param stock     the stock to read from
     * @param converter the converter used for currency conversion
     * @param nok       the NOK currency instance
     * @return the latest price in NOK
     */
    protected static BigDecimal priceInNok(Stock stock, CurrencyConverter converter, Currency nok) {
        return converter.convert(stock.getSalesPrice(), stock.getCurrency(), nok);
    }

    /**
     * Returns the latest price change of the given stock converted to NOK.
     *
     * @param stock     the stock to read from
     * @param converter the converter used for currency conversion
     * @param nok       the NOK currency instance
     * @return the latest price change in NOK
     */
    protected static BigDecimal changeInNok(Stock stock, CurrencyConverter converter, Currency nok) {
        return converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), nok);
    }

    /**
     * Returns the NOK range between the high and low price over the most recent
     * {@code weeks} prices, read from {@link Stock#getRecentHigh(int)} and
     * {@link Stock#getRecentLow(int)}.
     *
     * @param stock     the stock to read from
     * @param converter the converter used for currency conversion
     * @param nok       the NOK currency instance
     * @param weeks     the number of recent weeks to consider
     * @return the high-low range in NOK
     */
    protected static BigDecimal highLowRange(Stock stock, CurrencyConverter converter,
                                             Currency nok, int weeks) {
        BigDecimal range = stock.getRecentHigh(weeks).subtract(stock.getRecentLow(weeks));
        return converter.convert(range, stock.getCurrency(), nok);
    }
}
