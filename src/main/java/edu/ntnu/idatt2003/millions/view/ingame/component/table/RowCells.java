package edu.ntnu.idatt2003.millions.view.ingame.component.table;

import javafx.scene.Node;
import java.util.Collection;
import java.util.Collections;
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
 * {@code getColumnDefs()} - not the renderer.</p>
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
     * @param column the column key to look up
     * @return the mapped node, or {@code null} if the column was not added to this builder
     */
    Node get(Column column) {
        return cells.get(column);
    }

    /**
     * Returns an unmodifiable view of all nodes in insertion order.
     *
     * <p>Used by {@link SortColumnTable} to wire {@code MOUSE_CLICKED} handlers
     * on non-{@link javafx.scene.control.ButtonBase} nodes so that clicking anywhere
     * on a data row activates the row's action without conflicting with interactive
     * cell controls.</p>
     *
     * @return an unmodifiable collection of all nodes in this row, in insertion order
     */
    Collection<Node> nodes() {
        return Collections.unmodifiableCollection(cells.values());
    }

    /**
     * Returns the first node in insertion order, or {@code null} if no cells were added.
     *
     * <p>Used by {@link SortColumnTable}-based cards as the default keyboard-focus
     * anchor for a row when no more specific anchor is supplied. Insertion order is
     * preserved by the backing {@link LinkedHashMap}.</p>
     *
     * @return the first inserted node, or {@code null} when this builder is empty
     */
    public Node firstNode() {
        return cells.values().stream().findFirst().orElse(null);
    }
}
