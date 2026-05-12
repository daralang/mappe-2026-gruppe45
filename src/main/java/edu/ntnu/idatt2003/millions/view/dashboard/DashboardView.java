package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.PortfolioView;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.TransactionsView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The dashboard view of the application.
 * Contains a tab bar for navigating between portfolio, transactions,
 * watchlist and loans. Tab navigation is purely visual state and
 * handled internally by this view.
 */
public class DashboardView extends VBox {

    private final GameService gameService;
    private final PortfolioController portfolioController;
    private final Runnable onExploreStocks;
    private final VBox contentArea;

    /**
     * Constructs a new DashboardView with a tab bar.
     *
     * @param gameService the game manager containing player and exchange
     * @param weekBar     the week bar shared with the rest of the application
     */
    public DashboardView(GameService gameService,
                         PortfolioController portfolioController,
                         WeekBar weekBar,
                         Runnable onExploreStocks) {
        this.gameService = gameService;
        this.portfolioController = portfolioController;
        this.onExploreStocks = onExploreStocks;
        getStyleClass().add("content-area");

        ViewHeader viewHeader = new ViewHeader(
                "dashboard.title",
                List.of(
                        "dashboard.tab.portfolio",
                        "dashboard.tab.transactions",
                        "dashboard.tab.watchlist",
                        "dashboard.tab.loans"
                ),
                weekBar
        );

        contentArea = new VBox();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        viewHeader.setTabAction(0, this::showPortfolio);
        viewHeader.setTabAction(1, this::showTransactions);
        viewHeader.setTabAction(2, this::showWatchlist);
        viewHeader.setTabAction(3, this::showLoans);

        getChildren().addAll(viewHeader, contentArea);
        showPortfolio();
    }

    private void showPortfolio() {
        contentArea.getChildren().setAll(
                new PortfolioView(gameService, portfolioController, onExploreStocks));
    }

    private void showTransactions() {
        contentArea.getChildren().setAll(new TransactionsView(gameService));
    }

    private void showWatchlist() {
        contentArea.getChildren().clear(); // remove this when implementing setAll
        // contentArea.getChildren().setAll(new WatchlistView(gameService));
    }

    private void showLoans() {
        contentArea.getChildren().clear(); // remove this when implementing setAll
        // contentArea.getChildren().setAll(new LoansView(gameService));
    }
}