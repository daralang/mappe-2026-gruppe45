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
     * Removes all rows from the summary.
     */
    public void clear() {
        getChildren().clear();
    }

    private HBox buildRow(String label, String value, boolean total) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add(
                total ? "modal-summary-total-label" : "modal-summary-label");

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add(
                total ? "modal-summary-total-value" : "modal-summary-value");

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