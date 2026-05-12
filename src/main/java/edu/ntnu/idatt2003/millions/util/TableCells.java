package edu.ntnu.idatt2003.millions.util;

import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Stateless helper that builds the table primitives shared by every
 * dashboard table card.
 *
 * <p>Covers two levels:</p>
 * <ul>
 *   <li><b>Cell factories</b> — {@link #header}, {@link #sortHeader}, {@link #data} and
 *       {@link #empty} return individual styled nodes. Callers compose
 *       them into rows themselves, so anything that needs a one-off cell
 *       (a row with a tooltip, a custom badge, a colored amount) gets the
 *       same look without going through a generic row builder.</li>
 *   <li><b>Grid helpers</b> — {@link #configureColumns}, {@link #addHeaderRow}
 *       and {@link #renderEmptyState} write directly to a {@link GridPane}.
 *       They encapsulate the small loops that every table would otherwise
 *       duplicate, so each table's {@code refresh()} reads top-to-bottom
 *       without boilerplate.</li>
 * </ul>
 *
 * <p>What is intentionally <b>not</b> here: a full table render pipeline.
 * Cards still own their own {@code refresh()} and choose what to do for
 * each row, which keeps the abstraction shallow and easy to follow. If a
 * third tabular card later shows that every table runs the same
 * {@code clear → header → empty-check → loop} skeleton, that's the right
 * moment to promote the skeleton into a {@code TableCard<T>} base class.</p>
 *
 * <p>Specialised cells — colored amount cells, badges, action buttons —
 * are intentionally outside this class: they belong to
 * {@code ChangeFormatter}, {@code TransactionTypeBadge} and the owning
 * table respectively, since their styling is more than just a typography
 * class.</p>
 */
public final class TableCells {

    /**
     * Number format shared by every table in the dashboard.
     * Norwegian locale gives comma decimal separator and space thousands
     * separator (e.g. {@code 1 234,56}).
     */
    public static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private TableCells() {
        // Utility class — should not be instantiated.
    }

    // ----- Cell factories ---------------------------------------------------

    /**
     * Creates a header cell with the shared {@code holdings-header} CSS class.
     *
     * @param text the header text
     * @return a styled header label
     */
    public static Label header(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("holdings-header");
        return label;
    }

    /**
     * Creates a sortable header button with the shared {@code holdings-header} CSS class.
     *
     * <p>Unlike {@link #header(String)}, which returns a non-interactive {@link Label},
     * this method returns a {@link Button} suitable for column headers that trigger
     * a sort action when clicked.
     *
     * @param text      the header button text
     * @param active    whether this column is the active sort column
     * @param ascending whether the active sort direction is ascending
     * @param onClick   the action to run when the button is clicked
     * @return a styled sort header button
     */
    public static Button sortHeader(String text, boolean active, boolean ascending, Runnable onClick) {
        String indicator = active ? (ascending ? " ↓ " : "  ↑") : " ↓↑";
        Button button = new Button(text + indicator);
        button.getStyleClass().add("holdings-header");
        button.setOnAction(e -> onClick.run());
        return button;
    }

    /**
     * Creates a plain data cell with the shared {@code holdings-cell} CSS class.
     *
     * @param text the cell text
     * @return a styled data label
     */
    public static Label data(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("holdings-cell");
        return label;
    }

    /**
     * Creates the centered empty-state label used when a table has no rows
     * to display. Returned as a {@link StyledText} so callers can apply
     * {@code GridPane.setColumnSpan} and {@code GridPane.setHalignment} on it.
     *
     * @param text the empty-state message
     * @return a styled empty-state label
     */
    public static StyledText empty(String text) {
        StyledText label = StyledText.widgetLabel(text);
        label.getStyleClass().add("holdings-empty");
        return label;
    }

    // ----- Grid helpers -----------------------------------------------------

    /**
     * Configures a grid's columns from parallel arrays of widths (as
     * percentages summing to about 100) and horizontal alignments.
     *
     * <p>Length of the two arrays must match; this is the table's
     * responsibility, not the helper's. Existing column constraints are
     * not cleared — call this once during construction.</p>
     *
     * @param grid       the grid to configure
     * @param widths     percentage width per column
     * @param alignments horizontal alignment per column
     */
    public static void configureColumns(GridPane grid, double[] widths, HPos[] alignments) {
        for (int i = 0; i < widths.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHalignment(alignments[i]);
            col.setPercentWidth(widths[i]);
            grid.getColumnConstraints().add(col);
        }
    }

    /**
     * Writes a header row to row 0 of the given grid, with one
     * {@link #header} cell per provided label.
     *
     * @param grid    the grid to write to
     * @param headers the header texts, one per column
     */
    public static void addHeaderRow(GridPane grid, String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            grid.add(header(headers[i]), i, 0);
        }
    }

    /**
     * Renders a centered empty-state message spanning the full width of
     * the table on row 1 (immediately below the header row).
     *
     * @param grid        the grid to write to
     * @param message     the empty-state text
     * @param columnCount the total number of columns to span across
     */
    public static void renderEmptyState(GridPane grid, String message, int columnCount) {
        StyledText label = empty(message);
        GridPane.setColumnSpan(label, columnCount);
        GridPane.setHalignment(label, HPos.CENTER);
        grid.add(label, 0, 1);
    }
}
