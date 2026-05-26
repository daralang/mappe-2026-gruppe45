package edu.ntnu.idatt2003.millions.view.ingame.leaderboard;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;

/**
 * Defines non-sortable columns for the leaderboard table.
 *
 * <p>Labels are resolved on every call so language changes are picked up automatically.</p>
 */
public class LeaderboardSort extends SortProvider<LeaderboardEntry, LeaderboardSort.SortColumn> {

    /** Column keys used for {@link edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells} lookup in the leaderboard table. */
    public enum SortColumn {
        RANK, PLAYER, RETURN, NET_WORTH, WEEKS, STATUS, OUTCOME
    }

    /**
     * Returns the ordered column definitions for the leaderboard table.
     *
     * <p>All columns are non-sortable to preserve correct rank display.</p>
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.nonSortable(SortColumn.RANK, "col.rank", 8, HPos.LEFT),
                TableColumnDef.nonSortable(SortColumn.PLAYER, "col.player", 20, HPos.LEFT),
                TableColumnDef.nonSortable(SortColumn.RETURN, "col.return", 14, HPos.RIGHT),
                TableColumnDef.nonSortable(SortColumn.NET_WORTH, "col.netWorth", 14, HPos.RIGHT),
                TableColumnDef.nonSortable(SortColumn.WEEKS, "col.weeks", 12, HPos.RIGHT),
                TableColumnDef.nonSortable(SortColumn.STATUS, "col.status", 16, HPos.LEFT),
                TableColumnDef.nonSortable(SortColumn.OUTCOME, "col.outcome", 16, HPos.LEFT)
        );
    }

    /**
     * Not used — all leaderboard columns are non-sortable.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     * @throws UnsupportedOperationException always, since sorting is disabled
     */
    @Override
    protected Comparator<LeaderboardEntry> buildComparator(SortColumn column) {
        throw new UnsupportedOperationException(
                "Leaderboard columns are non-sortable; buildComparator should never be called.");
    }
}
