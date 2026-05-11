package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreviewService;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog.*;
import javafx.application.Platform;

import java.math.BigDecimal;

/**
 * Controller for portfolio actions (buy, sell, sell all, view details).
 * Opens the corresponding dialogs and delegates the actual transactions
 * to the model via {@link GameService}.
 *
 * <p>Provides read-only operations such as {@link #previewBuy(Stock, BigDecimal)}
 * and {@link #getCurrentBalance()} for views that need to display
 * derived data without performing a mutation.</p>
 */
public class PortfolioController {

    private final GameService gameService;
    private final TransactionPreviewService previewService;

    /**
     * Constructs a new PortfolioController.
     *
     * @param gameService the game manager containing player and exchange
     */
    public PortfolioController(GameService gameService) {
        this.gameService = gameService;
        this.previewService = new TransactionPreviewService();
    }

    // ---- Read-only operations (called by views) ----

    /**
     * Returns the player's current cash balance.
     *
     * @return the player's available funds
     */
    public BigDecimal getCurrentBalance() {
        return gameService.getPlayer().getMoney();
    }

    /**
     * Returns a preview of buying the given quantity of a stock.
     *
     * @param stock    the stock to buy
     * @param quantity the quantity to buy
     * @return a preview with computed values
     */
    public TransactionPreview previewBuy(Stock stock, BigDecimal quantity) {
        return previewService.previewPurchase(
                stock, quantity, gameService.getPlayer(),
                gameService.getCurrencyConverter());
    }

    /**
     * Returns a preview of selling the given quantity of a share.
     *
     * @param share    the share to sell from
     * @param quantity the quantity to sell
     * @return a preview with computed values
     */
    public TransactionPreview previewSell(Share share, BigDecimal quantity) {
        return previewService.previewSale(
                share, quantity, gameService.getPlayer(),
                gameService.getCurrencyConverter());
    }

    // Dialog opening

    /**
     * Opens the buy dialog for the given stock and executes the purchase
     * if the user confirms.
     *
     * @param stock the stock to buy
     */
    public void openBuyDialog(Stock stock) {
        BuyDialog dialog = new BuyDialog(stock, this);
        dialog.setOnConfirm(quantity -> buy(stock, quantity, dialog));
        dialog.show();
    }

    /**
     * Opens the sell dialog for the given share and executes the sale
     * if the user confirms.
     *
     * @param share the share to sell from
     */
    public void openSellDialog(Share share) {
        SellDialog dialog = new SellDialog(share, this);
        dialog.setOnConfirm(quantity -> sell(share, quantity, dialog));
        dialog.show();
    }

    /**
     * Opens the sell-all dialog for the given share and executes the
     * full sale if the user confirms.
     *
     * @param share the share to sell entirely
     */
    public void openSellAllDialog(Share share) {
        SellAllDialog dialog = new SellAllDialog(share, this);
        dialog.setOnConfirm(quantity -> sell(share, quantity, dialog));
        dialog.show();
    }

    // Mutating operations

    private void buy(Stock stock, BigDecimal quantity, BuyDialog dialog) {
        BigDecimal balanceBefore = gameService.getPlayer().getMoney();
        TransactionPreview preview = previewService.previewPurchase(
                stock, quantity, gameService.getPlayer(),
                gameService.getCurrencyConverter());
        try {
            Transaction transaction = gameService.buy(stock.getSymbol(), quantity);
            dialog.close();
            BigDecimal balanceAfter = gameService.getPlayer().getMoney();
            Platform.runLater(() ->
                    new BuyReceipt(transaction, balanceBefore, balanceAfter, preview).show());
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }
            dialog.showError(message);
        }
    }

    private void sell(Share share, BigDecimal quantity, AbstractSellDialog dialog) {
        BigDecimal balanceBefore = gameService.getPlayer().getMoney();
        TransactionPreview preview = previewService.previewSale(
                share, share.getQuantity(), gameService.getPlayer(),
                gameService.getCurrencyConverter());
        try {
            // TODO: when partial sale is implemented, pass quantity through.
            //  For now, the model only supports selling the full Share instance.
            Transaction transaction = gameService.sell(share);
            dialog.close();
            BigDecimal balanceAfter = gameService.getPlayer().getMoney();
            Platform.runLater(() ->
                    new SellReceipt(transaction, balanceBefore, balanceAfter, preview).show());
        } catch (Exception e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }
            dialog.showError(message);
        }
    }

    // TODO: openDetailsDialog(Share share)
}