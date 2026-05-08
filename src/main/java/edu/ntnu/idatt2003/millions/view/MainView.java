package edu.ntnu.idatt2003.millions.view;

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
 */
public class MainView {

    private final GameManager gameManager;
    private final BorderPane root;
    private final Header header;
    private final WeekBar weekBar;

    /**
     * Constructs a new MainView with a header and dashboard as the default content.
     */
    public MainView(GameManager gameManager) {
        this.gameManager = gameManager;
        this.weekBar = new WeekBar(gameManager);
        header = new Header();
        root = new BorderPane();
        root.setTop(header);
        showDashboard();
    }

    /**
     * Returns the header component.
     *
     * @return the header
     */
    public Header getHeader() {
        return header;
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
     * Returns the weekbar component
     *
     * @return the weekbar
     */
    public WeekBar getWeekBar() {
        return weekBar;
    }

    /**
     * Switches the content area to the dashboard view.
     */
    public void showDashboard() {
        root.setCenter(wrapScrollable(new DashboardView(gameManager, weekBar)));
    }

    /**
     * Switches the content area to the exchange view.
     */
    public void showExchange() {
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