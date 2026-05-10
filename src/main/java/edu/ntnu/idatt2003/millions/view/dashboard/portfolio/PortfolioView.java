package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The portfolio tab view displayed under the dashboard.
 * Shows net worth with chart, weekly performance, available funds,
 * portfolio value, player status, and the player's holdings.
 */
public class PortfolioView extends VBox {

    private final GameManager gameManager;
    private final PortfolioController controller;

    /**
     * Constructs a new PortfolioView.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public PortfolioView(GameManager gameManager) {
        this.gameManager = gameManager;
        this.controller = new PortfolioController(gameManager);
        setSpacing(16);

        HBox topRow = buildTopRow();
        HoldingsCard holdingsCard = new HoldingsCard(gameManager, controller);

        getChildren().addAll(topRow, holdingsCard);
    }

    private HBox buildTopRow() {
        HBox row = new HBox(16);

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

        row.getChildren().addAll(netWorthCard, rightCards);
        return row;
    }
}