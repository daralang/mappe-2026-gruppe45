package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.ExploreStocksButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View wrapping the player's watchlist view displayed under the dashboard.
 * Shows the watchlist, list of stocks that user might want to keep track of,
 * with table of common stock info and action buttons, buy, note and remove.
 */
public class WatchlistView extends VBox {

    /**
     * Constructs a new WatchlistView.
     *
     * @param gameService     the game service containing player and exchange state
     * @param controller      the controller used to open buy dialogs
     * @param onExploreStocks callback invoked when the player clicks the explore button
     */
    public WatchlistView(GameService gameService,
                         TradeController controller,
                         Runnable onExploreStocks) {
        setSpacing(16);

        WatchlistCard card = new WatchlistCard(gameService, controller);
        VBox.setVgrow(card, Priority.ALWAYS);

        ExploreStocksButton exploreButton = new ExploreStocksButton(onExploreStocks);
        exploreButton.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(card,exploreButton);
    }
}
