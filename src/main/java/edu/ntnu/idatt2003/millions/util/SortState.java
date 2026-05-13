package edu.ntnu.idatt2003.millions.util;

import java.util.Objects;

/**
 * Reusable state holder for sortable table columns with primary and secondary sort support.
 *
 * @param <T> the column identifier type
 */
public class SortState<T> {

    private T activeColumn;
    private boolean ascending = true;

    private T secondaryColumn;
    private boolean secondaryAscending = true;

    /**
     * Activates or toggles sorting for the given column.
     * If the column is already the active primary, its direction is toggled.
     * Otherwise the current primary cascades to secondary and the new column
     * becomes the primary in ascending order.
     *
     * @param column the column to sort by
     * @throws NullPointerException if column is null
     */
    public void toggle(T column) {
        Objects.requireNonNull(column, "Column cannot be null");
        if (Objects.equals(activeColumn, column)) {
            ascending = !ascending;
        } else {
            secondaryColumn = activeColumn;
            secondaryAscending = ascending;
            activeColumn = column;
            ascending = true;
        }
    }

    /**
     * Clears both the primary and secondary sort.
     */
    public void clear() {
        activeColumn = null;
        ascending = true;
        secondaryColumn = null;
        secondaryAscending = true;
    }

    /**
     * Returns whether the given column is the active primary sort column.
     *
     * @param column the column to check
     * @return true if the column is the primary active column
     */
    public boolean isActive(T column) {
        return Objects.equals(activeColumn, column);
    }

    /**
     * Returns whether the given column is the active secondary sort column.
     *
     * @param column the column to check
     * @return true if the column is the secondary active column
     */
    public boolean isSecondaryActive(T column) {
        return Objects.equals(secondaryColumn, column);
    }

    /**
     * Returns whether any primary column is actively sorted.
     *
     * @return true if a primary sort is active
     */
    public boolean hasActiveSort() {
        return activeColumn != null;
    }

    /**
     * Returns whether a secondary sort column is set.
     *
     * @return true if a secondary sort is active
     */
    public boolean hasSecondarySort() {
        return secondaryColumn != null;
    }

    /**
     * Returns the active primary column.
     *
     * @return the primary column, or null if no sort is active
     */
    public T getActiveColumn() {
        return activeColumn;
    }

    /**
     * Returns the active secondary column.
     *
     * @return the secondary column, or null if no secondary sort is active
     */
    public T getSecondaryColumn() {
        return secondaryColumn;
    }

    /**
     * Returns whether the primary sort direction is ascending.
     *
     * @return true when sorting ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * Returns whether the secondary sort direction is ascending.
     *
     * @return true when secondary sorting ascending
     */
    public boolean isSecondaryAscending() {
        return secondaryAscending;
    }
}
