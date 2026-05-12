package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksInvestedCard;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.card.StocksUnrealizedReturnCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange stocks tab.
 *
 * <p>Assembles four portfolio summary cards ({@link PortfolioValueCard},
 * {@link AvailableFundsCard}, {@link StocksInvestedCard},
 * {@link StocksUnrealizedReturnCard}) followed by a sortable, searchable
 * {@link StocksTable}.
 *
 * <p>All value queries are delegated to the individual card classes;
 * mutations are routed through {@link PortfolioController}.
 */
public class StocksView extends VBox {

    /**
     * Constructs a new StocksView.
     *
     * @param gameService the game service containing player and exchange state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksView(GameService gameService, PortfolioController controller) {
        setSpacing(16);
        getStyleClass().add("content-area");

        HBox cards = new HBox(16,
                withGrow(new PortfolioValueCard(gameService, "exchange.stocks.portfolio")),
                withGrow(new AvailableFundsCard(gameService, "exchange.stocks.available")),
                withGrow(new StocksInvestedCard(gameService)),
                withGrow(new StocksUnrealizedReturnCard(gameService))
        );

        getChildren().addAll(cards, new StocksTable(gameService, controller));
    }

    /**
     * Sets horizontal grow priority to ALWAYS for the given card and returns it.
     * Used to make summary cards fill the available width equally.
     *
     * @param card the card to configure
     * @return the same card with grow priority set
     */
    private VBox withGrow(VBox card) {
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
