package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.dashboard.DashboardView;

/**
 * Controller for the dashboard view.
 * Binds tab buttons to their corresponding content views.
 */
public class DashboardController {

    private final DashboardView view;
    private final GameManager gameManager;

    /**
     * Constructs a new DashboardController and binds tab events.
     *
     * @param view        the dashboard view to control
     * @param gameManager the game manager containing player and exchange
     */
    public DashboardController(DashboardView view, GameManager gameManager) {
        this.view = view;
        this.gameManager = gameManager;
        bindEvents();
    }

    private void bindEvents() {
        view.getPortfolioButton().setOnAction(e -> view.showPortfolio());
        view.getTransactionsButton().setOnAction(e -> view.showTransactions());
        view.getWatchlistButton().setOnAction(e -> view.showWatchlist());
        view.getLoansButton().setOnAction(e -> view.showLoans());
    }
}