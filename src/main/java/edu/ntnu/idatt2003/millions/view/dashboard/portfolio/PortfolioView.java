package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.StatusCard;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.WeeklyChangeCard;
import javafx.scene.layout.HBox;
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

        WeeklyChangeCard weeklyChangeCard = new WeeklyChangeCard(gameManager);
        AvailableFundsCard availableFundsCard = new AvailableFundsCard(gameManager);
        PortfolioValueCard portfolioValueCard = new PortfolioValueCard(gameManager);
        StatusCard statusCard = new StatusCard(gameManager);


        VBox rightCards = new VBox(12,
                weeklyChangeCard,
                availableFundsCard,
                portfolioValueCard,
                statusCard
        );
        rightCards.setMinWidth(220);
        rightCards.setMaxWidth(260);

        getChildren().addAll(rightCards);
    }
}