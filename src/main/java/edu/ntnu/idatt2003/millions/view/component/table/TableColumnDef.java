package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import java.util.function.Supplier;
import javafx.geometry.HPos;

/**
 * Immutable metadata for a single table column.
 *
 * <p>Use the static factory methods for the four column variants:</p>
 * <ul>
 *   <li>{@link #of} static, non-sortable, no tooltip</li>
 *   <li>{@link #sortable(String, Object, double, HPos)} sortable, no tooltip</li>
 *   <li>{@link #sortable(String, Object, String, double, HPos)} sortable with an info tooltip</li>
 *   <li>{@link #spacer} non-sortable spacer with no label</li>
 * </ul>
 *
 * <p>Labels are resolved lazily via {@link #resolveLabel()} so that language
 * and currency changes are picked up automatically on every header refresh,
 * without requiring the column-def list to be recreated.</p>
 *
 * <p>Note: {@code labelSupplier} is a functional-interface field, so
 * {@code equals} and {@code hashCode} compare suppliers by reference.
 * {@link TableColumnDef} instances are never compared for equality in this
 * codebase, so this is intentional and safe.</p>
 *
 * @param <Column>      the sort-column enum type; use {@code Void} or {@code Object}
 *                      when the column is not sortable
 * @param labelSupplier supplier that resolves the display text for the column header;
 *                      called on every {@link #resolveLabel()} invocation
 * @param sortColumn    the enum constant that identifies this column for sorting,
 *                      or {@code null} if the column is not sortable
 * @param tooltipKey    the i18n key for an {@link InfoTooltip} icon next to the header label,
 *                      or {@code null} if no tooltip
 * @param percentWidth  the column width as a percentage of the table's total width
 * @param alignment     the horizontal alignment of all cells in this column
 */
public record TableColumnDef<Column>(
        Supplier<String> labelSupplier,
        Column sortColumn,
        String tooltipKey,
        double percentWidth,
        HPos alignment
) {

    /**
     * Creates a static, non-sortable column without a tooltip.
     *
     * @param <Column>     the sort-column type (inferred; not used for static columns)
     * @param labelKey     the i18n key for the column header label
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with no sort column and no tooltip
     */
    public static <Column> TableColumnDef<Column> of(
            String labelKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), null, null,
                percentWidth, alignment);
    }

    /**
     * Creates a static, non-sortable column with an info tooltip icon.
     *
     * <p>Use this when the column is not sortable but still needs an
     * {@link InfoTooltip} icon to explain its content (e.g. a sparkline trend column).</p>
     *
     * @param <Column>     the sort-column type (inferred; not used for static columns)
     * @param labelKey     the i18n key for the column header label
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with no sort column and an info tooltip
     */
    public static <Column> TableColumnDef<Column> of(
            String labelKey, String tooltipKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), null, tooltipKey,
                percentWidth, alignment);
    }

    /**
     * Creates a sortable column without a tooltip.
     *
     * @param <Column>     the sort-column enum type
     * @param labelKey     the i18n key for the column header label
     * @param sortColumn   the enum constant that identifies this column for sorting
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with a sort column and no tooltip
     */
    public static <Column> TableColumnDef<Column> sortable(
            String labelKey, Column sortColumn, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), sortColumn, null,
                percentWidth, alignment);
    }

    /**
     * Creates a sortable column with an info tooltip icon.
     *
     * <p>Use this when a column header needs both a sort button and an
     * {@link InfoTooltip} icon, for example a numeric column whose label
     * alone is not self-explanatory.</p>
     *
     * @param <Column>     the sort-column enum type
     * @param labelKey     the i18n key for the column header label
     * @param sortColumn   the enum constant that identifies this column for sorting
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with both a sort column and a tooltip
     */
    public static <Column> TableColumnDef<Column> sortable(
            String labelKey, Column sortColumn, String tooltipKey,
            double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), sortColumn, tooltipKey,
                percentWidth, alignment);
    }

    /**
     * Creates a non-sortable spacer column with no label and no tooltip.
     * Use this for action or padding columns that have no header text.
     *
     * @param <Column>     the sort-column type (inferred; not used for spacer columns)
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new {@link TableColumnDef} with an empty label and no sort column
     */
    public static <Column> TableColumnDef<Column> spacer(double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> "", null, null, percentWidth, alignment);
    }

    /**
     * Resolves and returns the current display label for this column.
     *
     * <p>Called by {@link TableHeaderRenderer} on every header refresh so that
     * i18n and currency changes are always reflected without recreating
     * the column-def list.</p>
     *
     * @return the resolved label text
     */
    public String resolveLabel() {
        return labelSupplier.get();
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
