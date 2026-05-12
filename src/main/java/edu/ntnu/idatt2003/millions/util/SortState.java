package edu.ntnu.idatt2003.millions.util;

import java.util.Objects;

/**
 * Reusable state holder for sortable table columns.
 *
 * @param <T> the column identifier type
 */
public class SortState<T> {

    private T activeColumn;
    private boolean ascending = true;

    /**
     * Activates or toggles sorting for the given column.
     *
     * @param column the column to sort by
     * @throws NullPointerException if column is null
     */
    public void toggle(T column) {
        Objects.requireNonNull(column, "Column cannot be null");
        if (Objects.equals(activeColumn, column)) {
            ascending = !ascending;
        } else {
            activeColumn = column;
            ascending = true;
        }
    }

    /**
     * Clears the active sort.
     */
    public void clear() {
        activeColumn = null;
        ascending = true;
    }

    /**
     * Returns whether the given column is actively sorted.
     *
     * @param column the column to check
     * @return true if the column is active
     */
    public boolean isActive(T column) {
        return Objects.equals(activeColumn, column);
    }

    /**
     * Returns whether any column is actively sorted.
     *
     * @return true if sorting is active
     */
    public boolean hasActiveSort() {
        return activeColumn != null;
    }

    /**
     * Returns the active column.
     *
     * @return the active column, or null if no sort is active
     */
    public T getActiveColumn() {
        return activeColumn;
    }

    /**
     * Returns whether the active sort direction is ascending.
     *
     * @return true when sorting ascending
     */
    public boolean isAscending() {
        return ascending;
    }
}
