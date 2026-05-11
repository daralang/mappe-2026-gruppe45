package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A vertical container for label-value summary rows, with built-in
 * styling matching the application's modal summary blocks.
 *
 * <p>Use {@link #addRow(String, String)} for regular rows and
 * {@link #addTotal(String, String)} for the bold final row with
 * a divider. Call {@link #clear()} to remove all rows.</p>
 */
public class SummaryBox extends VBox {

    public SummaryBox() {
        getStyleClass().add("modal-summary");
    }

    /**
     * Adds a regular row to the summary.
     *
     * @param label the row label
     * @param value the row value
     */
    public void addRow(String label, String value) {
        getChildren().add(buildRow(label, value, false));
    }

    /**
     * Adds a total row (bold, with divider) to the summary.
     *
     * @param label the total label
     * @param value the total value
     */
    public void addTotal(String label, String value) {
        getChildren().add(buildRow(label, value, true));
    }

    /**
     * Adds a right-aligned conversion line in small, muted text.
     * Use for displaying the NOK equivalent of a foreign-currency total.
     *
     * @param value the formatted conversion string (e.g. "= 9 210,00 NOK")
     */
    public void addConversion(String value) {
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("modal-summary-conversion-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(spacer, valueNode);
        row.getStyleClass().add("modal-summary-conversion");
        getChildren().add(row);
    }

    /**
     * Removes all rows from the summary.
     */
    public void clear() {
        getChildren().clear();
    }

    private HBox buildRow(String label, String value, boolean total) {
        Label labelNode;
        Label valueNode;
        if (total) {
            labelNode = new Label(label);
            labelNode.getStyleClass().add("modal-summary-total-text");
            valueNode = new Label(value);
            valueNode.getStyleClass().add("modal-summary-total-text");
        } else {
            labelNode = StyledText.detailLabel(label);
            valueNode = StyledText.detailValue(value);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(labelNode, spacer, valueNode);
        row.getStyleClass().add("modal-summary-row");
        if (total) {
            row.getStyleClass().add("modal-summary-total");
        }
        return row;
    }
}