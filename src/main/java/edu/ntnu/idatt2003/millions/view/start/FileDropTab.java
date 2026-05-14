package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.view.component.FileDropZone;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.function.Consumer;

/**
 * Abstract base class for start-screen tab panels that own a {@link FileDropZone}
 * and a confirm action button.
 *
 * <p>Encapsulates the shared structure of the new-game and load-game tabs:
 * a drop zone for file selection and an action button that triggers the tab's
 * primary action. Event wiring for the browse button, drag-and-drop, and the
 * action button is handled here via callbacks injected through
 * {@link #setOnBrowse(Runnable)}, {@link #setOnFileDrop(Consumer)},
 * and {@link #setOnAction(Runnable)}.</p>
 */
public abstract class FileDropTab extends VBox {

    protected static final double CARD_WIDTH = 460;
    private static final double FORM_SPACING = 16;

    private final FileDropZone fileDropZone;
    private final Button actionButton;

    private String filePath = "";
    private Runnable onBrowse;
    private Consumer<File> onFileDrop;
    private Runnable onAction;

    /**
     * Initialises the shared VBox layout, creates the {@link FileDropZone} and
     * action button, and wires their events to the callback fields.
     * Subclasses must call {@code getChildren().addAll(...)} to define the layout order.
     */
    protected FileDropTab() {
        setSpacing(FORM_SPACING);
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(24));

        fileDropZone = new FileDropZone();
        fileDropZone.setMaxWidth(CARD_WIDTH);

        actionButton = new Button();
        actionButton.setMaxWidth(CARD_WIDTH);
        actionButton.getStyleClass().add("start-action-button");

        fileDropZone.getBrowseButton().setOnAction(e -> {
            if (onBrowse != null) {
                onBrowse.run();
            }
        });
        fileDropZone.setOnFileDropped(file -> {
            if (onFileDrop != null) {
                onFileDrop.accept(file);
            }
        });
        actionButton.setOnAction(e -> {
            if (onAction != null) {
                onAction.run();
            }
        });
    }

    /**
     * Returns the currently stored file path.
     *
     * @return file path, or empty string when none is selected
     */
    public final String getFilePath() {
        return filePath;
    }

    /**
     * Updates the stored file path and mirrors the filename in the drop zone.
     * Calls {@link #onFileCleared()} when the path is blank or {@code null},
     * and {@link #onFileSelected()} otherwise, so subclasses can react to the change.
     *
     * @param path the absolute file path; {@code null} or blank resets the zone
     */
    public final void setFilePath(String path) {
        if (path == null || path.isBlank()) {
            this.filePath = "";
            fileDropZone.setFileName("");
            onFileCleared();
        } else {
            this.filePath = path;
            fileDropZone.setFileName(path);
            onFileSelected();
        }
    }

    /**
     * Returns the {@link FileDropZone} owned by this tab.
     * Subclasses use this to place the drop zone in their layout and to update
     * its display texts in {@code updateTexts()}.
     *
     * @return the shared {@link FileDropZone} instance
     */
    protected FileDropZone getFileDropZone() {
        return fileDropZone;
    }

    /**
     * Returns the action {@link Button} owned by this tab.
     * Subclasses use this to place the button in their layout.
     * To update the button label, use {@link #setActionButtonText(String)}.
     *
     * @return the shared action {@link Button} instance
     */
    protected Button getActionButton() {
        return actionButton;
    }

    /**
     * Sets the label text on the action button.
     * Provided so subclasses can update the button text in {@code updateTexts()}
     * without holding a direct reference to the button.
     *
     * @param text the button label to display
     */
    protected void setActionButtonText(String text) {
        actionButton.setText(text);
    }

    /**
     * Called by {@link #setFilePath(String)} when the path is cleared.
     * Override to react to file removal, for example disabling dependent controls.
     */
    protected void onFileCleared() {}

    /**
     * Called by {@link #setFilePath(String)} when a valid path is set.
     * Override to react to file selection, for example enabling dependent controls.
     */
    protected void onFileSelected() {}

    /**
     * Registers the callback invoked when the user clicks the browse button
     * on the {@link FileDropZone}.
     *
     * @param callback the action to run on browse
     */
    public void setOnBrowse(Runnable callback) {
        this.onBrowse = callback;
    }

    /**
     * Registers the callback invoked when the user drops a file onto the
     * {@link FileDropZone}.
     *
     * @param callback the action to run with the dropped file
     */
    public void setOnFileDrop(Consumer<File> callback) {
        this.onFileDrop = callback;
    }

    /**
     * Registers the callback invoked when the user clicks the action button.
     *
     * @param callback the action to run
     */
    public void setOnAction(Runnable callback) {
        this.onAction = callback;
    }
}
