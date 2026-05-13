package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Reusable sortable table component backed by a {@link GridPane}.
 *
 * <p>Manages column layout, row insertion, sort state and empty-state display.
 * Header-cell building and caching is delegated to {@link TableHeaderRenderer}
 * so this class stays focused on grid structure and the public table API.</p>
 *
 * <p>Cards retain responsibility for data fetching, filtering, sorting and
 * cell construction.</p>
 *
 * @param <Column> the sort-column enum type; use a wildcard or {@code Object}
 *                 when no column is sortable
 */
public class SortColumnTable<Column> {

    private static final double DEFAULT_HGAP = 20;

    private final Supplier<List<TableColumnDef<Column>>> columnSupplier;
    private final int columnCount;
    private final SortState<Column> sortState = new SortState<>();
    private final GridPane grid = new GridPane();
    private final TableHeaderRenderer<Column> headerRenderer = new TableHeaderRenderer<>(sortState);

    /**
     * Constructs a sortable table with default horizontal gap of 20px.
     *
     * @param columnSupplier supplier that returns the ordered column definitions
     * @throws NullPointerException if {@code columnSupplier} is null
     */
    public SortColumnTable(Supplier<List<TableColumnDef<Column>>> columnSupplier) {
        this(columnSupplier, DEFAULT_HGAP);
    }

    /**
     * Constructs a sortable table with the given horizontal gap between columns.
     *
     * @param columnSupplier supplier that returns the ordered column definitions
     * @param gap            the horizontal gap between columns in pixels
     * @throws NullPointerException if {@code columnSupplier} is null
     */
    public SortColumnTable(Supplier<List<TableColumnDef<Column>>> columnSupplier, double gap) {
        this.columnSupplier = Objects.requireNonNull(columnSupplier, "columnSupplier cannot be null");
        List<TableColumnDef<Column>> initial = columnSupplier.get();
        this.columnCount = initial.size();
        grid.setHgap(gap);
        grid.setMinWidth(0);
        grid.setMaxWidth(Double.MAX_VALUE);
        configureColumns(initial);
    }

    // -------------------------------------------------------------------------
    // Grid management
    // -------------------------------------------------------------------------

    /**
     * Clears all nodes from the grid, including header and data rows.
     * Call this at the start of every refresh before calling {@link #refreshHeader}.
     */
    public void clearRows() {
        grid.getChildren().clear();
    }

    /**
     * Builds or refreshes the header row into row 0 of the grid.
     *
     * <p>Delegates to {@link TableHeaderRenderer} which caches header cells and
     * updates label text, sort indicators and button actions on each call.</p>
     *
     * @param onChanged callback invoked after any sort-state change so the
     *                  owning card can trigger a data refresh
     */
    public void refreshHeader(Runnable onChanged) {
        headerRenderer.renderInto(grid, columnSupplier.get(), onChanged);
    }

    /**
     * Adds a standard data row where each supplied node maps to the next
     * column in order, starting from column 0.
     *
     * @param rowIndex the grid row to write to (row 0 is reserved for the header)
     * @param cells    the nodes to place, one per column
     */
    public void addRow(int rowIndex, Node... cells) {
        for (int i = 0; i < cells.length; i++) {
            grid.add(cells[i], i, rowIndex);
        }
    }

    /**
     * Adds a single cell at an explicit column and row position.
     *
     * <p>Use this when a cell needs constraints that {@link #addRow} cannot express.</p>
     *
     * @param cell   the node to add
     * @param column the zero-based column index
     * @param row    the zero-based row index
     */
    public void addCell(Node cell, int column, int row) {
        grid.add(cell, column, row);
    }

    /**
     * Adds a node that spans the full table width at the given row.
     *
     * @param node the node to add
     * @param row  the zero-based row index
     */
    public void addFullWidthRow(Node node, int row) {
        GridPane.setColumnSpan(node, columnCount);
        grid.add(node, 0, row);
    }

    /**
     * Renders a centred empty-state message spanning all columns on row 1.
     *
     * @param message the localised empty-state text to display
     */
    public void renderEmptyState(String message) {
        TableCells.renderEmptyState(grid, message, columnCount);
    }

    /**
     * Sets the minimum height of the table grid.
     *
     * @param height the minimum height in pixels
     */
    public void setMinHeight(double height) {
        grid.setMinHeight(height);
    }

    /**
     * Returns this table as a {@link Node} for embedding in a parent layout.
     *
     * <p>The underlying {@link GridPane} is not exposed directly to prevent
     * callers from bypassing the controlled insertion API.</p>
     *
     * @return the table node
     */
    public Node asNode() {
        return grid;
    }

    // -------------------------------------------------------------------------
    // Sort state
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link SortState} owned by this table.
     *
     * <p>Pass this to a {@code *Sort} instance's {@code applySort} method so
     * sort direction and column selection are driven by what the user clicked
     * in the header.</p>
     *
     * @return the sort state
     */
    public SortState<Column> getSortState() {
        return sortState;
    }

    /**
     * Clears the active sort column and direction, returning the table to
     * its unsorted state.
     */
    public void clearSort() {
        sortState.clear();
    }

    /**
     * Returns whether a sort column is currently active.
     *
     * @return {@code true} if a sort column is active, {@code false} otherwise
     */
    public boolean isSortActive() {
        return sortState.hasActiveSort();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Configures the grid's column constraints from the initial column definitions.
     * Called once during construction; widths and alignments are stable across
     * language changes.
     *
     * @param cols the initial column definitions
     */
    private void configureColumns(List<TableColumnDef<Column>> cols) {
        for (TableColumnDef<Column> col : cols) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            grid.getColumnConstraints().add(cc);
        }
    }
}
