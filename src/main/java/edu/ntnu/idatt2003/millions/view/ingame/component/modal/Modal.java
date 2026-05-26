// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.ingame.component.modal;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

/**
 * Base class for all modal windows.
 *
 * <p>Subclasses implement {@code buildContent()} and optionally
 * {@code buildStandardHeader()}, {@code onBeforeShow()},
 * {@code configureCard()}, and {@code showStage()} (override to use
 * {@code showAndWait()} for blocking dialogs).</p>
 *
 * <p>ESC closes the modal via a scene-level event filter installed in {@link #show()}.
 * Each modal has its own {@link javafx.stage.Stage} and {@link javafx.scene.Scene},
 * so keyboard isolation is achieved naturally without the application-wide
 * {@code KeyboardNavigationService} context stack.
 * Override {@link #handleKeyPressed} and call {@code super} to add shortcuts.</p>
 */
public abstract class Modal {

    protected final Stage stage = new Stage();

    /**
     * Builds and displays the modal. Subclasses do not override this;
     * they override {@link #buildContent()} and the optional hooks.
     */
    public final void show() {
        VBox card = new VBox();
        card.getStyleClass().add("modal-card");
        configureCard(card);
        card.getChildren().add(buildContent());

        StackPane root = new StackPane(card);
        root.getStyleClass().add("modal-root");
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/tokens.css"))
                        .toExternalForm()
        );
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css"))
                        .toExternalForm()
        );

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        card.heightProperty().addListener((obs, oldH, newH) -> Platform.runLater(() -> {
            stage.setMinHeight(0);
            stage.setMinWidth(0);
            stage.sizeToScene();
        }));

        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);

        onBeforeShow();
        stage.sizeToScene();
        showStage();
        Platform.runLater(() -> {
            javafx.scene.Node target = firstFocusTarget();
            if (target != null) {
                target.requestFocus();
            }
        });
    }

    /**
     * Closes the modal.
     */
    public void close() {
        stage.close();
    }

    /**
     * Scene-level key handler. ESC closes; ENTER/SPACE fires the focused {@link Button}.
     *
     * @param event the key event
     * @return {@code true} if the event was handled
     */
    protected boolean handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            close();
            event.consume();
            return true;
        }
        if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
            javafx.scene.Node focused = stage.getScene().getFocusOwner();
            if (focused instanceof Button button) {
                button.fire();
                event.consume();
                return true;
            }
        }
        return false;
    }

    /**
     * Builds a standard modal header with a title and a close button.
     * Subclasses can use this for a consistent look or build their own.
     *
     * @param title the header title text
     * @return the header node
     */
    protected HBox buildStandardHeader(String title) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("\u2715");
        closeButton.getStyleClass().add("modal-close");
        closeButton.setOnAction(e -> close());

        HBox header = new HBox(titleLabel, spacer, closeButton);
        header.getStyleClass().add("modal-header");
        return header;
    }

    /**
     * Builds a standard modal header with a title, a muted subtitle on the
     * line below, and a close button in the top-right corner.
     *
     * @param title    the main header title
     * @param subtitle secondary text displayed below the title in muted style
     * @return the header node
     */
    protected VBox buildStandardHeaderWithSubtitle(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("\u2715");
        closeButton.getStyleClass().add("modal-close");
        closeButton.setOnAction(e -> close());

        HBox titleRow = new HBox(titleLabel, spacer, closeButton);

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("detail-label");

        VBox header = new VBox(4, titleRow, subtitleLabel);
        header.getStyleClass().add("modal-header");
        return header;
    }

    /**
     * Builds the content placed inside the modal card.
     * Called once when the modal is shown.
     *
     * @return the root node of the modal's content
     */
    protected abstract Region buildContent();

    /**
     * Called after the content is built but before the stage is shown.
     * Subclasses may override to perform initial rendering work.
     * Default does nothing.
     */
    protected void onBeforeShow() {
        // default: no-op
    }

    /**
     * Called immediately after the modal card {@link VBox} is created, before
     * content is added. Subclasses may override to adjust card dimensions or
     * apply additional style classes without affecting other modals.
     *
     * <p>Default does nothing.
     *
     * @param card the modal card node
     */
    protected void configureCard(VBox card) {
        // default: no-op
    }

    /**
     * Requests a stage resize to fit current content. Call this after any
     * visibility or managed change that grows or shrinks the scene content.
     * The resize is deferred to the next JavaFX pulse so the layout pass
     * that follows the visibility change has already run.
     */
    protected void sizeToContent() {
        Platform.runLater(() -> {
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            stage.setMinHeight(0);
            stage.setMinWidth(0);
            stage.sizeToScene();
        });
    }

    /**
     * Returns the node that should receive focus when the modal opens.
     * Default returns {@code null} (no auto-focus). Override to return
     * the first input field.
     *
     * @return the node to focus, or {@code null}
     */
    protected javafx.scene.Node firstFocusTarget() {
        return null;
    }

    /**
     * Shows the stage. Default uses non-blocking {@code show()}.
     * Subclasses that need to block the caller can override to use
     * {@code stage.showAndWait()} instead.
     */
    protected void showStage() {
        stage.show();
    }
}