package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.GainersCard;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.LosersCard;
import edu.ntnu.idatt2003.millions.view.exchange.overview.StockRankingTable;
import edu.ntnu.idatt2003.millions.view.exchange.overview.card.TotalStocksCard;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange overview tab.
 * Assembles market summary cards ({@link TotalStocksCard}, {@link GainersCard},
 * {@link LosersCard}) and ranked winner/loser tables ({@link StockRankingTable}).
 * Each card manages its own observer registration and updates.
 * This view registers itself as a {@link GameObserver} only to refresh the
 * ranking tables, which are not self-updating components.
 */
public class ExchangeOverview extends VBox implements GameObserver {

    private final GameManager gameManager;
    private final StockRankingTable winnersTable;
    private final StockRankingTable losersTable;

    private static final int RANKING_LIMIT = 5;

    /**
     * Constructs a new ExchangeOverview.
     *
     * @param gameManager the game manager containing player and exchange
     * @throws NullPointerException if gameManager is null
     */
    public ExchangeOverview(GameManager gameManager) {
        this.gameManager = gameManager;
        gameManager.addObserver(this);
        setSpacing(16);
        getStyleClass().add("content-area");

        HBox statCards = new HBox(16,
                withGrow(new TotalStocksCard(gameManager)),
                withGrow(new GainersCard(gameManager)),
                withGrow(new LosersCard(gameManager))
        );

        Exchange exchange = gameManager.getExchange();
        winnersTable = new StockRankingTable(
                "exchange.overview.weeklyWinners",
                exchange.getGainers(RANKING_LIMIT)
        );
        losersTable = new StockRankingTable(
                "exchange.overview.weeklyLosers",
                exchange.getLosers(RANKING_LIMIT)
        );

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

    /**
     * Called when the game state has changed.
     * Refreshes the ranking tables with updated gainers and losers.
     * Stat cards ({@link TotalStocksCard}, {@link GainersCard}, {@link LosersCard})
     * update themselves via their own {@link GameObserver} registration.
     */
    @Override
    public void onGameUpdated() {
        Exchange exchange = gameManager.getExchange();
        winnersTable.update(exchange.getGainers(RANKING_LIMIT));
        losersTable.update(exchange.getLosers(RANKING_LIMIT));
    }
}
