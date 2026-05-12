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
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards, a search bar with an inline clear sort
 * button, and a sortable, paginated {@link StocksTable}.
 *
 * <p>Search is triggered explicitly by pressing Enter or clicking the search button,
 * delegated entirely to {@link SearchBar}. Sort state is managed by
 * {@link StocksSort} inside the table. The clear sort button is shown only when a
 * sort is active and is placed right-aligned on the same row as the search bar via
 * {@link StocksTable#setOnSortChanged(Runnable)}.
 */
public class StocksView extends VBox implements GameObserver {

    private final StocksTable stocksTable;
    private final SearchBar searchBar;
    private final GameService gameService;

    /**
     * Constructs a new StocksView.
     *
     * <p>Wires the search bar to {@link StocksTable#filter(String)} and registers
     * a sort-change callback so the clear sort button visibility tracks the active
     * sort state via opacity.
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
                term -> stocksTable.filter(term));

        Button clearSortButton = new Button(LanguageManager.get("exchange.stocks.sort.clear"));
        clearSortButton.getStyleClass().add("clear-button");
        clearSortButton.setOpacity(0);
        clearSortButton.setOnAction(e -> stocksTable.clearSort());

        stocksTable.setOnSortChanged(() ->
                clearSortButton.setOpacity(stocksTable.isSortActive() ? 1 : 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox searchRow = new HBox(8, searchBar, spacer, clearSortButton);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        setSpacing(16);
        getStyleClass().add("content-area");

        HBox cards = new HBox(16,
                withGrow(new PortfolioValueCard(gameService, "exchange.stocks.portfolio")),
                withGrow(new AvailableFundsCard(gameService, "exchange.stocks.available")),
                withGrow(new StocksInvestedCard(gameService)),
                withGrow(new StocksUnrealizedReturnCard(gameService))
        );

        gameService.addObserver(this);

        getChildren().addAll(cards, searchRow, stocksTable);
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
