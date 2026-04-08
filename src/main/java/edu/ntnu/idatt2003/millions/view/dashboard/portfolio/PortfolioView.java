package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The portfolio tab view displayed under the dashboard.
 * Shows net worth with chart, weekly performance, available funds,
 * portfolio value and player status.
 */
public class PortfolioView extends HBox {

    private final GameManager gameManager;

    /**
     * Constructs a new PortfolioView.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public PortfolioView(GameManager gameManager) {
        this.gameManager = gameManager;
        setSpacing(16);

        NetWorthCard netWorthCard = new NetWorthCard(gameManager);
        HBox.setHgrow(netWorthCard, Priority.ALWAYS);

        VBox rightCards = new VBox(12,
                new WeeklyChangeCard(gameManager),
                new AvailableFundsCard(gameManager),
                new PortfolioValueCard(gameManager),
                new StatusCard(gameManager)
        );
        rightCards.setMinWidth(220);
        rightCards.setMaxWidth(260);

        netWorthCard.prefHeightProperty().bind(rightCards.heightProperty());
        netWorthCard.maxHeightProperty().bind(rightCards.heightProperty());

        getChildren().addAll(netWorthCard, rightCards);
    }
}