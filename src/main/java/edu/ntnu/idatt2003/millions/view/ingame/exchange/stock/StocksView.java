package edu.ntnu.idatt2003.millions.view.ingame.exchange.stock;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.ingame.exchange.stock.card.StocksInvestedCard;
import edu.ntnu.idatt2003.millions.view.ingame.exchange.stock.card.StocksCard;
import edu.ntnu.idatt2003.millions.view.ingame.exchange.stock.card.StocksUnrealizedReturnCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards and a sortable, searchable,
 * paginated {@link StocksCard}.</p>
 *
 * <p>Implements {@link SearchFocusProvider} to support {@code Cmd/Ctrl+F}
 * by delegating to the {@link StocksCard} search field.</p>
 */
public class StocksView extends VBox implements SearchFocusProvider {

    private final StocksCard stocksCard;

    /**
     * Constructs a new StocksView.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, TradeController controller) {
        setSpacing(16);

        HBox summaryCards = createSummaryCards(gameService);
        stocksCard = new StocksCard(gameService, controller);

        getChildren().addAll(summaryCards, stocksCard);
    }

    /**
     * Focuses the stocks search field.
     */
    @Override
    public void focusSearch() {
        stocksCard.focusSearch();
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
