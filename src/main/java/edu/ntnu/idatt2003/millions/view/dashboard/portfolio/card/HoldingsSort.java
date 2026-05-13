package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the holdings table.
 *
 * <p>Provides {@link #getColumnDefs()} so {@link SortColumnTable} can build a
 * header row with correct labels, widths, alignments and sort keys. Labels are
 * resolved on every call so language changes are picked up automatically.</p>
 *
 * <p>Sorting is applied via {@link #applySort(List, SortState)}, which reads the
 * active column and direction from the sort state owned by the table component.
 * This class owns no sort state itself — it is a pure sort-logic provider.</p>
 */
public class HoldingsSort {

    /**
     * Columns that support ascending/descending sort in the holdings table.
     */
    public enum SortColumn {
        COMPANY, QUANTITY, WEEKLY_CHANGE, VALUE_NOK, RETURN_PCT, RETURN_NOK
    }

    private final PortfolioService portfolioService;
    private final CurrencyConverter converter;

    /**
     * Constructs a holdings sorter.
     *
     * @param portfolioService the service used to compute NOK values for sorting
     * @param converter        the converter used for currency-aware comparators
     * @throws NullPointerException if either argument is null
     */
    public HoldingsSort(PortfolioService portfolioService, CurrencyConverter converter) {
        this.portfolioService = Objects.requireNonNull(portfolioService, "portfolioService cannot be null");
        this.converter = Objects.requireNonNull(converter, "converter cannot be null");
    }

    /**
     * Returns the ordered column definitions for the holdings table.
     *
     * <p>Called by {@link SortColumnTable} on every header refresh so that
     * column labels are re-resolved from {@link LanguageManager} and always
     * reflect the active language.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.of("", 18, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("dashboard.portfolio.company"),
                        SortColumn.COMPANY, 22, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("dashboard.portfolio.quantity"),
                        SortColumn.QUANTITY, 10, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("dashboard.portfolio.weeklyChange"),
                        SortColumn.WEEKLY_CHANGE,
                        "tooltip.shared.weeklyChange", 12, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("dashboard.portfolio.valueNok"),
                        SortColumn.VALUE_NOK,
                        "tooltip.holdings.valueNok", 12, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("dashboard.portfolio.returnPct"),
                        SortColumn.RETURN_PCT,
                        "tooltip.shared.returnPct", 10, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("dashboard.portfolio.returnNok"),
                        SortColumn.RETURN_NOK,
                        "tooltip.shared.returnNok", 11, HPos.RIGHT),
                TableColumnDef.of("", 5, HPos.CENTER)
        );
    }

    /**
     * Sorts the given share list in-place according to the active column and
     * direction in the provided {@link SortState}. When a secondary sort column
     * is active, ties in the primary comparator are broken by the secondary via
     * {@link Comparator#thenComparing}. No-op when no sort column is active.
     *
     * @param shares the list to sort in-place
     * @param state  the sort state read from the owning {@link SortColumnTable}
     */
    public void applySort(List<Share> shares, SortState<SortColumn> state) {
        if (!state.hasActiveSort()) return;

        Comparator<Share> comparator = buildComparator(state.getActiveColumn());
        if (!state.isAscending()) comparator = comparator.reversed();

        if (state.hasSecondarySort()) {
            Comparator<Share> secondary = buildComparator(state.getSecondaryColumn());
            if (!state.isSecondaryAscending()) secondary = secondary.reversed();
            comparator = comparator.thenComparing(secondary);
        }

        shares.sort(comparator);
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    private Comparator<Share> buildComparator(SortColumn column) {
        return switch (column) {
            case COMPANY ->
                    Comparator.comparing((Share s) -> s.getStock().getCompany());
            case QUANTITY ->
                    Comparator.comparing((Share s) -> s.getQuantity());
            case WEEKLY_CHANGE ->
                    Comparator.comparing((Share s) -> s.getStock().getWeeklyChangePercent());
            case VALUE_NOK ->
                    Comparator.comparing((Share s) -> portfolioService.getShareValueInNok(s, converter));
            case RETURN_PCT ->
                    Comparator.comparing((Share s) -> s.getReturnPercent());
            case RETURN_NOK ->
                    Comparator.comparing((Share s) -> portfolioService.getShareReturnInNok(s, converter));
        };
    }
}
