package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.Header;
import edu.ntnu.idatt2003.millions.view.component.StatusFooter;
import edu.ntnu.idatt2003.millions.view.component.TitleBar;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.DashboardView;
import edu.ntnu.idatt2003.millions.view.exchange.ExchangeView;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/**
 * The main view of the application.
 * Contains a persistent {@link Header} and a content area that switches
 * between different views depending on user navigation.
 *
 * <p>Navigation between Dashboard and Exchange is handled internally as
 * purely visual state. Domain-related actions (save, exit, advance week)
 * are delegated to the controller via callbacks supplied at construction.</p>
 */
public class MainView {

    private final GameService gameService;
    private final PortfolioController portfolioController;
    private final BorderPane root;
    private final Header header;
    private final WeekBar weekBar;
    private final StatusFooter footer;

    /**
     * Constructs a new MainView with a custom title bar, header, and dashboard
     * as the default content.
     *
     * @param stage          the primary stage, used by the title bar for window controls
     * @param gameService    the game manager containing player and exchange
     * @param onSaveGame     callback invoked when the user clicks "Save game"
     * @param onExitGame     callback invoked when the user clicks "Exit game"
     * @param onAdvanceWeek  callback invoked when the user clicks "Advance week"
     */
    public MainView(Stage stage,
                    GameService gameService,
                    PortfolioController portfolioController,
                    Runnable onSaveGame,
                    Runnable onExitGame,
                    Runnable onAdvanceWeek) {
        this.gameService = gameService;
        this.portfolioController = portfolioController;
        this.weekBar = new WeekBar(gameService, onAdvanceWeek);
        this.header = new Header(
                this::showDashboard,
                this::showExchange,
                onSaveGame,
                onExitGame
        );
        this.footer = new StatusFooter(gameService);
        this.root = new BorderPane();
        root.getStyleClass().add("main-root");
        root.setTop(new VBox(new TitleBar(stage), header));
        root.setBottom(footer);

        // Clip all children to the rounded corner shape so no child
        // background bleeds into the transparent corner areas.
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(root.widthProperty());
        clip.heightProperty().bind(root.heightProperty());
        clip.setArcWidth(24);
        clip.setArcHeight(24);
        root.setClip(clip);

        Runnable updateCorners = () -> {
            boolean flat = stage.isMaximized() || stage.isFullScreen();
            clip.setArcWidth(flat ? 0 : 24);
            clip.setArcHeight(flat ? 0 : 24);
            if (flat) {
                root.getStyleClass().add("main-root-flat");
            } else {
                root.getStyleClass().remove("main-root-flat");
            }
        };
        stage.maximizedProperty().addListener((obs, old, val) -> updateCorners.run());
        stage.fullScreenProperty().addListener((obs, old, val) -> updateCorners.run());
        updateCorners.run();

        showDashboard();
    }

    /**
     * Returns the root layout of this view.
     *
     * @return the root BorderPane
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Switches the content area to the dashboard view.
     */
    private void showDashboard() {
        root.setCenter(wrapScrollable(
                new DashboardView(gameService, portfolioController, weekBar, this::showExchangeOnStocksTab)));
    }

    /**
     * Switches the content area to the exchange view.
     */
    private void showExchange() {
        root.setCenter(wrapScrollable(new ExchangeView(gameService, weekBar, portfolioController)));
    }

    private void showExchangeOnStocksTab() {
        ExchangeView exchangeView = new ExchangeView(gameService, weekBar, portfolioController);
        exchangeView.selectStocksTab();
        root.setCenter(wrapScrollable(exchangeView));
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