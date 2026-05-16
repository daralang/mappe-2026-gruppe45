package edu.ntnu.idatt2003.millions.view.dashboard.loans;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Defines sortable columns and comparators for the active loans table.
 *
 */
public class LoansSort extends SortProvider<Loan, LoansSort.SortColumn> {

    /**
     * Columns that support ascending/descending sort in the active loans table.
     */
    public enum SortColumn {
        LOAN, RATE, WEEKS_LEFT, WEEKLY_COST, REMAINING
    }

    private final GameService gameService;

    /**
     * Constructs a loans sorter.
     *
     * @param gameService the game service used to read the current week
     *                    when building the {@code WEEKS_LEFT} comparator
     * @throws NullPointerException if gameService is null
     */
    public LoansSort(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
    }

    /**
     * Returns the ordered column definitions for the active loans table.
     *
     * <p>Called by {@link SortColumnTable}
     * on every header refresh so that column labels are re-resolved and always
     * reflect the active language.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable("col.loan",SortColumn.LOAN, 30, HPos.LEFT),
                TableColumnDef.sortable("col.rate", SortColumn.RATE, 12, HPos.RIGHT),
                TableColumnDef.sortable("col.weeksLeft", SortColumn.WEEKS_LEFT, 14, HPos.RIGHT),
                TableColumnDef.sortable("col.weeklyCost", SortColumn.WEEKLY_COST,
                        "tooltip.loans.weeklyCost",18, HPos.RIGHT),
                TableColumnDef.sortable("col.remaining", SortColumn.REMAINING,  18, HPos.RIGHT),
                TableColumnDef.spacer(8, HPos.RIGHT)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * <p>The {@code WEEKS_LEFT} case captures the current game week at the moment
     * the comparator is requested, so the ordering reflects what the player sees
     * in the rendered table.</p>
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    @Override
    protected Comparator<Loan> buildComparator(SortColumn column) {
        return switch (column) {
            case LOAN -> Comparator.comparing(l -> l.offer().id());
            case RATE -> Comparator.comparing(l -> l.offer().weeklyInterestRate());
            case WEEKS_LEFT -> {
                int currentWeek = gameService.getExchange().getWeek();
                yield Comparator.comparingInt(l -> l.weeksRemaining(currentWeek));
            }
            case WEEKLY_COST -> Comparator.comparing(Loan::weeklyInterest);
            case REMAINING -> Comparator.comparing(Loan::principal);
        };
    }
}
