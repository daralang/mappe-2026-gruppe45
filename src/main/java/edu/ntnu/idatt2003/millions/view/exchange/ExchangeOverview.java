package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.exchange.overview.StockRankingTable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * View for the exchange overview tab.
 * Displays weekly market summary cards and ranked winner/loser tables.
 * Updates once per week advance via {@link GameObserver}.
 */
public class ExchangeOverview extends VBox implements GameObserver {

    private final GameManager gameManager;

    private final Label totalStocksValue;
    private final Label rosedValue;
    private final Label fellValue;

    private final StockRankingTable winnersTable;
    private final StockRankingTable losersTable;

    private static final int RANKING_LIMIT = 5;

    /**
     * Constructs a new ExchangeOverviewView and registers itself as a game observer.
     *
     * @param gameManager the game manager containing player and exchange
     * @throws NullPointerException if gameManager is null
     */
    public ExchangeOverview(GameManager gameManager) {
        this.gameManager = gameManager;
        gameManager.addObserver(this);
        setSpacing(16);
        getStyleClass().add("content-area");

        totalStocksValue = new Label();
        rosedValue       = new Label();
        fellValue        = new Label();

        HBox statCards = new HBox(16,
                buildStatCard("exchange.overview.totalStocks", totalStocksValue),
                buildStatCard("exchange.overview.roseThisWeek", rosedValue),
                buildStatCard("exchange.overview.fellThisWeek", fellValue)
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

        HBox.setHgrow(winnersTable, Priority.ALWAYS);
        HBox.setHgrow(losersTable, Priority.ALWAYS);
        HBox tables = new HBox(16, winnersTable, losersTable);

        getChildren().addAll(statCards, tables);

        updateStats();
    }

    /**
     * Builds a single stat card with a label and a value label.
     *
     * @param titleKey   the i18n key for the card title
     * @param valueLabel the label to display the value in
     * @return a VBox styled as a card
     */
    private VBox buildStatCard(String titleKey, Label valueLabel) {
        Label title = new Label(LanguageManager.get(titleKey));
        title.getStyleClass().add("card-label");
        valueLabel.getStyleClass().add("card-value");

        VBox card = new VBox(4, title, valueLabel);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /**
     * Updates the stat cards with current exchange data.
     */
    private void updateStats() {
        Exchange exchange = gameManager.getExchange();
        totalStocksValue.setText(String.valueOf(exchange.getStocks().size()));
        rosedValue.setText(String.valueOf(exchange.getGainers(Integer.MAX_VALUE).size()));
        fellValue.setText(String.valueOf(exchange.getLosers(Integer.MAX_VALUE).size()));
    }

    /**
     * Called when the game state has changed.
     * Refreshes stat cards and ranking tables.
     */
    @Override
    public void onGameUpdated() {
        Exchange exchange = gameManager.getExchange();
        updateStats();
        winnersTable.update(exchange.getGainers(RANKING_LIMIT));
        losersTable.update(exchange.getLosers(RANKING_LIMIT));
    }
}