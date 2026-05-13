package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import javafx.geometry.HPos;

/**
 * Immutable metadata for a single table column.
 *
 * <p>Use the static factory methods for the three common column variants:</p>
 * <ul>
 *   <li>{@link #of} static, non-sortable, no tooltip</li>
 *   <li>{@link #sortable} sortable, no tooltip</li>
 *   <li>{@link #withTooltip} static with an info tooltip icon</li>
 * </ul>
 *
 * @param <Column>     the sort-column enum type; use {@code Void} or {@code Object}
 *                     when the column is not sortable
 * @param label        the resolved display text shown in the column header
 * @param sortColumn   the enum constant that identifies this column for sorting,
 *                     or {@code null} if the column is not sortable
 * @param tooltipKey   the i18n key for an {@link InfoTooltip} icon next to the header label,
 *                     or {@code null} if no tooltip
 * @param percentWidth the column width as a percentage of the table's total width
 * @param alignment    the horizontal alignment of all cells in this column
 */
public record TableColumnDef<Column>(
        String label,
        Column sortColumn,
        String tooltipKey,
        double percentWidth,
        HPos alignment
) {

    /**
     * Creates a static, non-sortable column without a tooltip.
     *
     * @param <Column>     the sort-column type (inferred; not used for static columns)
     * @param label        the resolved display text
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with no sort column and no tooltip
     */
    public static <Column> TableColumnDef<Column> of(
            String label, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(label, null, null, percentWidth, alignment);
    }

    /**
     * Creates a sortable column without a tooltip.
     *
     * @param <Column>     the sort-column enum type
     * @param label        the resolved display text
     * @param sortColumn   the enum constant that identifies this column for sorting
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with a sort column and no tooltip
     */
    public static <Column> TableColumnDef<Column> sortable(
            String label, Column sortColumn, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(label, sortColumn, null, percentWidth, alignment);
    }

    /**
     * Creates a static, non-sortable column with an info tooltip icon.
     *
     * @param <Column>     the sort-column type (inferred; not used for static columns)
     * @param label        the resolved display text
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with a tooltip and no sort column
     */
    public static <Column> TableColumnDef<Column> withTooltip(
            String label, String tooltipKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(label, null, tooltipKey, percentWidth, alignment);
    }

    /**
     * Creates a sortable column with an info tooltip icon.
     *
     * <p>Use this when a column header needs both a sort button and an
     * {@link InfoTooltip} icon — for example a numeric column whose label
     * alone is not self-explanatory.</p>
     *
     * @param <Column>     the sort-column enum type
     * @param label        the resolved display text
     * @param sortColumn   the enum constant that identifies this column for sorting
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with both a sort column and a tooltip
     */
    public static <Column> TableColumnDef<Column> sortableWithTooltip(
            String label, Column sortColumn, String tooltipKey,
            double percentWidth, HPos alignment) {
        return new TableColumnDef<>(label, sortColumn, tooltipKey, percentWidth, alignment);
    }

    /**
     * Returns true if this column supports sorting.
     *
     * @return {@code true} when {@code sortColumn} is non-null
     */
    public boolean isSortable() {
        return sortColumn != null;
    }

    /**
     * Returns true if this column has an info tooltip icon.
     *
     * @return {@code true} when {@code tooltipKey} is non-null
     */
    public boolean hasTooltip() {
        return tooltipKey != null;
    }
}
