package edu.ntnu.idatt2003.millions.view.component.table;

import javafx.scene.Node;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A column-keyed row builder for {@link SortColumnTable}.
 *
 * <p>Maps column enum constants to their JavaFX nodes so that row rendering
 * is independent of column position. Column positions are resolved by
 * {@link SortColumnTable} using the column definitions returned by the table's
 * column supplier, so reordering columns only requires changing
 * {@code getColumnDefs()} — not the renderer.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * table.addRow(rowIndex, RowCells.<MySort.SortColumn>builder()
 *     .put(MySort.SortColumn.NAME, nameLabel)
 *     .put(MySort.SortColumn.VALUE, valueLabel)
 *     .put(MySort.SortColumn.ACTION, actionButton)
 *     .build());
 * }</pre>
 *
 * @param <Column> the column enum type used by the owning table
 */
public final class RowCells<Column> {

    private final Map<Column, Node> cells = new LinkedHashMap<>();

    private RowCells() {
    }

    /**
     * Creates a new empty {@code RowCells} builder.
     *
     * @param <Column> the column enum type
     * @return a new builder instance
     */
    public static <Column> RowCells<Column> builder() {
        return new RowCells<>();
    }

    /**
     * Maps the given column key to the given node.
     *
     * @param column the column enum constant identifying this cell's column
     * @param node   the JavaFX node to place in that column
     * @return this builder, for fluent chaining
     * @throws NullPointerException if either argument is {@code null}
     */
    public RowCells<Column> put(Column column, Node node) {
        Objects.requireNonNull(column, "column must not be null");
        Objects.requireNonNull(node, "node must not be null");
        cells.put(column, node);
        return this;
    }

    /**
     * Returns the node mapped to the given column key, or {@code null} if none was registered.
     *
     * <p>Package-private: only {@link SortColumnTable} should call this when
     * placing cells into the grid.</p>
     *
     * @param column the column key to look up
     * @return the mapped node, or {@code null} if the column was not added to this builder
     */
    Node get(Column column) {
        return cells.get(column);
    }
}
