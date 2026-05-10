package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.PortfolioView;
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

    private final GameManager gameManager;
    private final PortfolioController portfolioController;
    private final VBox contentArea;

    /**
     * Constructs a new DashboardView with a tab bar.
     *
     * @param gameManager the game manager containing player and exchange
     * @param weekBar     the week bar shared with the rest of the application
     */
    public DashboardView(GameManager gameManager, PortfolioController portfolioController, WeekBar weekBar) {
        this.gameManager = gameManager;
        this.portfolioController = portfolioController;
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

        viewHeader.getTabButton(0).setOnAction(e -> showPortfolio());
        viewHeader.getTabButton(1).setOnAction(e -> showTransactions());
        viewHeader.getTabButton(2).setOnAction(e -> showWatchlist());
        viewHeader.getTabButton(3).setOnAction(e -> showLoans());

        getChildren().addAll(viewHeader, contentArea);
        showPortfolio();
    }

    private void showPortfolio() {
        contentArea.getChildren().setAll(new PortfolioView(gameManager, portfolioController));
    }

    private void showTransactions() {
        // contentArea.getChildren().setAll(new TransactionsView(gameManager));
    }

    private void showWatchlist() {
        // contentArea.getChildren().setAll(new WatchlistView(gameManager));
    }

    private void showLoans() {
        // contentArea.getChildren().setAll(new LoansView(gameManager));
    }
}