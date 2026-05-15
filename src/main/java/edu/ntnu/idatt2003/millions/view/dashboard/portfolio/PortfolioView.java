package edu.ntnu.idatt2003.millions.view.dashboard.portfolio;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.view.component.card.AvailableFundsCard;
import edu.ntnu.idatt2003.millions.view.component.card.PortfolioValueCard;
import edu.ntnu.idatt2003.millions.view.component.ExploreStocksButton;
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

    /**
     * Constructs a new PortfolioView.
     *
     * @param gameService     the game manager containing player and exchange
     * @param controller      the controller handling portfolio actions
     * @param onExploreStocks callback invoked when the user clicks the explore-stocks button
     */
    public PortfolioView(GameService gameService,
                         TradeController controller,
                         Runnable onExploreStocks) {
        setSpacing(16);

        HBox topRow = buildTopRow(gameService);
        HoldingsCard holdingsCard = new HoldingsCard(gameService, controller, new PortfolioService());
        ExploreStocksButton exploreButton = new ExploreStocksButton(onExploreStocks);
        exploreButton.setMaxWidth(Double.MAX_VALUE);
        RealizedReturnsCard realizedReturnsCard = new RealizedReturnsCard(gameService);

        getChildren().addAll(topRow, holdingsCard, exploreButton, realizedReturnsCard);
    }

    private HBox buildTopRow(GameService gameService) {
        HBox row = new HBox(16);

        NetWorthCard netWorthCard = new NetWorthCard(gameService);
        HBox.setHgrow(netWorthCard, Priority.ALWAYS);

        VBox rightCards = new VBox(12,
                new WeeklyChangeCard(gameService),
                new AvailableFundsCard(gameService, "dashboard.availableFunds"),
                new PortfolioValueCard(gameService, "dashboard.portfolioValue"),
                new StatusCard(gameService)
        );
        rightCards.setMinWidth(220);
        rightCards.setMaxWidth(260);

        netWorthCard.prefHeightProperty().bind(rightCards.heightProperty());
        netWorthCard.maxHeightProperty().bind(rightCards.heightProperty());

        row.getChildren().addAll(netWorthCard, rightCards);
        return row;
    }
}
