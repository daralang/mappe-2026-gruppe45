package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.service.GameService;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View wrapping the player's watchlist.
 *
 * <p>Acts as a thin layout container that instantiates {@link WatchlistCard}
 * and lets it fill the available vertical space. Tab navigation and lazy
 * initialization are handled by the parent {@link edu.ntnu.idatt2003.millions.view.dashboard.DashboardView}.</p>
 */
public class WatchlistView extends VBox {

    /**
     * Constructs a new WatchlistView.
     *
     * @param gameService     the game service containing player and exchange state
     * @param tradeController the controller used to open buy dialogs
     * @param onExploreStocks callback invoked when the player clicks the explore button
     */
    public WatchlistView(GameService gameService,
                         TradeController tradeController,
                         Runnable onExploreStocks) {
        WatchlistCard card = new WatchlistCard(gameService, tradeController, onExploreStocks);
        VBox.setVgrow(card, Priority.ALWAYS);
        getChildren().add(card);
    }
}
