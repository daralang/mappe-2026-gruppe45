// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Windows implementation of {@link TitleBar} with a custom drag region and window controls
 * (minimise, maximise/restore, close).
 *
 * <p>When a {@link GameService} is provided, a {@link Header} navigation bar is
 * placed below the controls strip. Without one, only the controls strip is shown,
 * as on the start screen.</p>
 *
 * <p>The close button delegates to a {@link #setOnCloseRequest(Runnable)} callback
 * rather than closing the stage directly, so the controller can route the request
 * through the exit-confirmation flow. The default callback closes the stage, which
 * is the desired behaviour on the start screen where no game is in progress.</p>
 */
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
    private Runnable onCloseRequest;

    private final Node node;
    private Header header;

    /**
     * Constructs the full game-screen title bar with navigation header.
     *
     * @param stage       the primary stage, used for window control actions
     * @param gameService the game service passed to the {@link Header} for notification state
     */
    public WindowsTitleBar(Stage stage, GameService gameService) {
        this(stage, gameService, "title-bar", true);
    }

    /**
     * Constructs a title bar without a navigation header, using a custom controls style class.
     *
     * @param stage              the primary stage
     * @param controlsStyleClass the CSS class applied to the controls strip
     * @param includeNavHeader   {@code true} to show an empty controls strip, {@code false} for controls only
     */
    WindowsTitleBar(Stage stage, String controlsStyleClass, boolean includeNavHeader) {
        this(stage, null, controlsStyleClass, includeNavHeader);
    }

    /**
     * Full constructor used by the delegating overloads.
     *
     * @param stage              the primary stage
     * @param gameService        the game service, or {@code null} to omit the navigation header
     * @param controlsStyleClass the CSS class applied to the controls strip
     * @param includeNavHeader   {@code true} to include a header row below the controls strip
     */
    WindowsTitleBar(Stage stage, GameService gameService,
                    String controlsStyleClass, boolean includeNavHeader) {
        this.onCloseRequest = stage::close;
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
    @Override public void setOnCloseRequest(Runnable r)   { onCloseRequest = r; }
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
        close.setOnAction(e -> onCloseRequest.run());

        HBox controls = new HBox(spacer, minimize, maximize, close);
        controls.getStyleClass().add(styleClass);
        return controls;
    }
}
