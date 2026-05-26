package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.start.component.FileDropZone;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
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
 * primary action.</p>
 *
 * <p>Event wiring for the browse button, drag-and-drop, and the action button
 * is handled here via callbacks injected through {@link #setOnBrowse(Runnable)},
 * {@link #setOnFileDrop(Consumer)}, and {@link #setOnAction(Runnable)}.</p>
 */
public abstract class FileDropTab extends VBox {

    protected static final double CARD_WIDTH = 460;
    private static final double FORM_SPACING = 16;
    private static final double BUTTON_AREA_TOP_OFFSET = -8;

    private static final String FEEDBACK_TONE_ERROR = "negative";
    private static final String FEEDBACK_TONE_SUCCESS = "positive";

    private final FileDropZone fileDropZone;
    private final Button actionButton;
    private final StyledText feedbackLabel;
    private final VBox buttonArea;

    private String filePath = "";
    private Runnable onBrowse;
    private Consumer<File> onFileDrop;
    private Runnable onAction;
    private Supplier<String> currentFeedbackSupplier;

    /**
     * Initialises the shared VBox layout, creates the {@link FileDropZone},
     * action button, and feedback label, and wires their events to the callback fields.
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

        feedbackLabel = StyledText.detailLabel();
        feedbackLabel.setMaxWidth(CARD_WIDTH);
        feedbackLabel.setWrapText(true);
        feedbackLabel.setOpacity(0);
        feedbackLabel.setVisible(false);
        // Bind managed to visible so layout space is never reserved while hidden
        feedbackLabel.managedProperty().bind(feedbackLabel.visibleProperty());
        // Re-translate the active feedback message when the language changes
        LanguageManager.addObserver(() -> {
            if (currentFeedbackSupplier != null && feedbackLabel.isVisible()) {
                feedbackLabel.setText(currentFeedbackSupplier.get());
            }
        });

        buttonArea = new VBox(8, feedbackLabel, actionButton);
        buttonArea.setAlignment(Pos.TOP_CENTER);
        buttonArea.setMaxWidth(CARD_WIDTH);
        // Tighten the gap above the button area: the standard FORM_SPACING (16) feels
        // too airy between the last form row and the inline feedback / action button.
        VBox.setMargin(buttonArea, new Insets(BUTTON_AREA_TOP_OFFSET, 0, 0, 0));

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
            clearFeedback();
            if (onAction != null) {
                onAction.run();
            }
        });
        actionButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                actionButton.fire();
                event.consume();
            }
        });
    }

    /**
     * Shows an inline error above the action button with a fade-in, replacing
     * any currently visible feedback. The supplier is stored so the message can
     * be re-translated on language change.
     *
     * @param messageSupplier produces the localised error string
     */
    public void showError(Supplier<String> messageSupplier) {
        showFeedback(messageSupplier, FEEDBACK_TONE_ERROR);
    }

    /**
     * Shows an inline success message above the action button with a fade-in,
     * replacing any currently visible feedback. The supplier is stored so the
     * message can be re-translated on language change.
     *
     * @param messageSupplier produces the localised success string
     */
    public void showSuccess(Supplier<String> messageSupplier) {
        showFeedback(messageSupplier, FEEDBACK_TONE_SUCCESS);
    }

    /**
     * Shows feedback in the shared inline label with the given tone style class
     * applied (either {@link #FEEDBACK_TONE_ERROR} or {@link #FEEDBACK_TONE_SUCCESS}).
     * Any previously applied tone class is removed first so the label cannot
     * carry both at the same time.
     *
     * @param messageSupplier  produces the localised feedback string
     * @param toneStyleClass   the tone CSS class to apply for this message
     */
    private void showFeedback(Supplier<String> messageSupplier, String toneStyleClass) {
        this.currentFeedbackSupplier = messageSupplier;
        feedbackLabel.setText(messageSupplier.get());
        feedbackLabel.getStyleClass().removeAll(FEEDBACK_TONE_ERROR, FEEDBACK_TONE_SUCCESS);
        feedbackLabel.getStyleClass().add(toneStyleClass);
        feedbackLabel.setOpacity(0);
        feedbackLabel.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.millis(200), feedbackLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    /**
     * Hides the inline feedback message, clears the stored supplier, and removes
     * any applied tone class so the label is ready for the next message.
     */
    public void clearFeedback() {
        currentFeedbackSupplier = null;
        feedbackLabel.setVisible(false);
        feedbackLabel.setOpacity(0);
        feedbackLabel.setText("");
        feedbackLabel.getStyleClass().removeAll(FEEDBACK_TONE_ERROR, FEEDBACK_TONE_SUCCESS);
    }

    /**
     * Returns the visible property of the feedback label, used by
     * {@link StartLayoutAnimator} to animate card height on feedback show/hide.
     *
     * @return the feedback label's visible property
     */
    public javafx.beans.value.ObservableBooleanValue feedbackVisibleProperty() {
        return feedbackLabel.visibleProperty();
    }

    /**
     * Returns the layout height the inline feedback row reserves when visible.
     *
     * @return the height in pixels the feedback row adds to {@link #getButtonArea()} when shown
     */
    public double computeFeedbackReservedHeight() {
        feedbackLabel.applyCss();
        return feedbackLabel.prefHeight(CARD_WIDTH) + buttonArea.getSpacing();
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
