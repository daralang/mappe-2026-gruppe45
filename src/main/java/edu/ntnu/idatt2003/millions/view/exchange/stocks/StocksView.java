package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksInvestedCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksUnrealizedReturnCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards and a searchbar, and a
 * sortable, paginated {@link StocksTable}.
 *
 * <p>Search is triggered explicitly by pressing Enter or clicking the search button,
 * delegated entirely to {@link SearchBar}. Sort state is managed by
 * {@link StocksSort} inside the table. This view only wires the components together
 * and updates the search bar status on each game state change.
 */
public class StocksView extends VBox implements GameObserver {

    private final StocksTable stocksTable;
    private final SearchBar searchBar;
    private final GameService gameService;

    /**
     * Constructs a new StocksView.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, PortfolioController controller) {
        this.gameService = gameService;
        this.stocksTable = new StocksTable(gameService, controller);
        this.searchBar = new SearchBar(
                "exchange.stocks.search.placeholder",
                "search.button",
                term -> {
                    stocksTable.filter(term);
                });

        setSpacing(16);
        getStyleClass().add("content-area");

        HBox cards = new HBox(16,
                withGrow(new PortfolioValueCard(gameService, "exchange.stocks.portfolio")),
                withGrow(new AvailableFundsCard(gameService, "exchange.stocks.available")),
                withGrow(new StocksInvestedCard(gameService)),
                withGrow(new StocksUnrealizedReturnCard(gameService))
        );

        gameService.addObserver(this);

        getChildren().addAll(cards, searchBar, stocksTable);
    }

    /**
     * Called when the game state changes.
     */
    @Override
    public void onGameUpdated() {
    }

    /**
     * Sets horizontal grow priority to ALWAYS for the given card and returns it.
     *
     * @param card the card to configure
     * @return the same card with grow priority set
     */
    private VBox withGrow(VBox card) {
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
