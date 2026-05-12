package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksInvestedCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksListCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksUnrealizedReturnCard;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards (each with subtitles), a search bar
 * with an inline clear sort button, a status label, and a sortable paginated
 * {@link StocksListCard}.
 *
 * <p>Search is triggered explicitly by pressing Enter or clicking the search button,
 * delegated entirely to {@link SearchBar}. Sort state is managed by
 * {@link StocksSort} inside the table. The clear sort button is shown only when a
 * sort is active and is placed right-aligned on the same row as the search bar via
 * {@link StocksListCard#setOnSortChanged(Runnable)}.
 */
public class StocksView extends VBox implements GameObserver {

    private final StocksListCard stocksListCard;
    private final SearchBar searchBar;
    private final GameService gameService;
    private final StyledText statusLabel = StyledText.widgetLabel();

    /**
     * Constructs a new StocksView.
     *
     * <p>Wires the search bar to {@link StocksListCard#filter(String)} and registers
     * a sort-change callback so the clear sort button visibility and the status
     * label are kept in sync with the table state.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, PortfolioController controller) {
        this.gameService = gameService;
        this.stocksListCard = new StocksListCard(gameService, controller);
        this.searchBar = new SearchBar(
                "exchange.stocks.search.placeholder",
                "search.button",
                term -> {
                    stocksListCard.filter(term);
                    updateStatus();
                });

        Button clearSortButton = new Button(LanguageManager.get("exchange.stocks.sort.clear"));
        clearSortButton.getStyleClass().add("search-clear-button");
        clearSortButton.setOpacity(0);
        clearSortButton.setOnAction(e -> stocksListCard.clearSort());

        stocksListCard.setOnSortChanged(() -> {
            clearSortButton.setOpacity(stocksListCard.isSortActive() ? 1 : 0);
            updateStatus();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox searchRow = new HBox(8, searchBar, spacer, clearSortButton);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        setSpacing(16);
        getStyleClass().add("content-area");

        HBox cards = new HBox(16,
                withGrow(new PortfolioValueCard(gameService,
                        "exchange.stocks.portfolio", "exchange.stocks.portfolio.sub")),
                withGrow(new AvailableFundsCard(gameService,
                        "exchange.stocks.available", "exchange.stocks.available.sub")),
                withGrow(new StocksInvestedCard(gameService,
                        "exchange.stocks.invested.positions")),
                withGrow(new StocksUnrealizedReturnCard(gameService,
                        "exchange.stocks.unrealized.sub"))
        );

        gameService.addObserver(this);

        getChildren().addAll(cards, searchRow, statusLabel, stocksListCard);
    }

    /**
     * Updates the status label to reflect the current filtered and total stock counts.
     * Uses the {@code exchange.stocks.status} i18n key with two positional arguments.
     */
    private void updateStatus() {
        statusLabel.setText(MessageFormat.format(
                LanguageManager.get("exchange.stocks.status"),
                stocksListCard.getFilteredCount(),
                stocksListCard.getTotalCount()));
    }

    /**
     * Called when the game state changes. Refreshes the status label to reflect
     * any change in the total or filtered stock count.
     */
    @Override
    public void onGameUpdated() {
        updateStatus();
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
