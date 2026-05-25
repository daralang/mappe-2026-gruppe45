package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.keyboard.ArrowKeyNavigator;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.geometry.VPos;
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
 * <p>Data rows form a keyboard grid: each row's focus anchor is focus-traversable
 * and shows a focus highlight. UP/DOWN move focus between rows and scroll the
 * focused row into view, consuming the keys so they navigate rows rather than
 * scrolling the surrounding view. At the first row (UP) and last row (DOWN) the
 * key is left unconsumed so it bubbles to the enclosing scroll pane, letting the
 * page scroll past the table.</p>
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
    private final RowHighlighter rowHighlighter = new RowHighlighter();
    private final RowScroller rowScroller = new RowScroller();
    private final List<NavigableRow> rows = new ArrayList<>();
    private final ArrowKeyNavigator rowNavigator = new ArrowKeyNavigator(
            rows::size,
            index -> {
                Node anchor = rows.get(index).anchor();
                anchor.requestFocus();
                rowScroller.ensureVisible(anchor);
            },
            index -> rows.get(index).onEnter().run(),
            false);

    /** A keyboard-navigable data row: its focus anchor and its activation action. */
    private record NavigableRow(Node anchor, Runnable onEnter) {}

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
     * Also clears the row-anchor registry used for keyboard navigation.
     * Call this at the start of every refresh before calling {@code refreshHeader}.
     */
    public void clearRows() {
        grid.getChildren().clear();
        rowHighlighter.clear();
        rows.clear();
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
            GridPane.setValignment(node, VPos.CENTER);
            GridPane.setFillHeight(node, false);
        }
    }

    /**
     * Adds a {@link RowCells}-based data row and makes it keyboard-reachable.
     *
     * @param gridRow     the grid row to write to (row 0 is reserved for the header)
     * @param focusAnchor the node focused when this row is reached
     * @param onEnter     action invoked on Enter or Space
     * @param cells       the column-keyed cells from {@link RowCells#builder()}
     */
    public void addSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter, RowCells<Column> cells) {
        addRow(gridRow, cells);
        registerSelectableRow(gridRow, focusAnchor, onEnter);
    }

    /**
     * Wires an already-inserted row into the keyboard grid: registers it as a
     * {@link NavigableRow}, binds the focus highlight, scrolls it into view on
     * focus, and delegates UP/DOWN/Enter/Space to the shared {@link ArrowKeyNavigator}.
     * At the first row (UP) and last row (DOWN) the key is left unconsumed so it
     * bubbles to the enclosing scroll pane, letting the page scroll past the table.
     *
     * @param gridRow     the grid row the cells were written to
     * @param focusAnchor the node focused when this row is reached
     * @param onEnter     action invoked on Enter or Space
     */
    private void registerSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter) {
        int index = rows.size();
        rows.add(new NavigableRow(focusAnchor, onEnter));
        rowHighlighter.bindFocus(gridRow, focusAnchor);
        focusAnchor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (isPastEdge(event.getCode(), index)) {
                return;
            }
            rowNavigator.syncIndex(index);
            if (rowNavigator.navigate(event)) {
                event.consume();
            }
        });
        focusAnchor.focusedProperty().addListener((obs, was, isFocused) -> {
            if (isFocused) {
                rowScroller.ensureVisible(focusAnchor);
            }
        });
    }

    /**
     * Returns {@code true} when the key would move past the table's edge: UP on the
     * first row or DOWN on the last row. Such keys are left unconsumed so the
     * enclosing scroll pane can scroll the page past the table.
     *
     * @param code  the pressed key code
     * @param index the index of the focused row
     * @return whether the key moves past the first or last row
     */
    private boolean isPastEdge(KeyCode code, int index) {
        return (code == KeyCode.UP && index == 0)
                || (code == KeyCode.DOWN && index == rows.size() - 1);
    }

    /**
     * Moves keyboard focus to the first navigable row, if any, and scrolls it
     * into view. Used to let a search field hand focus to the results on DOWN.
     *
     * <p>Returns whether the handoff actually happened, so callers such as
     * {@link edu.ntnu.idatt2003.millions.view.component.SearchBar} can decide
     * whether to consume the DOWN key: when the table is empty there is no row to
     * focus, and the key should be left unconsumed so it bubbles to the enclosing
     * {@link edu.ntnu.idatt2003.millions.view.component.KeyboardScrollPane} and
     * scrolls the page instead of being trapped in the search field.</p>
     *
     * @return {@code true} if a row received focus; {@code false} if there are no rows
     */
    public boolean focusFirstRow() {
        if (rows.isEmpty()) {
            return false;
        }
        Node first = rows.get(0).anchor();
        first.requestFocus();
        rowScroller.ensureVisible(first);
        return true;
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
