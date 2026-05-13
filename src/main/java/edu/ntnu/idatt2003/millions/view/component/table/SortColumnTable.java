package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Reusable sortable table component backed by a {@link GridPane}.
 *
 * <p>Encapsulates the table infrastructure across every table card: column configuration,
 * header-row rendering (static and sortable), empty-state display,
 * and sort state management. Header nodes are created once and reused across refreshes
 * so tooltip observers and JavaFX controls are not recreated unnecessarily. Cards retain
 * responsibility for data fetching, filtering, sorting and cell construction.</p>
 *
 * @param <Column> the sort-column enum type; use a wildcard or {@code Object}
 *            when no column is sortable
 */
public class SortColumnTable<Column> {

    private static final double DEFAULT_HGAP = 20;

    private final Supplier<List<TableColumnDef<Column>>> columnSupplier;
    private final int columnCount;
    private final SortState<Column> sortState = new SortState<>();
    private final GridPane grid = new GridPane();
    private final List<HeaderCell<Column>> headerCells = new ArrayList<>();

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
        configureColumns(initial);
    }

    /**
     * Configures the grid's column constraints from the initial column definitions.
     * Called once during construction; widths and alignments are stable across
     * language changes so the supplier need not be re-called for this.
     *
     * @param cols the initial column definitions to read structure from
     */
    private void configureColumns(List<TableColumnDef<Column>> cols) {
        for (TableColumnDef<Column> col : cols) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            grid.getColumnConstraints().add(cc);
        }
    }

    /**
     * Clears all nodes from the grid, including any previously rendered
     * header and data rows. Call this at the start of every refresh before
     * calling {@link #refreshHeader}.
     */
    public void clearRows() {
        grid.getChildren().clear();
    }

    /**
     * Builds or refreshes the header row into row 0 of the grid.
     *
     * <p>Sortable columns render as clickable {@link Button}s with a
     * directional indicator (↓↑ / ↓ / ↑). Clicking a button activates
     * ascending sort, or toggles direction if already active; a second
     * active column shifts to secondary sort with a "²" prefix. Static
     * columns render as plain. Columns with a tooltip key get an {@link InfoTooltip}
     * icon attached to the right of the header text. Header nodes are cached
     * after the first call; later calls update label text, sort indicators and
     * button actions without creating new tooltip observers.</p>
     *
     * @param onChanged callback invoked after any sort-state change so the
     *                  owning card can trigger a data refresh
     */
    public void refreshHeader(Runnable onChanged) {
        List<TableColumnDef<Column>> current = columnSupplier.get();
        ensureHeaderCells(current, onChanged);
        for (int i = 0; i < current.size(); i++) {
            HeaderCell<Column> headerCell = headerCells.get(i);
            updateHeaderCell(headerCell, current.get(i), onChanged);
            grid.add(headerCell.root(), i, 0);
        }
    }

    /**
     * Creates the cached header cells on the first header refresh.
     *
     * @param columns   the current column definitions
     * @param onChanged callback invoked after any sort-state change
     */
    private void ensureHeaderCells(List<TableColumnDef<Column>> columns, Runnable onChanged) {
        if (!headerCells.isEmpty()) {
            return;
        }
        for (TableColumnDef<Column> column : columns) {
            headerCells.add(buildHeaderCell(column, onChanged));
        }
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
     * <p>Use this when a cell needs constraints that cannot be expressed by
     * {@link #addRow}, for example {@code GridPane.setValignment(node, VPos.TOP)}
     * set directly after this call.</p>
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
     * <p>Intended for structural rows such as dividers between data and
     * totals, or summary labels that should cover all columns.</p>
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
     * <p>Delegates to {@link TableCells#renderEmptyState} so the message
     * inherits the shared {@code holdings-empty} style.</p>
     *
     * @param message the localised empty-state text to display
     */
    public void renderEmptyState(String message) {
        TableCells.renderEmptyState(grid, message, columnCount);
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
     * Clears the active sort column and direction, returning the table to
     * its unsorted state. Equivalent to calling {@code getSortState().clear()}.
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
     * Builds the header cell for the given column definition.
     * Delegates to {@link #buildSortableButton} for sortable columns and
     * wraps both variants in a tooltip {@link HBox} when a tooltip key is set.
     *
     * @param col       the column definition
     * @param onChanged the sort-change callback
     * @return the cached header cell to add at row 0
     */
    private HeaderCell<Column> buildHeaderCell(TableColumnDef<Column> col, Runnable onChanged) {
        Labeled base = col.isSortable()
                ? buildSortableButton(col, onChanged)
                : TableCells.header(col.label());

        if (!col.hasTooltip()) {
            return new HeaderCell<>(base, base);
        }

        InfoTooltip icon = new InfoTooltip(col.tooltipKey());
        icon.getStyleClass().add("holdings-header-icon");
        HBox wrapper = new HBox(6, base, icon);
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        GridPane.setFillWidth(wrapper, false);
        icon.attachToParent(wrapper);
        return new HeaderCell<>(wrapper, base);
    }

    /**
     * Updates a cached header cell from the latest column definition.
     *
     * <p>The column supplier may resolve localized labels on every call, so this
     * method keeps reused header nodes in sync with the current language and
     * {@link SortState}.</p>
     *
     * @param headerCell the cached header cell to update
     * @param col        the latest column definition
     * @param onChanged  callback invoked after any sort-state change
     */
    private void updateHeaderCell(
            HeaderCell<Column> headerCell,
            TableColumnDef<Column> col,
            Runnable onChanged
    ) {
        Labeled label = headerCell.label();
        if (label instanceof Button button && col.isSortable()) {
            button.setText(sortHeaderText(col));
            button.setOnAction(e -> {
                sortState.toggle(col.sortColumn());
                onChanged.run();
            });
        } else {
            label.setText(col.label());
        }
    }

    /**
     * Builds a sort button for the given column.
     *
     * <p>When the column is the secondary sort, a {@code ²} prefix is appended
     * to indicate its tiebreaker role. The button delegates to
     * {@link SortState#toggle} on click, then fires {@code onChanged}.</p>
     *
     * @param col       the sortable column definition
     * @param onChanged the sort-change callback
     * @return a styled sort header button via {@link TableCells#sortHeader}
     */
    private Button buildSortableButton(TableColumnDef<Column> col, Runnable onChanged) {
        Column sortColumn = col.sortColumn();
        Runnable action = () -> {
            sortState.toggle(sortColumn);
            onChanged.run();
        };
        return TableCells.sortHeader(sortHeaderText(col), false, true, action);
    }

    /**
     * Formats the visible text for a sortable header button.
     *
     * @param col the sortable column definition
     * @return label text with primary, secondary or inactive sort indicator
     */
    private String sortHeaderText(TableColumnDef<Column> col) {
        Column sortColumn = col.sortColumn();
        if (sortState.isSecondaryActive(sortColumn)) {
            return "² " + col.label()
                    + (sortState.isSecondaryAscending() ? " ↓ " : "  ↑");
        }
        if (sortState.isActive(sortColumn)) {
            return col.label() + (sortState.isAscending() ? " ↓ " : "  ↑");
        }
        return col.label() + " ↓↑";
    }

    /**
     * Cached header node and its visible label/button.
     *
     * @param root  the node inserted into the table grid
     * @param label the {@link Labeled} control whose text is refreshed
     * @param <Column> the sort-column enum type
     */
    private record HeaderCell<Column>(Node root, Labeled label) {
    }
}
