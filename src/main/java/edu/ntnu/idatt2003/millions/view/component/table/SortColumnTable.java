package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Reusable sortable table component backed by a {@link GridPane}.
 *
 * <p>Manages column layout, row insertion, sort state, empty-state display and an
 * optional clear-sort button. Header-cell building and caching is delegated to
 * {@link TableHeaderRenderer} so this class stays focused on grid structure and
 * the public table API.</p>
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
    private double[] leftInsets;

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

    /**
     * Clears all nodes from the grid, including header and data rows.
     * Call this at the start of every refresh before calling {@link #refreshHeader}.
     */
    public void clearRows() {
        grid.getChildren().clear();
    }

    /**
     * Builds or refreshes the header row into row 0 of the grid, with sort buttons
     * always enabled.
     *
     * @param onChanged callback invoked after any sort-state change so the
     *                  owning card can trigger a data refresh
     */
    public void refreshHeader(Runnable onChanged) {
        refreshHeader(onChanged, true);
    }

    /**
     * Builds or refreshes the header row into row 0 of the grid.
     *
     * @param onChanged callback invoked after any sort-state change so the
     *                  owning card can trigger a data refresh
     * @param sortable  {@code true} to allow sorting; {@code false} to disable all
     *                  sort buttons (e.g. when the filtered result set is empty)
     */
    public void refreshHeader(Runnable onChanged, boolean sortable) {
        headerRenderer.renderInto(grid, columnSupplier.get(), onChanged, sortable);
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
            if (leftInsets != null && i < leftInsets.length && leftInsets[i] > 0) {
                GridPane.setMargin(cells[i], new Insets(0, 0, 0, leftInsets[i]));
            }
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
     * Creates a clear-sort button managed by the internal {@link TableHeaderRenderer}
     * and returns it for placement in the owning card's layout.
     *
     * <p>The button is hidde. until a sort becomes active. On every call to {@link #refreshHeader}
     * the button's text and visibility are updated automatically, so the owning card does not need to
     * manage this state manually.</p>
     *
     * @param labelSupplier supplier that returns the current button label, called on
     *                      every header refresh so i18n updates are picked up automatically
     * @param onClear       callback invoked after the sort is cleared, typically
     *                      {@code this::refresh} in the owning card
     * @return the configured button, ready to place in the card's search row
     * @throws NullPointerException if {@code labelSupplier} or {@code onClear} is null
     */
    public Button createClearSortButton(Supplier<String> labelSupplier, Runnable onClear) {
        return headerRenderer.createClearSortButton(labelSupplier, onClear);
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

    /**
     * Configures the grid's column constraints from the initial column definitions.
     * Called once during construction; widths and alignments are stable across
     * language changes.
     *
     * @param cols the initial column definitions
     */
    private void configureColumns(List<TableColumnDef<Column>> cols) {
        leftInsets = new double[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            TableColumnDef<Column> col = cols.get(i);
            leftInsets[i] = col.leftInset();
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            grid.getColumnConstraints().add(cc);
        }
    }
}
