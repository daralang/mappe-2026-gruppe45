package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.Header;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class WindowsTitleBar implements TitleBar {

    private static final String ICON_MAXIMIZE = "▢";
    private static final String ICON_RESTORE  = "❐";

    private double dragStartX;
    private double dragStartY;

    private Runnable onDashboard   = () -> {};
    private Runnable onExchange    = () -> {};
    private Runnable onLeaderboard = () -> {};
    private Runnable onNewGame     = () -> {};
    private Runnable onSave        = () -> {};
    private Runnable onExit        = () -> {};

    private final Node node;
    private Header header;

    public WindowsTitleBar(Stage stage, GameService gameService) {
        this(stage, gameService, "title-bar", true);
    }

    WindowsTitleBar(Stage stage, String controlsStyleClass, boolean includeNavHeader) {
        this(stage, null, controlsStyleClass, includeNavHeader);
    }

    WindowsTitleBar(Stage stage, GameService gameService,
                    String controlsStyleClass, boolean includeNavHeader) {
        HBox controls = buildControls(stage, controlsStyleClass);
        if (includeNavHeader && gameService != null) {
            header = new Header(
                    () -> onDashboard.run(),
                    () -> onExchange.run(),
                    () -> onSave.run(),
                    () -> onExit.run(),
                    gameService
            );
            header.setOnLeaderboard(() -> onLeaderboard.run());
            header.setOnNewGame(() -> onNewGame.run());
            node = new VBox(controls, header);
        } else if (includeNavHeader) {
            node = new VBox(controls);
        } else {
            node = controls;
        }
    }

    @Override public Node getNode()                       { return node; }
    @Override public Node getOverlayNode()                { return header != null ? header.getOverlayNode() : null; }
    @Override public void setOnDashboard(Runnable r)      { onDashboard = r; }
    @Override public void setOnExchange(Runnable r)       { onExchange = r; }
    @Override public void setOnLeaderboard(Runnable r)    { onLeaderboard = r; if (header != null) header.setOnLeaderboard(r); }
    @Override public void setOnNewGame(Runnable r)        { onNewGame = r; if (header != null) header.setOnNewGame(r); }
    @Override public void setOnSave(Runnable r)           { onSave = r; }
    @Override public void setOnExit(Runnable r)           { onExit = r; }
    @Override public void onGameUpdated()                 { if (header != null) header.onGameUpdated(); }
    @Override public void onLanguageChanged()             {}

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

        FontIcon closeIcon = new FontIcon("fth-x");
        closeIcon.getStyleClass().add(
            "title-bar-light".equals(styleClass) ? "title-bar-close-icon-light" : "title-bar-close-icon"
        );
        Button close = new Button();
        close.setGraphic(closeIcon);
        close.getStyleClass().addAll("title-bar-btn", "title-bar-btn-close");
        close.setOnAction(e -> stage.close());

        HBox controls = new HBox(spacer, minimize, maximize, close);
        controls.getStyleClass().add(styleClass);
        return controls;
    }
}
