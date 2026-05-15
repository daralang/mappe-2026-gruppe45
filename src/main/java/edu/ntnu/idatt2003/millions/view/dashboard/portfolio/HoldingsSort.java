package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
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
                TableColumnDef.spacer(18, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("col.company"),
                        SortColumn.COMPANY, 22, HPos.LEFT),
                TableColumnDef.sortable(
                        LanguageManager.get("col.quantity"),
                        SortColumn.QUANTITY, 10, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("col.weeklyChange"),
                        SortColumn.WEEKLY_CHANGE,
                        "tooltip.shared.weeklyChange", 12, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("col.valueNok"),
                        SortColumn.VALUE_NOK,
                        "tooltip.holdings.valueNok", 12, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("col.returnPct"),
                        SortColumn.RETURN_PCT,
                        "tooltip.shared.returnPct", 10, HPos.RIGHT),
                TableColumnDef.sortableWithTooltip(
                        LanguageManager.get("col.returnNok"),
                        SortColumn.RETURN_NOK,
                        "tooltip.shared.returnNok", 11, HPos.RIGHT),
                TableColumnDef.spacer(5, HPos.CENTER)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
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
        };
    }
}
