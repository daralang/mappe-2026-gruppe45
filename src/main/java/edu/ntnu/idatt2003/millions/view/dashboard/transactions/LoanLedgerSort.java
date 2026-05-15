package edu.ntnu.idatt2003.millions.view.dashboard.transactions;

import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.LoanLedgerCard;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Defines sortable columns and comparators for the loan-ledger table.
 *
 * <p>Provides {@link #getColumnDefs()} so
 * {@link SortColumnTable}
 * can build a header row with the correct labels, widths, alignments and sort keys.
 * Labels are resolved on every call so language changes are picked up automatically.</p>
 *
 * <p>The LOAN column sorts alphabetically on the loan label
 * (e.g. "Standardlån #1"). Because these labels are computed by
 * {@link LoanLedgerCard} at render time, a {@link Function} that maps an entry to its label is supplied
 * in the constructor so the comparator stays consistent with what the user sees.</p>
 */
public class LoanLedgerSort extends SortProvider<LoanLedgerEntry, LoanLedgerSort.SortColumn> {

    /**
     * Columns that support ascending/descending sort in the loan-ledger table.
     */
    public enum SortColumn {
        WEEK, LOAN, TYPE, AMOUNT
    }

    private final Function<LoanLedgerEntry, String> loanLabelFn;

    /**
     * Constructs a loan-ledger sorter.
     *
     * @param loanLabelFn a function that maps a {@link LoanLedgerEntry} to its
     *                    human-readable loan label (e.g. "Standardlån #1");
     *                    used for alphabetic sorting of the LOAN column
     * @throws NullPointerException if {@code loanLabelFn} is null
     */
    public LoanLedgerSort(Function<LoanLedgerEntry, String> loanLabelFn) {
        this.loanLabelFn = Objects.requireNonNull(loanLabelFn, "loanLabelFn cannot be null");
    }

    /**
     * Returns the ordered column definitions for the loan-ledger table.
     *
     * <p>Called by
     * {@link edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable}
     * on every header refresh so that column labels are re-resolved from
     * {@link LanguageManager} and always reflect the active language.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.loans.col.week"),
                        SortColumn.WEEK, 10, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.loans.col.loan"),
                        SortColumn.LOAN, 38, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.loans.col.type"),
                        SortColumn.TYPE, 22, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("transactions.loans.col.amount"),
                        SortColumn.AMOUNT, 30, HPos.RIGHT)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    @Override
    protected Comparator<LoanLedgerEntry> buildComparator(SortColumn column) {
        return switch (column) {
            case WEEK -> Comparator.comparingInt(LoanLedgerEntry::week)
                    .thenComparingInt(e -> typePriority(e.type()));
            case LOAN -> Comparator.comparing(loanLabelFn);
            case TYPE -> Comparator.comparingInt(e -> e.type().ordinal());
            case AMOUNT -> Comparator.comparing(LoanLedgerEntry::amount);
        };
    }

    private static int typePriority(LoanLedgerEntryType type) {
        return switch (type) {
            case INTEREST     -> 0;
            case DISBURSEMENT -> 1;
            case REPAYMENT    -> 2;
        };
    }
}
