package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.view.component.Header;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Title bar for Windows and Linux.
 *
 * <p>Renders a thin black strip with custom minimize / maximize / close buttons
 * and a draggable spacer, stacked above the shared {@link Header} navbar.
 * The combined {@link VBox} is the node placed at the top of the main layout.</p>
 *
 * <p>Requires {@link javafx.stage.StageStyle#TRANSPARENT} to be set on the stage
 * before the scene is attached — the factory handles this.</p>
 */
public class WindowsTitleBar implements TitleBar {

    private static final String ICON_MAXIMIZE = "▢";
    private static final String ICON_RESTORE  = "❐";

    private double dragStartX;
    private double dragStartY;

    private Runnable onDashboard = () -> {};
    private Runnable onExchange  = () -> {};
    private Runnable onSave      = () -> {};
    private Runnable onExit      = () -> {};

    private final Node node;

    public WindowsTitleBar(Stage stage) {
        this(stage, "title-bar", true);
    }

    WindowsTitleBar(Stage stage, String controlsStyleClass, boolean includeNavHeader) {
        HBox controls = buildControls(stage, controlsStyleClass);
        if (includeNavHeader) {
            Header header = new Header(
                    () -> onDashboard.run(),
                    () -> onExchange.run(),
                    () -> onSave.run(),
                    () -> onExit.run()
            );
            node = new VBox(controls, header);
        } else {
            node = controls;
        }
    }

    @Override public Node getNode()                    { return node; }
    @Override public void setOnDashboard(Runnable r)   { onDashboard = r; }
    @Override public void setOnExchange(Runnable r)    { onExchange = r; }
    @Override public void setOnSave(Runnable r)        { onSave = r; }
    @Override public void setOnExit(Runnable r)        { onExit = r; }
    @Override public void onGameUpdated()              {}
    @Override public void onLanguageChanged()          {}

    private HBox buildControls(Stage stage, String styleClass) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.getStyleClass().add("title-bar-drag-area");
        spacer.setOnMousePressed(e -> {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
        });
        spacer.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - dragStartX);
            stage.setY(e.getScreenY() - dragStartY);
        });
        spacer.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) stage.setMaximized(!stage.isMaximized());
        });

        Button minimize = new Button("–");
        minimize.getStyleClass().add("title-bar-btn");
        minimize.setOnAction(e -> stage.setIconified(true));

        Button maximize = new Button(stage.isMaximized() ? ICON_RESTORE : ICON_MAXIMIZE);
        maximize.getStyleClass().addAll("title-bar-btn", "title-bar-btn-max");
        maximize.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        stage.maximizedProperty().addListener((obs, old, isMax) -> {
            maximize.setText(isMax ? ICON_RESTORE : ICON_MAXIMIZE);
            if (isMax) maximize.getStyleClass().remove("title-bar-btn-max");
            else       maximize.getStyleClass().add("title-bar-btn-max");
        });
        stage.fullScreenProperty().addListener((obs, old, isFull) -> {
            maximize.setText(isFull ? ICON_RESTORE : ICON_MAXIMIZE);
            if (isFull) maximize.getStyleClass().remove("title-bar-btn-max");
            else        maximize.getStyleClass().add("title-bar-btn-max");
        });

        Button close = new Button("✕");
        close.getStyleClass().addAll("title-bar-btn", "title-bar-btn-close");
        close.setOnAction(e -> stage.close());

        HBox controls = new HBox(spacer, minimize, maximize, close);
        controls.getStyleClass().add(styleClass);
        return controls;
    }
}
