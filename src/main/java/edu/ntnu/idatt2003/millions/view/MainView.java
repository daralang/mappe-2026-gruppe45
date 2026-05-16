package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.controller.MainController;
import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusRegistry;
import edu.ntnu.idatt2003.millions.keyboard.TabNavigationRegistry;
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
 * <p>Navigation between Dashboard, Exchange and Leaderboard is handled
 * internally as purely visual state. The {@link DashboardView} is cached
 * so that sub-views survive tab switches and keyboard shortcuts
 * (Shift+1–4) can jump directly to a dashboard tab.</p>
 *
 * <p>Notifies the injected {@link SearchFocusRegistry} whenever the active
 * view changes, so the {@code Cmd/Ctrl+F} shortcut registered in
 * {@link MainController} always
 * reaches the correct search field without coupling the controller to this view.</p>
 *
 * <p>Domain-related actions (save, exit, advance week) are delegated to
 * the controller via callbacks supplied at construction.</p>
 */
public class MainView {

    private final GameService gameService;
    private final TradeController tradeController;
    private final LoanController loanController;
    private final ToastService toastService;
    private final SearchFocusRegistry searchFocusRegistry;
    private final TabNavigationRegistry tabNavigationRegistry;
    private final StackPane outerRoot;
    private final BorderPane content;
    private final WeekBar weekBar;
    private final StatusFooter footer;

    private DashboardView dashboardView;
    private ScrollPane dashboardScrollable;

    /**
     * Constructs a new MainView with a platform-appropriate title bar and
     * dashboard as the default content.
     *
     * @param stage               the primary stage, used for window-state listeners
     * @param gameService         the game manager containing player and exchange
     * @param titleBar            the platform title bar; save/exit callbacks already wired by the controller
     * @param onAdvanceWeek       callback invoked when the user clicks "Advance week"
     * @param searchFocusRegistry   registry updated whenever the active view changes,
     *                              allowing the controller to trigger search focus
     *                              without depending on this view directly
     * @param tabNavigationRegistry registry updated whenever the active view changes when tab changes
     */
    public MainView(Stage stage,
                    GameService gameService,
                    TradeController tradeController,
                    LoanController loanController,
                    TitleBar titleBar,
                    Runnable onAdvanceWeek,
                    ToastService toastService,
                    SearchFocusRegistry searchFocusRegistry,
                    TabNavigationRegistry tabNavigationRegistry) {
        this.gameService = gameService;
        this.tradeController = tradeController;
        this.loanController = loanController;
        this.toastService = toastService;
        this.searchFocusRegistry = searchFocusRegistry;
        this.tabNavigationRegistry = tabNavigationRegistry;
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
     * The {@link DashboardView} is created once and reused on subsequent calls
     * so sub-view state (lazy init, scroll position) survives navigation.
     */
    public void showDashboard() {
        if (dashboardScrollable == null) {
            dashboardView = new DashboardView(
                    gameService, tradeController, loanController, weekBar, this::showExchangeOnStocksTab);
            dashboardScrollable = wrapScrollable(dashboardView);
        }
        searchFocusRegistry.setActive(dashboardView);
        tabNavigationRegistry.setActive(dashboardView::showTab);
        content.setCenter(dashboardScrollable);
    }

    /**
     * Navigates to the dashboard and activates the Portfolio tab (Shift+1).
     */
    public void showDashboardPortfolio() {
        showDashboard();
        dashboardView.showPortfolio();
    }

    /**
     * Navigates to the dashboard and activates the Transactions tab (Shift+2).
     */
    public void showDashboardTransactions() {
        showDashboard();
        dashboardView.showTransactions();
    }

    /**
     * Navigates to the dashboard and activates the Watchlist tab (Shift+3).
     */
    public void showDashboardWatchlist() {
        showDashboard();
        dashboardView.showWatchlist();
    }

    /**
     * Navigates to the dashboard and activates the Loans tab (Shift+4).
     */
    public void showDashboardLoans() {
        showDashboard();
        dashboardView.showLoans();
    }

    /**
     * Switches the content area to the exchange view.
     */
    public void showExchange() {
        ExchangeView exchangeView = new ExchangeView(gameService, weekBar, tradeController);
        searchFocusRegistry.setActive(exchangeView);
        tabNavigationRegistry.setActive(exchangeView::showTab);
        content.setCenter(wrapScrollable(exchangeView));
    }

    /**
     * Switches the content area to the leaderboard view.
     */
    public void showLeaderboard() {
        LeaderboardView leaderboardView = new LeaderboardView(gameService, toastService, weekBar);
        searchFocusRegistry.setActive(leaderboardView);
        tabNavigationRegistry.setActive(null);
        content.setCenter(wrapScrollable(leaderboardView));
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
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.getStyleClass().add("content-scroll");
        return scrollPane;
    }
}