package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import java.util.function.Supplier;
import javafx.geometry.HPos;

/**
 * Immutable metadata for a single table column.
 *
 * <p>Created via the static factories, which vary along three axes: sortable vs.
 * non-sortable, with vs. without a {@link RowCells} column key, and with vs.
 * without an {@link InfoTooltip}; {@link #spacer} variants carry no header label.
 * Labels resolve lazily via {@link #resolveLabel()} so that language and currency
 * changes are picked up on every header refresh without recreating the list.</p>
 *
 * @param <Column>      the sort-column enum type
 * @param labelSupplier supplier that resolves the display text for the column header;
 *                      called on every {@link #resolveLabel()} invocation
 * @param columnKey     the enum constant that uniquely identifies this column;
 *                      used as the key in {@link RowCells} and, for sortable columns,
 *                      passed to the sort comparator
 * @param isSortable    {@code true} if this column supports ascending/descending sort
 * @param tooltipKey    the i18n key for an {@link InfoTooltip} icon next to the header label,
 *                      or {@code null} if no tooltip
 * @param percentWidth  the column width as a percentage of the table's total width
 * @param alignment     the horizontal alignment of all cells in this column
 */
public record TableColumnDef<Column>(
        Supplier<String> labelSupplier,
        Column columnKey,
        boolean isSortable,
        String tooltipKey,
        double percentWidth,
        HPos alignment
) {
    /**
     * Creates a non-sortable column with a column key and no tooltip.
     *
     * <p>Use this when the column is not sortable but requires a key for
     * {@link RowCells}-based row building.</p>
     *
     * @param <Column>     the column enum type
     * @param columnKey    the enum constant that uniquely identifies this column
     * @param labelKey     the i18n key for the column header label
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new non-sortable {@link TableColumnDef} with the given column key
     */
    public static <Column> TableColumnDef<Column> nonSortable(
            Column columnKey, String labelKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), columnKey, false, null,
                percentWidth, alignment);
    }

    /**
     * Creates a non-sortable column with a column key and an info tooltip.
     *
     * <p>Use this when the column is not sortable but needs both a key for
     * {@link RowCells}-based row building and an {@link InfoTooltip} icon in its header.</p>
     *
     * @param <Column>     the column enum type
     * @param columnKey    the enum constant that uniquely identifies this column
     * @param labelKey     the i18n key for the column header label
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new non-sortable {@link TableColumnDef} with the given column key and tooltip
     */
    public static <Column> TableColumnDef<Column> nonSortable(
            Column columnKey, String labelKey, String tooltipKey,
            double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), columnKey, false,
                tooltipKey, percentWidth, alignment);
    }

    /**
     * Creates a sortable column without a tooltip.
     *
     * @param <Column>     the sort-column enum type
     * @param labelKey     the i18n key for the column header label
     * @param columnKey    the enum constant that identifies this column for sorting and
     *                     for {@link RowCells} lookup
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new sortable {@link TableColumnDef}
     */
    public static <Column> TableColumnDef<Column> sortable(
            String labelKey, Column columnKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), columnKey, true, null,
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
     * @param columnKey    the enum constant that identifies this column for sorting and
     *                     for {@link RowCells} lookup
     * @param tooltipKey   the i18n key for the tooltip content
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new sortable {@link TableColumnDef} with a tooltip
     */
    public static <Column> TableColumnDef<Column> sortable(
            String labelKey, Column columnKey, String tooltipKey,
            double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> LanguageManager.get(labelKey), columnKey, true,
                tooltipKey, percentWidth, alignment);
    }

    /**
     * Creates a non-sortable spacer column with a column key but no header label or tooltip.
     *
     * <p>Use this for action or chevron columns that need a {@link RowCells} key for
     * {@link RowCells}-based row building, yet should render no header text - for
     * example a buy/sell button group or a details chevron.</p>
     *
     * @param <Column>     the column enum type
     * @param columnKey    the enum constant that uniquely identifies this column
     * @param percentWidth the column width as a percentage of total table width
     * @param alignment    the horizontal alignment for cells in this column
     * @return a new keyed spacer {@link TableColumnDef} with no header label
     */
    public static <Column> TableColumnDef<Column> spacer(
            Column columnKey, double percentWidth, HPos alignment) {
        return new TableColumnDef<>(() -> "", columnKey, false, null, percentWidth, alignment);
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
     * Returns {@code true} if this column has a unique key suitable for use
     * as a {@link RowCells} entry.
     *
     * @return {@code true} when {@code columnKey} is non-null
     */
    public boolean hasColumnKey() {
        return columnKey != null;
    }

    /**
     * Returns {@code true} if this column has an info tooltip icon.
     *
     * @return {@code true} when {@code tooltipKey} is non-null
     */
    public boolean hasTooltip() {
        return tooltipKey != null;
    }
}
