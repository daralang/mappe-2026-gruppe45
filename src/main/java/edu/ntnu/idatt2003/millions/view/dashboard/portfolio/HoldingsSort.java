package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import java.util.List;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the holdings table.
 *
 * <p>Provides {@link #getColumnDefs()} so {@link SortColumnTable} can build a
 * header row with correct labels, widths, alignments and sort keys. Labels are
 * resolved on every call so language changes are picked up automatically.</p>
 */
public class HoldingsSort extends SortProvider<Share, HoldingsSort.SortColumn> {

    /**
     * All columns in the holdings table.
     * COMPANY, QUANTITY, WEEKLY_CHANGE, VALUE_NOK, RETURN_PCT and RETURN_NOK
     * are sortable; ACTIONS and DETAILS are non-sortable and exist only as
     * structural keys for {@link RowCells}.
     */
    public enum SortColumn {
        ACTIONS, COMPANY, QUANTITY, WEEKLY_CHANGE, VALUE_NOK, RETURN_PCT, RETURN_NOK, DETAILS
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
     * column labels are re-resolved and always reflect the active language.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.spacer(SortColumn.ACTIONS, 14, HPos.LEFT),
                TableColumnDef.sortable(
                        "col.company", SortColumn.COMPANY, 22, HPos.LEFT),
                TableColumnDef.sortable(
                        "col.quantity", SortColumn.QUANTITY, 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.weeklyChange", SortColumn.WEEKLY_CHANGE,
                        "tooltip.shared.weeklyChange", 13, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.valueNok", SortColumn.VALUE_NOK,
                        "tooltip.holdings.valueNok", 12, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.returnPct", SortColumn.RETURN_PCT,
                        "tooltip.shared.returnPct", 10, HPos.RIGHT),
                TableColumnDef.sortable(
                        "col.returnNok", SortColumn.RETURN_NOK,
                        "tooltip.shared.returnNok", 12, HPos.RIGHT),
                TableColumnDef.spacer(SortColumn.DETAILS, 5, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     * {@link SortColumn#ACTIONS} and {@link SortColumn#DETAILS} are non-sortable
     * structural columns and are excluded from sortable column definitions in
     * {@link #getColumnDefs()}, so they should never reach this method.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     * @throws IllegalStateException if a non-sortable column is encountered
     */
    @Override
    protected Comparator<Share> buildComparator(SortColumn column) {
        return switch (column) {
            case COMPANY ->
                    Comparator.comparing(s -> s.getStock().getCompany());
            case QUANTITY ->
                    Comparator.comparing(Share::getQuantity);
            case WEEKLY_CHANGE ->
                    Comparator.comparing(s -> s.getStock().getWeeklyChangePercent());
            case VALUE_NOK ->
                    Comparator.comparing(s -> portfolioService.getShareValueInNok(s, converter));
            case RETURN_PCT ->
                    Comparator.comparing(Share::getReturnPercent);
            case RETURN_NOK ->
                    Comparator.comparing(s -> portfolioService.getShareReturnInNok(s, converter));
            case ACTIONS, DETAILS ->
                    throw new IllegalStateException(column + " is not sortable");
        };
    }
}
