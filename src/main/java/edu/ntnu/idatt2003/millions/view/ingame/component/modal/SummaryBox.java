package edu.ntnu.idatt2003.millions.view.ingame.component.modal;

import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.InfoTooltip;
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
     * Inserts a small uppercase title at the top of the summary box.
     * Call before any {@link #addRow} calls.
     *
     * @param title the section title text
     */
    public void setSectionTitle(String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-summary-section-title");
        getChildren().add(0, titleLabel);
    }

    /**
     * Adds a regular row to the summary.
     *
     * @param label the row label
     * @param value the row value
     */
    public void addRow(String label, String value) {
        getChildren().add(buildRow(label, value, false, null, null));
    }

    /**
     * Adds a regular row and applies an extra CSS class to the value node.
     * Use this to layer {@code positive} or {@code negative} on a value.
     *
     * @param label           the row label
     * @param value           the row value
     * @param extraValueClass additional style class for the value label, or null
     */
    public void addRow(String label, String value, String extraValueClass) {
        getChildren().add(buildRow(label, value, false, extraValueClass, null));
    }

    /**
     * Adds a regular row with an {@link InfoTooltip} next to the label.
     * The tooltip triggers on hover anywhere over the row.
     * Pass {@code null} for {@code extraValueClass} when no color modifier is needed.
     *
     * @param label           the row label
     * @param value           the row value
     * @param extraValueClass additional style class for the value label, or null
     * @param tooltipKey      the i18n key used to look up the tooltip text
     */
    public void addRow(String label, String value, String extraValueClass, String tooltipKey) {
        getChildren().add(buildRow(label, value, false, extraValueClass, tooltipKey));
    }

    /**
     * Adds a total row (bold, with divider) to the summary.
     *
     * @param label the total label
     * @param value the total value
     */
    public void addTotal(String label, String value) {
        getChildren().add(buildRow(label, value, true, null, null));
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

    private HBox buildRow(String label, String value, boolean total,
                          String extraValueClass, String tooltipKey) {
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

        if (extraValueClass != null) {
            valueNode.getStyleClass().add(extraValueClass);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row;
        if (tooltipKey != null) {
            InfoTooltip infoTooltip = new InfoTooltip(tooltipKey);
            HBox labelContainer = new HBox(5, labelNode, infoTooltip);
            row = new HBox(labelContainer, spacer, valueNode);
            infoTooltip.attachToParent(row);
        } else {
            row = new HBox(labelNode, spacer, valueNode);
        }
        row.getStyleClass().add("modal-summary-row");
        if (total) {
            row.getStyleClass().add("modal-summary-total");
        }
        return row;
    }
}