package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.SortState;

import java.util.Comparator;
import java.util.List;

/**
 * Abstract base class for sort-logic providers used with {@link SortColumnTable}.
 *
 * <p>Implements the Template Method pattern: {@link #applySort} contains the
 * general algorithm for primary/secondary sorting, while subclasses supply
 * column-specific comparators via {@link #buildComparator}.</p>
 *
 * @param <T> the type of item being sorted
 * @param <C> the enum type identifying sort columns
 */
public abstract class SortProvider<T, C> {

    /**
     * Sorts the given list in-place according to the active column and direction
     * in the provided {@link SortState}. When a secondary sort column is active,
     * ties in the primary comparator are broken by the secondary via
     * {@link Comparator#thenComparing}. No-op when no sort column is active.
     *
     * @param items the list to sort in-place
     * @param state the sort state read from the owning {@link SortColumnTable}
     */
    public void applySort(List<T> items, SortState<C> state) {
        if (!state.hasActiveSort()) return;

        Comparator<T> comparator = buildComparator(state.getActiveColumn());
        if (!state.isAscending()) comparator = comparator.reversed();

        if (state.hasSecondarySort()) {
            Comparator<T> secondary = buildComparator(state.getSecondaryColumn());
            if (!state.isSecondaryAscending()) secondary = secondary.reversed();
            comparator = comparator.thenComparing(secondary);
        }

        items.sort(comparator);
    }

    /**
     * Builds a comparator for the given sort column.
     * Implemented by each subclass to define column-specific ordering.
     *
     * @param column the column to sort by
     * @return a comparator for the given column
     */
    protected abstract Comparator<T> buildComparator(C column);
}
