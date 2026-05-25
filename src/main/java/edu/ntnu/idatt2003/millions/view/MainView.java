package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.controller.MainController;
import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.keyboard.PageArrowDispatcher;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusRegistry;
import edu.ntnu.idatt2003.millions.keyboard.TabNavigationRegistry;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.view.component.toast.ToastOverlay;
import edu.ntnu.idatt2003.millions.view.component.KeyboardScrollPane;
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
 * internally as purely visual state. All three top-level views are cached
 * after their first creation so that sub-view state (active tab, scroll
 * position) survives navigation, and keyboard shortcuts (Shift+1–4) can
 * jump directly to a dashboard tab.</p>
 *
 * <p>Each view owns its own {@link WeekBar} instance. Because a JavaFX node
 * can only have one parent at a time, sharing a single instance across views
 * would cause it to disappear from whichever view last lost focus. Giving
 * each view its own instance avoids this node-stealing issue without
 * requiring any cleanup logic, since all three views live for the entire
 * application lifetime.</p>
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
    private final Runnable onAdvanceWeek;
    private final SearchFocusRegistry searchFocusRegistry;
    private final TabNavigationRegistry tabNavigationRegistry;
    private final StackPane outerRoot;
    private final BorderPane content;
    private final StatusFooter footer;

    private DashboardView dashboardView;
    private KeyboardScrollPane dashboardScrollable;
    private ExchangeView exchangeView;
    private KeyboardScrollPane exchangeScrollable;
    private LeaderboardView leaderboardView;
    private KeyboardScrollPane leaderboardScrollable;

    /** The scroll pane of the currently shown view, supplied to the page-arrow dispatcher. */
    private KeyboardScrollPane activeScrollable;

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
        this.onAdvanceWeek = onAdvanceWeek;
        this.searchFocusRegistry = searchFocusRegistry;
        this.tabNavigationRegistry = tabNavigationRegistry;
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
        new PageArrowDispatcher(() -> activeScrollable).install(outerRoot);

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
     * so sub-view state (active tab, scroll position) survives navigation.
     */
    public void showDashboard() {
        if (dashboardScrollable == null) {
            dashboardView = new DashboardView(
                    gameService, tradeController, loanController, createWeekBar(), this::showExchangeOnStocksTab);
            dashboardScrollable = wrapScrollable(dashboardView);
        }
        searchFocusRegistry.setActive(dashboardView);
        tabNavigationRegistry.setActive(dashboardView::showTab);
        activeScrollable = dashboardScrollable;
        content.setCenter(dashboardScrollable);
    }

    /**
     * Switches the content area to the exchange view.
     * The {@link ExchangeView} is created once and reused on subsequent calls
     * so sub-view state (active tab, scroll position) survives navigation.
     */
    public void showExchange() {
        ensureExchangeView();
        searchFocusRegistry.setActive(exchangeView);
        tabNavigationRegistry.setActive(exchangeView::showTab);
        activeScrollable = exchangeScrollable;
        content.setCenter(exchangeScrollable);
    }

    /**
     * Switches the content area to the leaderboard view.
     * The {@link LeaderboardView} is created once and reused on subsequent calls
     * so sub-view state (scroll position) survives navigation.
     */
    public void showLeaderboard() {
        if (leaderboardScrollable == null) {
            leaderboardView = new LeaderboardView(gameService, toastService, createWeekBar());
            leaderboardScrollable = wrapScrollable(leaderboardView);
        }
        searchFocusRegistry.setActive(leaderboardView);
        tabNavigationRegistry.setActive(null);
        activeScrollable = leaderboardScrollable;
        content.setCenter(leaderboardScrollable);
    }

    /**
     * Switches the content area to the exchange view with the stocks tab pre-selected.
     * Reuses the cached {@link ExchangeView} if it already exists.
     */
    private void showExchangeOnStocksTab() {
        ensureExchangeView();
        exchangeView.selectStocksTab();
        searchFocusRegistry.setActive(exchangeView);
        tabNavigationRegistry.setActive(exchangeView::showTab);
        activeScrollable = exchangeScrollable;
        content.setCenter(exchangeScrollable);
    }

    /**
     * Lazily initialises the {@link ExchangeView} and its {@link WeekBar} on first use.
     * Subsequent calls are no-ops.
     */
    private void ensureExchangeView() {
        if (exchangeScrollable == null) {
            exchangeView = new ExchangeView(gameService, createWeekBar(), tradeController);
            exchangeScrollable = wrapScrollable(exchangeView);
        }
    }

    /**
     * Creates a new {@link WeekBar} bound to the current game service and advance-week callback.
     * Each top-level view owns its own instance to avoid JavaFX node-stealing,
     * since a node can only belong to one parent at a time.
     *
     * @return a new {@link WeekBar} instance
     */
    private WeekBar createWeekBar() {
        return new WeekBar(gameService, onAdvanceWeek);
    }

    /**
     * Wraps a view node in a {@link KeyboardScrollPane} so it is vertically
     * scrollable and responds to arrow-key scrolling regardless of focus.
     *
     * @param content the view node to wrap
     * @return a configured scroll pane containing the content
     */
    private KeyboardScrollPane wrapScrollable(Node content) {
        return new KeyboardScrollPane(content);
    }
}
