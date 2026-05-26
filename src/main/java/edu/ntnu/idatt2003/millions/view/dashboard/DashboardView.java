package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.controller.loan.LoanController;
import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.keyboard.PageFocusProvider;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.keyboard.TabNavigationRegistry;
import edu.ntnu.idatt2003.millions.service.game.GameService;
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
 * keyboard shortcuts (Shift+1–4). Each method also updates the active tab
 * indicator in {@link ViewHeader} directly, ensuring the highlight is correct
 * whether the tab was activated by click or by keyboard shortcut.</p>
 */
public class DashboardView extends VBox implements SearchFocusProvider, PageFocusProvider {

    private final GameService gameService;
    private final TradeController tradeController;
    private final LoanController loanController;
    private final Runnable onExploreStocks;
    private final VBox contentArea;
    private final ViewHeader viewHeader;

    private PortfolioView portfolioView;
    private TransactionsView transactionsView;
    private WatchlistView watchlistView;
    private LoansView loansView;
    private SearchFocusProvider activeSubview;

    /**
     * Constructs a new DashboardView with a tab bar.
     *
     * @param gameService      the game manager containing player and exchange
     * @param tradeController  the controller used to open buy/sell dialogs
     * @param loanController   the controller used to open loan dialogs
     * @param weekBar          the week bar owned exclusively by this view; each top-level view
     *                         receives its own instance since a JavaFX node can only belong to one parent at a time
     * @param onExploreStocks  callback invoked when the user navigates to the exchange stocks tab
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

        viewHeader.setTabAction(0, () -> showTab(0));
        viewHeader.setTabAction(1, () -> showTab(1));
        viewHeader.setTabAction(2, () -> showTab(2));
        viewHeader.setTabAction(3, () -> showTab(3));

        getChildren().addAll(viewHeader, contentArea);
        showTab(0);
    }

    /**
     * Switches to the tab at the given zero-based index.
     * Updates the active tab indicator in {@link ViewHeader} and delegates to the
     * appropriate private show-method. Called by {@link TabNavigationRegistry}
     * when a {@code Shift+N} shortcut fires and by the tab buttons in the header.
     * Indices out of range are ignored.
     *
     * @param index 0 = Portfolio, 1 = Transactions, 2 = Watchlist, 3 = Loans
     */
    public void showTab(int index) {
        switch (index) {
            case 0 -> { viewHeader.setActive(viewHeader.getTabButton(0)); showPortfolio(); }
            case 1 -> { viewHeader.setActive(viewHeader.getTabButton(1)); showTransactions(); }
            case 2 -> { viewHeader.setActive(viewHeader.getTabButton(2)); showWatchlist(); }
            case 3 -> { viewHeader.setActive(viewHeader.getTabButton(3)); showLoans(); }
            default -> { }
        }
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

    /**
     * Focuses the active dashboard tab as the keyboard entry point for this page.
     */
    @Override
    public void focusPageEntry() {
        viewHeader.focusActiveTab();
    }

    /**
     * Switches the content area to the portfolio sub-view.
     * Lazily initialises {@link PortfolioView} on first call.
     */
    private void showPortfolio() {
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
    private void showTransactions() {
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
    private void showWatchlist() {
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
    private void showLoans() {
        if (loansView == null) {
            loansView = new LoansView(gameService, loanController);
            loansView.getAvailableLoansCard().setOnApplyClicked(loanController::openLoanDialog);
        }
        activeSubview = null;
        contentArea.getChildren().setAll(loansView);
    }
}
