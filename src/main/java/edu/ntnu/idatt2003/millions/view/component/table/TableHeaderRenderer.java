package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.util.SortState;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Package-private helper that builds and updates the header row for a {@link SortColumnTable}.
 *
 * <p>Owns the header-cell cache so that tooltip observers and JavaFX controls are not
 * recreated on every refresh. On the first call to {@link #renderInto} the cells are
 * built and stored; subsequent calls update label text, sort indicators and button
 * actions in place.</p>
 *
 * <p>Receives a shared {@link SortState} from the owning {@link SortColumnTable} so
 * that sort indicators always reflect the current state without an extra indirection.</p>
 *
 * @param <Column> the sort-column enum type used by the owning table
 */
class TableHeaderRenderer<Column> {

    private final SortState<Column> sortState;
    private final List<HeaderCell<Column>> headerCells = new ArrayList<>();

    /**
     * Constructs a renderer backed by the given sort state.
     *
     * @param sortState the sort state shared with the owning {@link SortColumnTable}
     */
    TableHeaderRenderer(SortState<Column> sortState) {
        this.sortState = sortState;
    }

    /**
     * Builds or refreshes the header row into row 0 of the given grid.
     *
     * <p>On the first call the header cells are created and cached. On every call
     * the cached cells are updated with the latest i18n labels and sort indicators,
     * then placed into row 0.</p>
     *
     * @param grid      the grid to render the header into
     * @param columns   the current column definitions, resolved fresh on every call
     * @param onChanged callback invoked after any sort-state change so the owning
     *                  card can trigger a data refresh
     */
    void renderInto(GridPane grid, List<TableColumnDef<Column>> columns, Runnable onChanged) {
        ensureHeaderCells(columns, onChanged);
        for (int i = 0; i < columns.size(); i++) {
            HeaderCell<Column> cell = headerCells.get(i);
            updateHeaderCell(cell, columns.get(i), onChanged);
            grid.add(cell.root(), i, 0);
        }
    }

    /**
     * Creates and caches the header cells on the first render call.
     *
     * @param columns   the column definitions to build cells from
     * @param onChanged callback passed through to each sortable button
     */
    private void ensureHeaderCells(List<TableColumnDef<Column>> columns, Runnable onChanged) {
        if (!headerCells.isEmpty()) {
            return;
        }
        for (TableColumnDef<Column> col : columns) {
            headerCells.add(buildHeaderCell(col, onChanged));
        }
    }

    /**
     * Builds the header cell for the given column definition.
     *
     * <p>Sortable columns render as a {@link Button}. Static columns render as a plain
     * label via {@link TableCells#header}. Columns with a tooltip key get an
     * {@link InfoTooltip} icon wrapped in an {@link HBox}.</p>
     *
     * @param col       the column definition
     * @param onChanged the sort-change callback
     * @return the built header cell
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
     * Updates a cached header cell with the latest label text and sort state.
     *
     * <p>The column supplier may resolve localised labels on every call, so this
     * method keeps reused header nodes in sync with the current language and
     * {@link SortState}.</p>
     *
     * @param cell      the cached header cell to update
     * @param col       the latest column definition
     * @param onChanged callback invoked after any sort-state change
     */
    private void updateHeaderCell(HeaderCell<Column> cell, TableColumnDef<Column> col, Runnable onChanged) {
        Labeled label = cell.label();
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
     * <p>The button delegates to {@link SortState#toggle} on click and fires
     * {@code onChanged} to trigger a data refresh in the owning card.</p>
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
     * Formats the visible text for a sortable header button based on the current
     * {@link SortState}.
     *
     * @param col the sortable column definition
     * @return label text with primary (↓/↑), secondary (²↓/²↑) or inactive (↓↑) indicator
     */
    private String sortHeaderText(TableColumnDef<Column> col) {
        Column sortColumn = col.sortColumn();
        if (sortState.isSecondaryActive(sortColumn)) {
            return "² " + col.label() + (sortState.isSecondaryAscending() ? " ↓ " : "  ↑");
        }
        if (sortState.isActive(sortColumn)) {
            return col.label() + (sortState.isAscending() ? " ↓ " : "  ↑");
        }
        return col.label() + " ↓↑";
    }

    /**
     * Cached header node and its visible label or button.
     *
     * @param root  the node inserted into the table grid (may be a wrapper {@link HBox})
     * @param label the {@link Labeled} whose text is refreshed on every render
     * @param <Column> the sort-column enum type
     */
    private record HeaderCell<Column>(Node root, Labeled label) {
    }
}
