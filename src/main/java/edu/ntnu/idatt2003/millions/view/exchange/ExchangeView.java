package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.keyboard.TabNavigationRegistry;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.exchange.overview.ExchangeOverview;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The exchange view of the application.
 * Contains a tab bar for navigating between overview and stocks.
 * Tab navigation is purely visual state and handled internally by this view.
 */
public class ExchangeView extends VBox implements SearchFocusProvider {

    private final GameService gameService;
    private final TradeController controller;
    private final ViewHeader viewHeader;
    private final VBox contentArea;
    private SearchFocusProvider activeTabProvider;

    /**
     * Constructs a new ExchangeView with the overview tab active.
     *
     * @param gameService the game manager containing player and exchange
     * @param weekBar     the week bar shared with the rest of the application
     * @param controller  the portfolio controller used to open buy/sell dialogs
     */
    public ExchangeView(GameService gameService, WeekBar weekBar, TradeController controller) {
        this.gameService = gameService;
        this.controller = controller;
        getStyleClass().add("content-area");

        viewHeader = new ViewHeader(
                "exchange.title",
                List.of(
                        "exchange.tab.overview",
                        "exchange.tab.stocks"
                ),
                weekBar
        );

        contentArea = new VBox();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        viewHeader.setTabAction(0, this::showOverview);
        viewHeader.setTabAction(1, this::showStocks);

        getChildren().addAll(viewHeader, contentArea);
        showOverview();
    }

    /**
     * Switches to the tab at the given zero-based index.
     * Called by {@link TabNavigationRegistry} when a {@code Shift+N} shortcut fires.
     * Indices out of range are ignored.
     *
     * @param index 0 = Overview, 1 = Stocks
     */
    public void showTab(int index) {
        switch (index) {
            case 0 -> { viewHeader.setActive(viewHeader.getTabButton(0)); showOverview(); }
            case 1 -> { viewHeader.setActive(viewHeader.getTabButton(1)); showStocks(); }
            default -> { }
        }
    }

    /**
     * Activates the stocks tab and shows its content.
     * Called from outside this view to navigate users directly to the
     * stocks tab (e.g. from the "Explore other stocks" button).
     */
    public void selectStocksTab() {
        viewHeader.setActive(viewHeader.getTabButton(1));
        showStocks();
    }

    /**
     * Focuses the search field of the currently active exchange tab.
     * The overview tab has no search bar; in that case this method is a no-op.
     */
    @Override
    public void focusSearch() {
        if (activeTabProvider != null) {
            activeTabProvider.focusSearch();
        }
    }

    private void showOverview() {
        activeTabProvider = null;
        contentArea.getChildren().setAll(new ExchangeOverview(gameService));
    }

    private void showStocks() {
        StocksView stocksView = new StocksView(gameService, controller);
        activeTabProvider = stocksView;
        contentArea.getChildren().setAll(stocksView);
    }
}