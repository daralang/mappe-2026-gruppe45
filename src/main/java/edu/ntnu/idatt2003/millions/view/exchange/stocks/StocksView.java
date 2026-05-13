package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.card.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.card.PortfolioValueCard;
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
 * <p>Assembles four portfolio summary cards, a search bar, a status label,
 * sort actions, and a sortable paginated {@link StocksListCard}.
 *
 * <p>Search is triggered explicitly by pressing Enter or clicking the search button,
 * delegated entirely to {@link SearchBar}. Sort state is managed by
 * {@link StocksSort} inside the table.
 */
public class StocksView extends VBox {

    private final StocksListCard stocksListCard;
    private final StyledText statusLabel = StyledText.widgetLabel();
    private final Pagination pagination;

    /**
     * Constructs a new StocksView.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, PortfolioController controller) {
        this.stocksListCard = new StocksListCard(gameService, controller);
        this.pagination = new Pagination(StocksListCard.PAGE_SIZE, stocksListCard::setPage);

        Button clearSortButton = createClearSortButton();
        HBox cards = createSummaryCards(gameService);
        HBox searchRow = createSearchRow();
        HBox listHeader = createListHeader(clearSortButton);
        Region spacer = createSpacer(12);

        configureRefreshCallback(clearSortButton);
        getStyleClass().add("content-area");
        getChildren().addAll(cards, spacer, searchRow, listHeader, stocksListCard, pagination);
        updateMetaInfo();
    }

    private HBox createSearchRow() {
        SearchBar searchBar = new SearchBar(
                "exchange.stocks.search.placeholder",
                "search.button",
                term -> {
                    stocksListCard.filter(term);
                    updateMetaInfo();
                });

        HBox searchRow = new HBox(8, searchBar);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        return searchRow;
    }

    private Button createClearSortButton() {
        Button button = new Button(LanguageManager.get("exchange.stocks.sort.clear"));
        button.getStyleClass().add("clear-sort-button");
        button.setVisible(false);
        button.setOnAction(e -> stocksListCard.clearSort());
        return button;
    }

    private HBox createListHeader(Button clearSortButton) {
        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);
        HBox listHeader = new HBox(statusLabel, statusSpacer, clearSortButton);
        listHeader.setAlignment(Pos.CENTER_LEFT);
        return listHeader;
    }

    private void configureRefreshCallback(Button clearSortButton) {
        stocksListCard.setOnRefreshed(() -> {
            boolean sortActive = stocksListCard.isSortActive();
            clearSortButton.setVisible(sortActive);
            updateMetaInfo();
        });
    }

    private HBox createSummaryCards(GameService gameService) {
        return new HBox(16,
                withGrow(new PortfolioValueCard(gameService,
                        "exchange.stocks.portfolio", "exchange.stocks.portfolio.sub")),
                withGrow(new AvailableFundsCard(gameService,
                        "exchange.stocks.available", "exchange.stocks.available.sub")),
                withGrow(new StocksInvestedCard(gameService,
                        "exchange.stocks.invested.positions")),
                withGrow(new StocksUnrealizedReturnCard(gameService,
                        "exchange.stocks.unrealized.sub"))
        );
    }

    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setMinHeight(height);
        return spacer;
    }

    private void updateMetaInfo() {
        pagination.update(stocksListCard.getCurrentPage(), stocksListCard.getFilteredCount());
        updateStatus();
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
