package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.service.TransactionPreviewService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog.*;
import edu.ntnu.idatt2003.millions.view.dialog.StockDetailModal;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Controller for portfolio actions (buy, sell, sell all, view details).
 * Opens the corresponding dialogs and delegates the actual transactions
 * to the model via {@link GameService}.
 *
 * <p>Provides read-only operations such as {@link #previewBuy(Stock, BigDecimal)}
 * and {@link #getCurrentBalance()} for views that need to display
 * derived data without performing a mutation.</p>
 */
public class TradeController {

    private static final int MAX_QUANTITY_SCALE = 4;
    private static final BigDecimal MIN_TRANSACTION_VALUE_NOK = BigDecimal.ONE;

    private final GameService gameService;
    private final TransactionPreviewService previewService;

    /**
     * Constructs a new PortfolioController.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TradeController(GameService gameService) {
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
     * Returns the current currency converter, used by views that call
     * read services directly with a currency converter argument.
     *
     * @return the active currency converter
     */
    public CurrencyConverter getCurrencyConverter() {
        return gameService.getCurrencyConverter();
    }

    public boolean isGameOver() {
        return gameService.isGameOver();
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

    /**
     * Game-policy validation for buy and sell quantity inputs.
     * Returns an i18n key if a rule is violated, or empty if both rules pass.
     *
     * <p>Rules: quantity has at most 4 decimal places; total order value in NOK
     * is at least 1.00 NOK.</p>
     *
     * @param quantity   the parsed quantity (positive, non-null)
     * @param totalInNok the full order value in NOK from the preview
     * @return an i18n key for the error message, or empty if valid
     */
    public Optional<String> validateTransactionInput(BigDecimal quantity, BigDecimal totalInNok) {
        if (quantity.stripTrailingZeros().scale() > MAX_QUANTITY_SCALE) {
            return Optional.of("dialog.error.tooManyDecimals");
        }
        if (totalInNok.compareTo(MIN_TRANSACTION_VALUE_NOK) < 0) {
            return Optional.of("dialog.error.belowMinimumValue");
        }
        return Optional.empty();
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
        Optional<String> policyError = validateTransactionInput(quantity, preview.totalInNok());
        if (policyError.isPresent()) {
            dialog.showError(LanguageManager.get(policyError.get()));
            return;
        }
        try {
            Transaction transaction = gameService.buy(stock.getSymbol(), quantity);
            dialog.close();
            BigDecimal balanceAfter = gameService.getPlayer().getMoney();
            Platform.runLater(() ->
                    new BuyReceipt(transaction, balanceBefore, balanceAfter, preview).show());
        } catch (IllegalArgumentException | IllegalStateException e) {
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
                share, quantity, gameService.getPlayer(),
                gameService.getCurrencyConverter());
        Optional<String> policyError = validateTransactionInput(quantity, preview.totalInNok());
        if (policyError.isPresent()) {
            dialog.showError(LanguageManager.get(policyError.get()));
            return;
        }
        try {
            Transaction transaction = gameService.sell(share, quantity);
            dialog.close();
            BigDecimal balanceAfter = gameService.getPlayer().getMoney();
            Platform.runLater(() ->
                    new SellReceipt(transaction, balanceBefore, balanceAfter, preview).show());
        } catch (IllegalArgumentException | IllegalStateException e) {
            String message = e.getMessage();
            if (message == null || message.isBlank()) {
                message = e.getClass().getSimpleName();
            }
            dialog.showError(message);
        }
    }

    /**
     * Opens the share details modal for the given share position.
     *
     * @param share the share to show details for
     */
    public void openDetailsModal(Share share) {
        ShareDetailsModal modal = new ShareDetailsModal(share, this);
        modal.show();
    }

    /**
     * Opens the read-only detail dialog for the given stock.
     *
     * @param stock the stock to show details for
     */
    public void openStockDetail(Stock stock) {
        new StockDetailModal(stock, gameService, this).show();
    }
}