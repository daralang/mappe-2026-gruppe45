package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.Header;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.DashboardView;
import edu.ntnu.idatt2003.millions.view.exchange.ExchangeView;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;

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

    private final GameManager gameManager;
    private final PortfolioController portfolioController;
    private final BorderPane root;
    private final Header header;
    private final WeekBar weekBar;

    /**
     * Constructs a new MainView with a header and dashboard as the default content.
     *
     * @param gameManager    the game manager containing player and exchange
     * @param onSaveGame     callback invoked when the user clicks "Save game"
     * @param onExitGame     callback invoked when the user clicks "Exit game"
     * @param onAdvanceWeek  callback invoked when the user clicks "Advance week"
     */
    public MainView(GameManager gameManager,
                    PortfolioController portfolioController,
                    Runnable onSaveGame,
                    Runnable onExitGame,
                    Runnable onAdvanceWeek) {
        this.gameManager = gameManager;
        this.portfolioController = portfolioController;
        this.weekBar = new WeekBar(gameManager, onAdvanceWeek);
        this.header = new Header(
                this::showDashboard,
                this::showExchange,
                onSaveGame,
                onExitGame
        );
        this.root = new BorderPane();
        root.setTop(header);
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
        root.setCenter(wrapScrollable(new DashboardView(gameManager, portfolioController, weekBar)));
    }

    /**
     * Switches the content area to the exchange view.
     */
    private void showExchange() {
        root.setCenter(wrapScrollable(new ExchangeView(gameManager, weekBar)));
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