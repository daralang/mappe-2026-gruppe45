package edu.ntnu.idatt2003.millions.view.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
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
 * Base class for modal windows in the application.
 * Encapsulates the boilerplate of building a transparent, modal stage
 * with a card-style content surface, an ESC handler, and common styling.
 *
 * <p>Subclasses provide their content via {@link #buildContent()} and may
 * optionally use {@link #buildStandardHeader(String)} for a consistent
 * header with title and close button. Override {@link #onBeforeShow()} for
 * any work that needs to run after the content is built but before the
 * modal is displayed (e.g. initial rendering).</p>
 *
 * <p>By default the modal uses non-blocking {@code show()}. Subclasses
 * that need to block the caller (e.g. dialogs awaiting user input) can
 * override {@link #showStage()} to use {@code showAndWait()} instead.</p>
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

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });

        onBeforeShow();
        stage.sizeToScene();
        showStage();
    }

    /**
     * Closes the modal.
     */
    public void close() {
        stage.close();
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
     * Shows the stage. Default uses non-blocking {@code show()}.
     * Subclasses that need to block the caller can override to use
     * {@code stage.showAndWait()} instead.
     */
    protected void showStage() {
        stage.show();
    }
}