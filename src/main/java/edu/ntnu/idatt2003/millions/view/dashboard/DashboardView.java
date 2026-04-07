package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.PortfolioView;
import javafx.scene.control.Button;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The dashboard view of the application.
 * Contains a tab bar for navigating between portfolio, transactions,
 * watchlist and loans.
 */
public class DashboardView extends VBox {

    private final GameManager gameManager;
    private final ViewHeader viewHeader;
    private final VBox contentArea;

    /**
     * Constructs a new DashboardView with a tab bar.
     */
    public DashboardView(GameManager gameManager, WeekBar weekBar) {
        this.gameManager = gameManager;
        getStyleClass().add("content-area");

        viewHeader = new ViewHeader(
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

        getChildren().addAll(viewHeader, contentArea);
        showPortfolio();
    }

    public Button getPortfolioButton() {
        return viewHeader.getTabButton(0);
    }

    public Button getTransactionsButton() {
        return viewHeader.getTabButton(1);
    }

    public Button getWatchlistButton() {
        return viewHeader.getTabButton(2);
    }

    public Button getLoansButton() {
        return viewHeader.getTabButton(3);
    }

    public void showPortfolio() {
        contentArea.getChildren().setAll(new PortfolioView(gameManager));
    }

    public void showTransactions() {
        //contentArea.getChildren().setAll(new TransactionsView(gameManager));
    }

    public void showWatchlist() {
        //contentArea.getChildren().setAll(new WatchlistView(gameManager));
    }

    public void showLoans() {
        //contentArea.getChildren().setAll(new LoansView(gameManager));
    }
}