package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.LoansView;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.PortfolioView;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.TransactionsView;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.WatchlistView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The dashboard view of the application.
 *
 * <p>Contains a tab bar for navigating between portfolio, transactions,
 * watchlist and loans. Each sub-view is lazily initialised on first tab selection.
 * The four tab methods are public so {@code MainView} can drive them from
 * keyboard shortcuts (Shift+1–4).</p>
 */
public class DashboardView extends VBox implements SearchFocusProvider {

    private final GameService gameService;
    private final TradeController tradeController;
    private final LoanController loanController;
    private final Runnable onExploreStocks;
    private final VBox contentArea;

    private PortfolioView portfolioView;
    private TransactionsView transactionsView;
    private WatchlistView watchlistView;
    private LoansView loansView;
    private SearchFocusProvider activeSubview;

    /**
     * Constructs a new DashboardView with a tab bar.
     *
     * @param gameService the game manager containing player and exchange
     * @param weekBar     the week bar shared with the rest of the application
     */
    public DashboardView(GameService gameService,
                         TradeController tradeController,
                         LoanController loanController,
                         WeekBar weekBar,
                         Runnable onExploreStocks) {
        this.gameService = gameService;
        this.tradeController = tradeController;
        this.loanController = loanController;
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

    /**
     * Switches the content area to the portfolio sub-view.
     * Lazily initialises {@link PortfolioView} on first call.
     */
    public void showPortfolio() {
        if (portfolioView == null) {
            portfolioView = new PortfolioView(gameService, tradeController, onExploreStocks);
        }
        activeSubview = portfolioView;
        contentArea.getChildren().setAll(portfolioView);
    }

    /**
     * Switches the content area to the transactions sub-view.
     * Lazily initialises {@link TransactionsView} on first call.
     */
    public void showTransactions() {
        if (transactionsView == null) {
            transactionsView = new TransactionsView(gameService);
        }
        activeSubview = transactionsView;
        contentArea.getChildren().setAll(transactionsView);
    }

    /**
     * Switches the content area to the watchlist sub-view.
     * Lazily initialises {@link WatchlistView} on first call.
     */
    public void showWatchlist() {
        if (watchlistView == null) {
            watchlistView = new WatchlistView(gameService, tradeController, onExploreStocks);
        }
        activeSubview = watchlistView;
        contentArea.getChildren().setAll(watchlistView);
    }

    /**
     * Switches the content area to the loans sub-view.
     * Lazily initialises {@link LoansView} on first call.
     */
    public void showLoans() {
        if (loansView == null) {
            loansView = new LoansView(gameService, loanController);
            loansView.getAvailableLoansCard().setOnApplyClicked(loanController::openLoanDialog);
        }
        activeSubview = null;
        contentArea.getChildren().setAll(loansView);
    }

    /**
     * Focuses the search field of the currently active dashboard sub-view.
     * The loans sub-view has no search bar; in that case this method is a no-op.
     */
    @Override
    public void focusSearch() {
        if (activeSubview != null) {
            activeSubview.focusSearch();
        }
    }
}