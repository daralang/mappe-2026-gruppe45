package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.FileDropZone;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract base class for start-screen tab panels that own a {@link FileDropZone}
 * and a confirm action button.
 *
 * <p>Encapsulates the shared structure of the new-game and load-game tabs:
 * a drop zone for file selection and an action button that triggers the tab's
 * primary action. An inline error label sits above the button and is shown
 * via {@link #showError(String)} and hidden via {@link #clearError()}.</p>
 *
 * <p>Event wiring for the browse button, drag-and-drop, and the action button
 * is handled here via callbacks injected through {@link #setOnBrowse(Runnable)},
 * {@link #setOnFileDrop(Consumer)}, and {@link #setOnAction(Runnable)}.</p>
 */
public abstract class FileDropTab extends VBox {

    protected static final double CARD_WIDTH = 460;
    private static final double FORM_SPACING = 16;

    private final FileDropZone fileDropZone;
    private final Button actionButton;
    private final Label errorLabel;
    private final VBox buttonArea;

    private String filePath = "";
    private Runnable onBrowse;
    private Consumer<File> onFileDrop;
    private Runnable onAction;
    private Supplier<String> currentErrorSupplier;

    /**
     * Initialises the shared VBox layout, creates the {@link FileDropZone},
     * action button, and error label, and wires their events to the callback fields.
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

        errorLabel = new Label();
        errorLabel.getStyleClass().addAll("detail-label", "negative");
        errorLabel.setMaxWidth(CARD_WIDTH);
        errorLabel.setWrapText(true);
        errorLabel.setOpacity(0);
        errorLabel.setVisible(false);
        // Bind managed to visible so layout space is never reserved while hidden
        errorLabel.managedProperty().bind(errorLabel.visibleProperty());
        // Re-translate the active error message when the language changes
        LanguageManager.addObserver(() -> {
            if (currentErrorSupplier != null && errorLabel.isVisible()) {
                errorLabel.setText(currentErrorSupplier.get());
            }
        });

        buttonArea = new VBox(8, errorLabel, actionButton);
        buttonArea.setAlignment(Pos.TOP_CENTER);
        buttonArea.setMaxWidth(CARD_WIDTH);

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
            clearError();
            if (onAction != null) {
                onAction.run();
            }
        });
    }

    /**
     * Shows an inline error above the action button with a fade-in.
     * The supplier is stored so the message can be re-translated on language change.
     *
     * @param messageSupplier produces the localised error string
     */
    public void showError(Supplier<String> messageSupplier) {
        this.currentErrorSupplier = messageSupplier;
        errorLabel.setText(messageSupplier.get());
        errorLabel.setOpacity(0);
        errorLabel.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Hides the inline error message and clears the stored supplier.
     */
    public void clearError() {
        currentErrorSupplier = null;
        errorLabel.setVisible(false);
        errorLabel.setOpacity(0);
        errorLabel.setText("");
    }

    /**
     * Returns the visible property of the error label, used by
     * {@link StartLayoutAnimator} to animate card height on error show/hide.
     *
     * @return the error label's visible property
     */
    public javafx.beans.value.ObservableBooleanValue errorVisibleProperty() {
        return errorLabel.visibleProperty();
    }

    /**
     * Returns the layout height the inline error row reserves when visible.
     *
     * <p>Computed as the error label's preferred height at the card width plus
     * the spacing between the label and the action button inside
     * {@link #getButtonArea()}. {@link StartLayoutAnimator} uses this value to
     * grow and shrink the surrounding tab content by an exact pixel delta
     * when the error is shown or hidden, instead of relying on the parent
     * {@code VBox} to grow on its own (which is blocked when the tab pane
     * height is explicitly constrained).</p>
     *
     * @return the height in pixels the error row adds to {@link #getButtonArea()} when shown
     */
    public double computeErrorReservedHeight() {
        errorLabel.applyCss();
        return errorLabel.prefHeight(CARD_WIDTH) + buttonArea.getSpacing();
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
     *
     * @return the shared {@link FileDropZone} instance
     */
    protected FileDropZone getFileDropZone() {
        return fileDropZone;
    }

    /**
     * Returns the action {@link Button} owned by this tab.
     *
     * @return the shared action {@link Button} instance
     */
    protected Button getActionButton() {
        return actionButton;
    }

    /**
     * Returns the button area containing the inline error label and the action button.
     * Subclasses should add this to their layout instead of {@link #getActionButton()} directly.
     *
     * @return a {@link VBox} with the error label above the action button
     */
    protected VBox getButtonArea() {
        return buttonArea;
    }

    /**
     * Sets the label text on the action button.
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
     * Registers the callback invoked when the user clicks the browse button.
     *
     * @param callback the action to run on browse
     */
    public void setOnBrowse(Runnable callback) {
        this.onBrowse = callback;
    }

    /**
     * Registers the callback invoked when the user drops a file onto the drop zone.
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
