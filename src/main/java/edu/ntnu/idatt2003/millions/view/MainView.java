package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.Header;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.DashboardView;
import edu.ntnu.idatt2003.millions.view.exchange.ExchangeView;
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
        root.setCenter(new DashboardView(gameManager, weekBar));
    }

    /**
     * Switches the content area to the exchange view.
     */
    public void showExchange() {
        root.setCenter(new ExchangeView(gameManager, weekBar));
    }
}