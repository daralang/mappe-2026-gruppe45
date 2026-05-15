package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.view.component.Header;
import edu.ntnu.idatt2003.millions.view.component.toast.ToastOverlay;
import edu.ntnu.idatt2003.millions.view.component.StatusFooter;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.titlebar.TitleBar;
import edu.ntnu.idatt2003.millions.view.dashboard.DashboardView;
import edu.ntnu.idatt2003.millions.view.exchange.ExchangeView;
import edu.ntnu.idatt2003.millions.view.leaderboard.LeaderboardView;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * The main view of the application.
 * Contains a persistent {@link TitleBar} and a content area that switches
 * between different views depending on user navigation.
 *
 * <p>Navigation between Dashboard and Exchange is handled internally as
 * purely visual state. Domain-related actions (save, exit, advance week)
 * are delegated to the controller via callbacks supplied at construction.</p>
 */
public class MainView {

    private final GameService gameService;
    private final TradeController tradeController;
    private final LoanController loanController;
    private final ToastService toastService;
    private final StackPane outerRoot;
    private final BorderPane content;
    private final WeekBar weekBar;
    private final StatusFooter footer;

    /**
     * Constructs a new MainView with a platform-appropriate title bar and
     * dashboard as the default content.
     *
     * @param stage          the primary stage, used for window-state listeners
     * @param gameService    the game manager containing player and exchange
     * @param titleBar       the platform title bar; save/exit callbacks already wired by the controller
     * @param onAdvanceWeek  callback invoked when the user clicks "Advance week"
     */
    public MainView(Stage stage,
                    GameService gameService,
                    TradeController tradeController,
                    LoanController loanController,
                    TitleBar titleBar,
                    Runnable onAdvanceWeek,
                    ToastService toastService) {
        this.gameService = gameService;
        this.tradeController = tradeController;
        this.loanController = loanController;
        this.toastService = toastService;
        this.weekBar = new WeekBar(gameService, onAdvanceWeek);
        titleBar.setOnDashboard(this::showDashboard);
        titleBar.setOnExchange(this::showExchange);
        titleBar.setOnLeaderboard(this::showLeaderboard);
        this.footer = new StatusFooter(gameService);
        this.content = new BorderPane();
        content.getStyleClass().add("main-root");
        content.setTop(titleBar.getNode());
        content.setBottom(footer);

        // Clip all children to the rounded corner shape so no child
        // background bleeds into the transparent corner areas.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(content.widthProperty());
        clip.heightProperty().bind(content.heightProperty());
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        content.setClip(clip);

        Runnable updateCorners = () -> {
            boolean flat = stage.isMaximized() || stage.isFullScreen();
            clip.setArcWidth(flat ? 0 : 24);
            clip.setArcHeight(flat ? 0 : 24);
            if (flat) {
                content.getStyleClass().add("main-root-flat");
            } else {
                content.getStyleClass().remove("main-root-flat");
            }
        };
        stage.maximizedProperty().addListener((obs, old, val) -> updateCorners.run());
        stage.fullScreenProperty().addListener((obs, old, val) -> updateCorners.run());
        updateCorners.run();

        this.outerRoot = new StackPane(content);

        ToastOverlay toastOverlay = new ToastOverlay(toastService);
        toastOverlay.translateYProperty().bind(
                Bindings.createDoubleBinding(
                        () -> -(footer.getLayoutBounds().getHeight() + 8),
                        footer.layoutBoundsProperty()));
        outerRoot.getChildren().add(toastOverlay);

        Node overlay = titleBar.getOverlayNode();
        if (overlay != null) {
            StackPane.setAlignment(overlay, Pos.TOP_RIGHT);
            StackPane.setMargin(overlay, new Insets(0, 16, 0, 0));
            Node titleBarNode = titleBar.getNode();
            overlay.translateYProperty().bind(
                    Bindings.createDoubleBinding(
                            () -> titleBarNode.getLayoutBounds().getHeight() + 8,
                            titleBarNode.layoutBoundsProperty()));
            outerRoot.getChildren().add(overlay);
        }

        showDashboard();
    }

    /**
     * Returns the root layout of this view.
     *
     * @return the root StackPane
     */
    public StackPane getRoot() {
        return outerRoot;
    }

    /**
     * Switches the content area to the dashboard view.
     */
    private void showDashboard() {
        content.setCenter(wrapScrollable(
                new DashboardView(gameService, tradeController, loanController, weekBar, this::showExchangeOnStocksTab)));
    }

    /**
     * Switches the content area to the exchange view.
     */
    private void showExchange() {
        content.setCenter(wrapScrollable(new ExchangeView(gameService, weekBar, tradeController)));
    }

    private void showLeaderboard() {
        content.setCenter(wrapScrollable(new LeaderboardView(gameService, toastService)));
    }

    private void showExchangeOnStocksTab() {
        ExchangeView exchangeView = new ExchangeView(gameService, weekBar, tradeController);
        exchangeView.selectStocksTab();
        content.setCenter(wrapScrollable(exchangeView));
    }

    /**
     * Wraps a view node in a vertically scrollable {@link ScrollPane}.
     * Horizontal scrolling is disabled; the content fills the pane's width.
     *
     * @param content the view node to wrap
     * @return a configured ScrollPane containing the content
     */
    private ScrollPane wrapScrollable(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }
}