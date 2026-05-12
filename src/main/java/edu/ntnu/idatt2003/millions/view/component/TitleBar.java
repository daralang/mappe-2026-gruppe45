package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Custom title bar that replaces the native OS title bar.
 *
 * <p>Rendered as a thin black strip that blends visually with the
 * application's black {@link Header}. The empty area is draggable
 * to move the window; double-clicking it toggles maximised state.
 * The three window-control buttons sit on the right edge.</p>
 *
 * <p>Requires {@link javafx.stage.StageStyle#UNDECORATED} to be set
 * on the stage before the scene is attached, otherwise the native title
 * bar and this custom one are both visible.</p>
 */
public class TitleBar extends HBox {

    private static final String ICON_MAXIMIZE = "▢";
    private static final String ICON_RESTORE  = "❐";

    private double dragX;
    private double dragY;

    /**
     * Constructs a new TitleBar wired to the given stage.
     *
     * @param stage the primary stage this bar controls
     */
    public TitleBar(Stage stage) {
        getStyleClass().add("title-bar");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.getStyleClass().add("title-bar-drag-area");

        spacer.setOnMousePressed(e -> {
            dragX = e.getSceneX();
            dragY = e.getSceneY();
        });
        spacer.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragX);
            stage.setY(e.getScreenY() - dragY);
        });
        spacer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
            }
        });

        Button minimize = new Button("–");
        minimize.getStyleClass().add("title-bar-btn");
        minimize.setOnAction(e -> stage.setIconified(true));

        Button maximize = new Button(stage.isMaximized() ? ICON_RESTORE : ICON_MAXIMIZE);
        maximize.getStyleClass().addAll("title-bar-btn", "title-bar-btn-max");
        maximize.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        stage.maximizedProperty().addListener((obs, old, isMax) -> {
            maximize.setText(isMax ? ICON_RESTORE : ICON_MAXIMIZE);
            if (isMax) {
                maximize.getStyleClass().remove("title-bar-btn-max");
            } else {
                maximize.getStyleClass().add("title-bar-btn-max");
            }
        });
        stage.fullScreenProperty().addListener((obs, old, isFullScreen) -> {
            maximize.setText(isFullScreen ? ICON_RESTORE : ICON_MAXIMIZE);
            if (isFullScreen) {
                maximize.getStyleClass().remove("title-bar-btn-max");
            } else {
                maximize.getStyleClass().add("title-bar-btn-max");
            }
        });

        Button close = new Button("✕");
        close.getStyleClass().addAll("title-bar-btn", "title-bar-btn-close");
        close.setOnAction(e -> stage.close());

        getChildren().addAll(spacer, minimize, maximize, close);
    }
}
