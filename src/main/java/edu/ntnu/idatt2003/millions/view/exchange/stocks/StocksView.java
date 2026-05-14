package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.card.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.card.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksInvestedCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksUnrealizedReturnCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards and a sortable, searchable,
 * paginated {@link StocksCard}.
 */
public class StocksView extends VBox {

    /**
     * Constructs a new StocksView.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, PortfolioController controller) {
        getStyleClass().add("content-area");

        HBox summaryCards = createSummaryCards(gameService);
        StocksCard stocksCard = new StocksCard(gameService, controller);

        getChildren().addAll(summaryCards, stocksCard);
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
