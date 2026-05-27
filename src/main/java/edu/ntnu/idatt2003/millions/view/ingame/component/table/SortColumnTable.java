package edu.ntnu.idatt2003.millions.view.ingame.component.table;

import edu.ntnu.idatt2003.millions.keyboard.navigation.ArrowKeyNavigator;
import edu.ntnu.idatt2003.millions.keyboard.navigation.PageArrowDispatcher;
import edu.ntnu.idatt2003.millions.keyboard.navigation.VerticalArrowHandler;
import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.format.TableCells;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.geometry.VPos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.Collection;
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
 * scrolling the surrounding view. At the first row (UP) and last row (DOWN),
 * the row strategy declines the key so {@link PageArrowDispatcher} scrolls the
 * active page past the table.</p>
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
        if (grid.getScene() != null) {
            Node focused = grid.getScene().getFocusOwner();
            if (isDescendantOfGrid(focused)) {
                grid.getScene().getRoot().requestFocus();
            }
        }
        grid.getChildren().clear();
        rowHighlighter.clear();
        rows.clear();
    }

    /**
     * Returns {@code true} if the given node is a descendant of this table's
     * backing {@link GridPane}.
     *
     * @param node the node to test; may be {@code null}
     * @return {@code true} if {@code node} is inside the grid, {@code false} otherwise
     */
    private boolean isDescendantOfGrid(Node node) {
        Node current = node;
        while (current != null) {
            if (current == grid) return true;
            current = current.getParent();
        }
        return false;
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
     * Adds a {@link RowCells}-based data row and makes it keyboard- and mouse-reachable.
     *
     * <p>After inserting the row and wiring the keyboard handlers, iterates all nodes
     * in {@code cells} and attaches a {@code MOUSE_CLICKED} handler to every node that
     * is not a {@link ButtonBase}. This lets the user open the detail view by clicking
     * anywhere on a data cell, while interactive controls (buttons, checkboxes) keep their
     * own click behaviour unchanged — {@link ButtonBase} already consumes
     * {@code MOUSE_CLICKED} before it bubbles, so no double-firing occurs on those.</p>
     *
     * @param gridRow     the grid row to write to (row 0 is reserved for the header)
     * @param focusAnchor the node focused when this row is reached
     * @param onEnter     action invoked on Enter, Space, or a primary click on a data cell
     * @param cells       the column-keyed cells from {@link RowCells#builder()}
     */
    public void addSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter, RowCells<Column> cells) {
        addRow(gridRow, cells);
        registerSelectableRow(gridRow, focusAnchor, onEnter, cells.nodes());
        for (Node node : cells.nodes()) {
            if (!(node instanceof ButtonBase)) {
                node.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        onEnter.run();
                        e.consume();
                    }
                });
            }
        }
    }

    /**
     * Wires an already-inserted row into the keyboard grid: registers it as a
     * {@link NavigableRow}, binds the focus highlight and registers its vertical-arrow
     * strategy under {@link PageArrowDispatcher#ARROW_HANDLER_KEY}. The central
     * dispatcher moves the selection through {@link #handleRowArrow(KeyEvent, int)};
     * this table keeps Enter/Space as the row's activation.
     *
     * @param gridRow     the grid row the cells were written to
     * @param focusAnchor the node focused when this row is reached
     * @param onEnter     action invoked on Enter or Space
     */
    private void registerSelectableRow(int gridRow, Node focusAnchor, Runnable onEnter, Collection<Node> rowNodes) {
        int index = rows.size();
        rows.add(new NavigableRow(focusAnchor, onEnter));
        rowHighlighter.bindFocus(gridRow, rowNodes);
        focusAnchor.getProperties().put(
                PageArrowDispatcher.ARROW_HANDLER_KEY,
                (VerticalArrowHandler) event -> handleRowArrow(event, index));
        focusAnchor.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                onEnter.run();
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
     * Vertical-arrow strategy for one row, while the row's anchor holds focus. Moves the selection within the table via the shared
     * {@link ArrowKeyNavigator}; declines the key at the first row (UP) and last row
     * (DOWN) so the dispatcher scrolls the page past the table instead.
     *
     * @param event the UP or DOWN key event
     * @param index the index of this row
     * @return {@code true} if the selection moved; {@code false} at the table edge
     */
    private boolean handleRowArrow(KeyEvent event, int index) {
        if (isPastEdge(event.getCode(), index)) {
            return false;
        }
        rowNavigator.syncIndex(index);
        return rowNavigator.navigate(event);
    }

    /**
     * Returns {@code true} when the key would move past the table's edge: UP on the
     * first row or DOWN on the last row. Such keys are declined by the row strategy
     * so {@link PageArrowDispatcher} can scroll the active page past the table.
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
     * total row) by column key.
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
