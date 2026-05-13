package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the transactions table.
 *
 * <p>Provides {@link #getColumnDefs()} so {@link SortColumnTable} can build a
 * header row with the correct labels, widths, alignments and sort keys.
 * Labels are resolved on every call so language changes are picked up automatically.</p>
 *
 * <p>Sorting is applied via {@link #applySort(List, SortState)}, which reads the
 * active column and direction from the sort state owned by the table component.
 * This class owns no sort state itself — it is a pure sort-logic provider.</p>
 *
 * <p>Comparators for amount-based columns (quantity, price, commission, tax, amount)
 * delegate to {@link TransactionStatsService} to compute NOK values consistently
 * with how the table renders them.</p>
 */
public class TransactionsSort {

    /**
     * Columns that support ascending/descending sort in the transactions table.
     */
    public enum SortColumn {
        WEEK, COMPANY, TYPE, QUANTITY, PRICE, COMMISSION, TAX, AMOUNT
    }

    private final TransactionStatsService statsService;
    private final CurrencyConverter converter;

    /**
     * Constructs a transactions sorter.
     *
     * @param statsService the service used to compute per-transaction stats for sorting
     * @param converter    the converter used for currency-aware comparators
     * @throws NullPointerException if either argument is null
     */
    public TransactionsSort(TransactionStatsService statsService, CurrencyConverter converter) {
        this.statsService = Objects.requireNonNull(statsService, "statsService cannot be null");
        this.converter = Objects.requireNonNull(converter, "converter cannot be null");
    }

    /**
     * Returns the ordered column definitions for the transactions table.
     *
     * <p>Called by {@link SortColumnTable} on every header refresh so that
     * column labels are re-resolved from {@link LanguageManager} and always
     * reflect the active language.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.week"),
                        SortColumn.WEEK, 8, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.company"),
                        SortColumn.COMPANY, 26, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.type"),
                        SortColumn.TYPE, 8, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.quantity"),
                        SortColumn.QUANTITY, 7, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.price"),
                        SortColumn.PRICE, 12, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.commission"),
                        SortColumn.COMMISSION, 12, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.tax"),
                        SortColumn.TAX, 13, HPos.RIGHT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.col.amount"),
                        SortColumn.AMOUNT, 14, HPos.RIGHT)
        );
    }

    /**
     * Sorts the given transaction list in-place according to the active column and
     * direction in the provided {@link SortState}. When a secondary sort column
     * is active, ties in the primary comparator are broken by the secondary via
     * {@link Comparator#thenComparing}. No-op when no sort column is active.
     *
     * @param transactions the list to sort in-place
     * @param state        the sort state read from the owning {@link SortColumnTable}
     */
    public void applySort(List<Transaction> transactions, SortState<SortColumn> state) {
        if (!state.hasActiveSort()) return;

        Comparator<Transaction> comparator = buildComparator(state.getActiveColumn());
        if (!state.isAscending()) comparator = comparator.reversed();

        if (state.hasSecondarySort()) {
            Comparator<Transaction> secondary = buildComparator(state.getSecondaryColumn());
            if (!state.isSecondaryAscending()) secondary = secondary.reversed();
            comparator = comparator.thenComparing(secondary);
        }

        transactions.sort(comparator);
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     * Amount-based columns delegate to {@link TransactionStatsService#getStats}
     * to stay consistent with how values are rendered in the table.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    private Comparator<Transaction> buildComparator(SortColumn column) {
        return switch (column) {
            case WEEK ->
                    Comparator.comparingInt(Transaction::getWeek);
            case COMPANY ->
                    Comparator.comparing((Transaction t) -> t.getShare().getStock().getCompany());
            case TYPE ->
                    Comparator.comparingInt((Transaction t) -> t instanceof Purchase ? 0 : 1);
            case QUANTITY ->
                    Comparator.comparing((Transaction t) ->
                            statsService.getStats(t, converter).quantity());
            case PRICE ->
                    Comparator.comparing((Transaction t) ->
                            statsService.getStats(t, converter).pricePerShare());
            case COMMISSION ->
                    Comparator.comparing((Transaction t) ->
                            statsService.getStats(t, converter).commissionNok());
            case TAX ->
                    Comparator.comparing((Transaction t) ->
                            statsService.getStats(t, converter).taxNok());
            case AMOUNT ->
                    Comparator.comparing((Transaction t) ->
                            statsService.getStats(t, converter).amountNok());
        };
    }
}
