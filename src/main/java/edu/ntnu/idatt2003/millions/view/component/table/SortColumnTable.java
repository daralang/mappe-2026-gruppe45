package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.keyboard.ArrowKeyNavigator;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.geometry.HPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Reusable sortable table component backed by a {@link GridPane}.
 *
 * <p>Manages column layout, row insertion, sort state and the empty state; header
 * cells are delegated to {@link TableHeaderRenderer}, and cards keep responsibility
 * for data fetching, filtering, sorting and cell construction.</p>
 *
 * <p>Keyboard navigation is delegated to an internal {@link ArrowKeyNavigator}; the
 * event filter is installed lazily on the first {@link #addSelectableRow} call and
 * removed by {@link #clearRows()}, so it never outlives the rows it serves.</p>
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
    private final List<SelectableRow> selectableRows = new ArrayList<>();
    private final RowHighlighter rowHighlighter = new RowHighlighter();
    private boolean rowFilterInstalled = false;
    private final javafx.event.EventHandler<KeyEvent> rowNavigationHandler = this::handleRowNavigation;
    private final ArrowKeyNavigator rowNavigator = new ArrowKeyNavigator(
            ArrowKeyNavigator.Orientation.VERTICAL,
            selectableRows::size,
            idx -> selectableRows.get(idx).focusAnchor().requestFocus(),
            idx -> selectableRows.get(idx).onEnter().run(),
            false
    );

    /** Metadata for a keyboard-navigable data row. */
    private record SelectableRow(int gridRow, Node focusAnchor, Runnable onEnter) {}

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
        grid.addEventHandler(MouseEvent.MOUSE_MOVED, e -> rowHighlighter.onMouseMoved(e.getX(), e.getY()));
        grid.addEventHandler(MouseEvent.MOUSE_EXITED, e -> rowHighlighter.clearHover());
        configureColumns(initial);
    }

    /**
     * Clears all nodes from the grid, including header and data rows.
     * Also clears the selectable-row registry and removes the row-navigation
     * event filter so it does not linger when there are no navigable rows.
     * Call this at the start of every refresh before calling {@code refreshHeader}.
     */
    public void clearRows() {
        grid.getChildren().clear();
        selectableRows.clear();
        rowHighlighter.clear();
        if (rowFilterInstalled) {
            grid.removeEventFilter(KeyEvent.KEY_PRESSED, rowNavigationHandler);
            rowFilterInstalled = false;
        }
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
     * Adds a standard data row where each supplied node maps to the next column in order,
     * starting from column 0. The horizontal alignment from each column's
     * {@link ColumnConstraints} is applied explicitly to the cell node so that data rows
     * align consistently with the header regardless of CSS defaults on the node type.
     *
     * @param rowIndex the grid row to write to (row 0 is reserved for the header)
     * @param cells    the nodes to place, one per column
     */
    public void addRow(int rowIndex, Node... cells) {
        List<ColumnConstraints> constraints = grid.getColumnConstraints();
        for (int i = 0; i < cells.length; i++) {
            grid.add(cells[i], i, rowIndex);
            if (i < constraints.size()) {
                HPos alignment = constraints.get(i).getHalignment();
                if (alignment != null) {
                    GridPane.setHalignment(cells[i], alignment);
                }
            }
        }
    }

    /**
     * Adds a standard data row using a {@link RowCells} column-keyed map.
     * Iterates the current column definitions in order.
     *
     * <p>With this overload, moving a column only requires reordering the entry in
     * {@code getColumnDefs()}, the renderer does not need to change. Keyless columns
     * (spacers) are skipped; every column that declares a key must have a matching
     * cell, otherwise this fails fast rather than rendering a silently empty column.</p>
     *
     * @param rowIndex the grid row to write to (row 0 is reserved for the header)
     * @param cells    the column-keyed cell map produced by {@link RowCells#builder()}
     * @throws IllegalStateException if a keyed column has no corresponding cell in {@code cells}
     */
    public void addRow(int rowIndex, RowCells<Column> cells) {
        List<TableColumnDef<Column>> cols = columnSupplier.get();

        Region rowBackground = new Region();
        rowBackground.getStyleClass().add("table-row-bg");
        rowBackground.setMouseTransparent(true);
        rowBackground.setMaxWidth(Double.MAX_VALUE);
        rowBackground.setMaxHeight(Double.MAX_VALUE);
        GridPane.setColumnSpan(rowBackground, columnCount);
        grid.add(rowBackground, 0, rowIndex);
        rowHighlighter.register(rowIndex, rowBackground);

        for (int i = 0; i < cols.size(); i++) {
            TableColumnDef<Column> col = cols.get(i);
            if (!col.hasColumnKey()) {
                continue;
            }
            Node node = cells.get(col.columnKey());
            if (node == null) {
                throw new IllegalStateException(
                        "No cell provided for keyed column: " + col.columnKey());
            }
            grid.add(node, i, rowIndex);
            GridPane.setHalignment(node, col.alignment());
        }
    }

    /**
     * Adds a data row and registers it for UP/DOWN keyboard navigation.
     *
     * <p>When this row has focus (or a descendant has focus) and the user
     * presses UP or DOWN, the internal {@link ArrowKeyNavigator} moves focus
     * to the adjacent row's {@code focusAnchor}. ENTER calls {@code onEnter}.
     * The grid event filter is attached lazily on the first call and removed
     * by {@link #clearRows()}.</p>
     *
     * @param gridRow     the grid row to write to (row 0 is reserved for the header)
     * @param focusAnchor the node that receives focus when navigating to this row
     * @param onEnter     action invoked when the user presses Enter on this row
     * @param cells       the nodes to place, one per column
     */
    public void addSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter, Node... cells) {
        addRow(gridRow, cells);
        registerSelectableRow(gridRow, focusAnchor, onEnter);
    }

    /**
     * Adds a {@link RowCells}-based data row and registers it for UP/DOWN/ENTER navigation.
     * Like {@link #addSelectableRow(int, Node, Runnable, Node...)} but builds the row from a
     * column-keyed map via {@link #addRow(int, RowCells)}, so column order stays owned by the
     * column definitions.
     *
     * @param gridRow     the grid row to write to (row 0 is reserved for the header)
     * @param focusAnchor the node focused when navigating to this row
     * @param onEnter     action invoked on Enter
     * @param cells       the column-keyed cells from {@link RowCells#builder()}
     */
    public void addSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter, RowCells<Column> cells) {
        addRow(gridRow, cells);
        registerSelectableRow(gridRow, focusAnchor, onEnter);
    }

    /**
     * Registers an already-inserted row for keyboard navigation, installing the
     * row-navigation event filter on the first call. Shared by both
     * {@code addSelectableRow} overloads; the filter is removed by {@link #clearRows()}.
     *
     * @param gridRow     the grid row the cells were written to
     * @param focusAnchor the node focused when navigating to this row
     * @param onEnter     action invoked on Enter
     */
    private void registerSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter) {
        selectableRows.add(new SelectableRow(gridRow, focusAnchor, onEnter));
        rowHighlighter.bindFocus(gridRow, focusAnchor);
        if (!rowFilterInstalled) {
            grid.addEventFilter(KeyEvent.KEY_PRESSED, rowNavigationHandler);
            rowFilterInstalled = true;
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
     * Returns the grid column index of the column with the given key, by scanning
     * the current column definitions. Lets callers position cells (e.g. a separate
     * total row) by column key instead of a hard-coded index, so the layout has a
     * single source of truth in the column definitions.
     *
     * @param key the column key to locate
     * @return the zero-based grid column index
     * @throws IllegalArgumentException if no column has the given key
     */
    public int columnIndex(Column key) {
        List<TableColumnDef<Column>> cols = columnSupplier.get();
        for (int i = 0; i < cols.size(); i++) {
            if (key.equals(cols.get(i).columnKey())) {
                return i;
            }
        }
        throw new IllegalArgumentException("No column with key: " + key);
    }

    /**
     * Handles UP/DOWN/ENTER when focus is inside a selectable row.
     *
     * <p>Delegates movement and confirmation to {@link ArrowKeyNavigator}.
     * Before each key event the navigator's index is silently synchronised
     * to the actually focused row via {@link ArrowKeyNavigator#syncIndex},
     * so navigation is correct even when focus arrives via Tab rather than
     * a previous arrow-key press.</p>
     */
    private void handleRowNavigation(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != KeyCode.UP && code != KeyCode.DOWN && code != KeyCode.ENTER) {
            return;
        }
        Node focused = grid.getScene() != null ? grid.getScene().getFocusOwner() : null;
        int dataIdx = findFocusedDataRowIndex(focused);
        if (dataIdx < 0) {
            return;
        }
        rowNavigator.syncIndex(dataIdx);
        if (rowNavigator.navigate(event)) {
            event.consume();
        }
    }

    /**
     * Walks up the scene-graph from {@code focused} to find the direct grid child,
     * then maps its grid-row to a selectable-row index.
     *
     * @param focused the current focus owner; may be {@code null}
     * @return the zero-based index in {@code selectableRows}, or {@code -1} if not found
     */
    private int findFocusedDataRowIndex(Node focused) {
        Node node = focused;
        while (node != null && node != grid) {
            if (node.getParent() == grid) {
                Integer row = GridPane.getRowIndex(node);
                int gridRow = (row != null) ? row : 0;
                for (int i = 0; i < selectableRows.size(); i++) {
                    if (selectableRows.get(i).gridRow() == gridRow) {
                        return i;
                    }
                }
                return -1;
            }
            node = node.getParent();
        }
        return -1;
    }

    /**
     * Configures the grid's column constraints from the initial column definitions.
     * Called once during construction; widths and alignments are stable across
     * language changes.
     *
     * @param cols the initial column definitions
     */
    private void configureColumns(List<TableColumnDef<Column>> cols) {
        applyColumnConstraints(grid, cols);
    }

    /**
     * Applies each column definition's percentage width and horizontal alignment
     * as a {@link ColumnConstraints} on the given grid, in order.
     *
     * <p>Shared so that a card's separate total-row grid can be given exactly the
     * same column geometry as the table, derived from the single column-definition
     * source rather than duplicated per caller.</p>
     *
     * @param grid the grid to configure
     * @param cols the ordered column definitions
     * @param <C>  the column key type
     */
    public static <C> void applyColumnConstraints(GridPane grid, List<TableColumnDef<C>> cols) {
        for (TableColumnDef<C> col : cols) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            grid.getColumnConstraints().add(cc);
        }
    }
}
