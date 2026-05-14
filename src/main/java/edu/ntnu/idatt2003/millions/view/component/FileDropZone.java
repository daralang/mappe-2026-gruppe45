package edu.ntnu.idatt2003.millions.view.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * A reusable drag-and-drop file upload zone.
 *
 * <p> Renders a dashed bordered box with a hint label, an "or" separator,
 * a browse button, and a filename label that appears once a file is selected.
 * Drag-over highlighting is handled internally as a purely visual concern.
 * The controller is responsible for binding {@link #getBrowseButton()} and
 * {@code setOnDragDropped} to perform file selection and validation. </p>
 */
public class FileDropZone extends VBox {

    private static final double PREF_HEIGHT = 160;
    private static final double SPACING = 10;

    private final Label hintLabel;
    private final Label orLabel;
    private final Button browseButton;
    private final Label fileNameLabel;

    /**
     * Creates a new {@link FileDropZone} with empty text labels.
     * Call {@link #setHintText}, {@link #setOrText}, and {@link #setBrowseText}
     * to populate the visible text, for example from {@code LanguageManager}.
     */
    public FileDropZone() {
        hintLabel = new Label();
        hintLabel.getStyleClass().add("drop-zone-hint");

        orLabel = new Label();
        orLabel.getStyleClass().add("drop-zone-or");

        browseButton = new Button();
        browseButton.getStyleClass().add("browse-button");

        fileNameLabel = new Label();
        fileNameLabel.setVisible(false);

        setSpacing(SPACING);
        setAlignment(Pos.CENTER);
        setPrefHeight(PREF_HEIGHT);
        setPadding(new Insets(20));
        getStyleClass().add("drop-zone");
        getChildren().addAll(hintLabel, orLabel, browseButton, fileNameLabel);

        bindDragHighlight();
    }

    /**
     * Wires internal drag-over and drag-exit handlers that toggle the
     * highlight CSS class. These are purely visual and do not perform
     * any file-selection logic.
     */
    private void bindDragHighlight() {
        setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
                getStyleClass().remove("drop-zone");
                getStyleClass().add("drop-zone-highlight");
            }
            e.consume();
        });

        setOnDragExited(e -> {
            getStyleClass().remove("drop-zone-highlight");
            if (!getStyleClass().contains("drop-zone")) {
                getStyleClass().add("drop-zone");
            }
            e.consume();
        });
    }

    /**
     * Sets the main instruction text shown inside the drop zone.
     *
     * @param text the hint text to display
     */
    public void setHintText(String text) {
        hintLabel.setText(text);
    }

    /**
     * Sets the "or" separator text shown between the hint and the browse button.
     *
     * @param text the separator text to display
     */
    public void setOrText(String text) {
        orLabel.setText(text);
    }

    /**
     * Sets the label on the browse button.
     *
     * @param text the button label to display
     */
    public void setBrowseText(String text) {
        browseButton.setText(text);
    }

    /**
     * Returns the browse button so the controller can attach an action handler.
     *
     * @return the browse {@link Button}
     */
    public Button getBrowseButton() {
        return browseButton;
    }

    /**
     * Displays the given filename below the browse button.
     * Passing {@code null} or a blank string hides the label and clears its text.
     *
     * @param fileName the filename to display, or {@code null} / blank to hide
     */
    public void setFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            fileNameLabel.setText("");
            fileNameLabel.setVisible(false);
        } else {
            fileNameLabel.setText(fileName);
            fileNameLabel.setVisible(true);
        }
    }
}
