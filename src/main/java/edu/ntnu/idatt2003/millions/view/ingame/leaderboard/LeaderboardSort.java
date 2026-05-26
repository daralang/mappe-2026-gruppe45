package edu.ntnu.idatt2003.millions.view.ingame.leaderboard;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortProvider;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.TableColumnDef;
import javafx.geometry.HPos;

import java.util.Comparator;
import java.util.List;

/**
 * Defines sortable columns and comparators for the leaderboard table.
 *
 * <p>Labels are resolved on every call so language changes are picked up automatically.</p>
 */
public class LeaderboardSort extends SortProvider<LeaderboardEntry, LeaderboardSort.SortColumn> {

    /** Columns that support ascending/descending sort in the leaderboard table. */
    public enum SortColumn {
        RANK, PLAYER, RETURN, NET_WORTH, WEEKS, STATUS, OUTCOME
    }

    /**
     * Returns the ordered column definitions for the leaderboard table.
     *
     * @return a fresh list of {@link TableColumnDef} in display order
     */
    @Override
    public List<TableColumnDef<SortColumn>> getColumnDefs() {
        return List.of(
                TableColumnDef.sortable("col.rank", SortColumn.RANK, 8, HPos.LEFT),
                TableColumnDef.sortable("col.player", SortColumn.PLAYER, 20, HPos.LEFT),
                TableColumnDef.sortable("col.return", SortColumn.RETURN, 14, HPos.RIGHT),
                TableColumnDef.sortable("col.netWorth", SortColumn.NET_WORTH, 14, HPos.RIGHT),
                TableColumnDef.sortable("col.weeks", SortColumn.WEEKS, 12, HPos.RIGHT),
                TableColumnDef.sortable("col.status", SortColumn.STATUS, 16, HPos.LEFT),
                TableColumnDef.sortable("col.outcome", SortColumn.OUTCOME, 16, HPos.LEFT)
        );
    }

    /**
     * Builds a {@link Comparator} for the given sort column.
     *
     * @param column the column to build a comparator for
     * @return a comparator for the given column
     */
    @Override
    protected Comparator<LeaderboardEntry> buildComparator(SortColumn column) {
        return switch (column) {
            // No explicit rank field exists; rank is derived by returnPercent
            // (highest return = rank 1). Reversed so ascending sort puts rank 1 at the top.
            case RANK ->
                    Comparator.comparing(LeaderboardEntry::returnPercent)
                              .thenComparing(LeaderboardEntry::finalNetWorth)
                              .reversed();
            case PLAYER ->
                    Comparator.comparing(LeaderboardEntry::playerName, String.CASE_INSENSITIVE_ORDER);
            case RETURN ->
                    Comparator.comparing(LeaderboardEntry::returnPercent);
            case NET_WORTH ->
                    Comparator.comparing(LeaderboardEntry::finalNetWorth);
            case WEEKS ->
                    Comparator.comparingInt(LeaderboardEntry::weeksPlayed);
            case STATUS ->
                    Comparator.comparing(LeaderboardEntry::status);
            case OUTCOME ->
                    Comparator.comparing(LeaderboardEntry::outcome);
        };
    }
}
