package edu.ntnu.idatt2003.millions.view.ingame.component.table;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A persistent total/summary row rendered below a {@link SortColumnTable}.
 *
 * <p>Lives in its own {@link GridPane} that mirrors the table's column constraints, so
 * total cells align under their columns regardless of which page is active. A card
 * rebuilds the row by calling {@link #beginRebuild()} and then {@link #put(Object, Node)}
 * for each total cell, keyed by the same column enum constant used in the table - so the
 * layout has a single source of truth in the column definitions </p>
 *
 * @param <Column> the column enum type shared with the owning {@link SortColumnTable}
 */
public final class TableTotalRow<Column> {

    private static final double HGAP = 20;

    private final SortColumnTable<Column> table;
    private final Supplier<List<TableColumnDef<Column>>> columnDefs;
    private final GridPane grid = new GridPane();

    /**
     * Creates a total row bound to the given table and column definitions.
     *
     * @param table      the table whose column indices position the total cells
     * @param columnDefs supplier of the ordered column definitions, used for the column
     *                   geometry and the divider's column span
     * @throws NullPointerException if either argument is null
     */
    public TableTotalRow(SortColumnTable<Column> table,
                  Supplier<List<TableColumnDef<Column>>> columnDefs) {
        this.table = Objects.requireNonNull(table, "table cannot be null");
        this.columnDefs = Objects.requireNonNull(columnDefs, "columnDefs cannot be null");
        grid.setHgap(HGAP);
        SortColumnTable.applyColumnConstraints(grid, columnDefs.get());
        setVisible(false);
    }

    /**
     * Returns the total row as a {@link Node} for embedding in the card's layout.
     *
     * @return the total-row node
     */
    public Node asNode() {
        return grid;
    }

    /**
     * Shows or hides the total row.
     *
     * @param visible {@code true} to show, {@code false} to hide and unmanage
     */
    public void setVisible(boolean visible) {
        grid.setVisible(visible);
        grid.setManaged(visible);
    }

    /**
     * Clears the row and lays down the top divider spanning all columns, ready for
     * fresh total cells to be added via {@link #put(Object, Node)}.
     */
    public void beginRebuild() {
        grid.getChildren().clear();
        Region divider = new Region();
        divider.getStyleClass().add("table-total-divider");
        GridPane.setColumnSpan(divider, columnDefs.get().size());
        grid.add(divider, 0, 0);
    }

    /**
     * Places a total cell under the column identified by {@code columnKey}.
     *
     * @param columnKey the column whose grid index positions the cell
     * @param cell      the cell node to place on the total row
     * @throws IllegalArgumentException if no column has the given key
     */
    public void put(Column columnKey, Node cell) {
        grid.add(cell, table.columnIndex(columnKey), 1);
    }
}
