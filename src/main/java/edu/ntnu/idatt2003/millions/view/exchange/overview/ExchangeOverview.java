package edu.ntnu.idatt2003.millions.view.exchange.overview;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.GainersCard;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.LosersCard;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.StockRankingCard;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.TotalStocksCard;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange overview tab.
 * Assembles market summary cards ({@link TotalStocksCard}, {@link GainersCard},
 * {@link LosersCard}) and ranked winner/loser tables ({@link StockRankingCard}).
 *
 * <p>Every card registers as its own observer and refreshes itself, so this view
 * only assembles the layout and holds no observer logic of its own.</p>
 */
public class ExchangeOverview extends VBox {

    private static final int RANKING_LIMIT = 5;

    /**
     * Constructs a new ExchangeOverview.
     *
     * @param gameService the game manager containing player and exchange
     * @throws NullPointerException if gameService is null
     */
    public ExchangeOverview(GameService gameService) {
        setSpacing(16);

        HBox statCards = new HBox(16,
                withGrow(new TotalStocksCard(gameService)),
                withGrow(new GainersCard(gameService)),
                withGrow(new LosersCard(gameService))
        );

        StockRankingCard winnersTable = new StockRankingCard(
                gameService, "exchange.overview.weeklyWinners",
                exchange -> exchange.getGainers(RANKING_LIMIT));
        StockRankingCard losersTable = new StockRankingCard(
                gameService, "exchange.overview.weeklyLosers",
                exchange -> exchange.getLosers(RANKING_LIMIT));

        winnersTable.setMaxWidth(Double.MAX_VALUE);
        losersTable.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints leftCol = new ColumnConstraints();
        leftCol.setPercentWidth(50);
        leftCol.setHgrow(Priority.ALWAYS);

        ColumnConstraints rightCol = new ColumnConstraints();
        rightCol.setPercentWidth(50);
        rightCol.setHgrow(Priority.ALWAYS);

        GridPane tables = new GridPane();
        tables.setHgap(16);
        tables.getColumnConstraints().addAll(leftCol, rightCol);
        tables.add(winnersTable, 0, 0);
        tables.add(losersTable, 1, 0);

        getChildren().addAll(statCards, tables);
    }

    /**
     * Sets horizontal grow priority to ALWAYS for the given card and returns it.
     * Used to make stat cards fill the available width equally.
     *
     * @param card the card to configure
     * @return the same card with grow priority set
     */
    private VBox withGrow(VBox card) {
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
}
