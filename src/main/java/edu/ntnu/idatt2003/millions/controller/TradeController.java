package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.service.TransactionPreviewService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.service.toast.ToastType;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog.*;
import edu.ntnu.idatt2003.millions.view.dialog.StockDetailModal;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for portfolio actions (buy, sell, sell all, view details, and watchlist).
 * Opens the corresponding dialogs and delegates the actual transactions
 * to the model via {@link GameService}. Shows {@link ToastService} feedback
 * for watchlist mutations.
 *
 * <p>Provides read-only operations such as {@link #previewBuy(Stock, BigDecimal)}
 * and {@link #getCurrentBalance()} for views that need to display
 * derived data without performing a mutation.</p>
 */
public class TradeController {

    private static final Logger LOGGER = Logger.getLogger(TradeController.class.getName());
    private static final int MAX_QUANTITY_SCALE = 4;
    private static final BigDecimal MIN_TRANSACTION_VALUE_NOK = BigDecimal.ONE;

    private final GameService gameService;
    private final TransactionPreviewService previewService;
    private final ToastService toastService;

    /**
     * Constructs a new TradeController.
     *
     * @param gameService  the game manager containing player and exchange
     * @param toastService the toast service used to surface feedback after watchlist mutations
     * @throws NullPointerException if either argument is null
     */
    public TradeController(GameService gameService, ToastService toastService) {
        this.gameService = Objects.requireNonNull(gameService, "gameService cannot be null");
        this.toastService = Objects.requireNonNull(toastService, "toastService cannot be null");
        this.previewService = new TransactionPreviewService();
    }

    // ---- Read-only operations (called by views) ----

    /**
     * Returns the player's current cash balance.
     *
     * @return the player's available funds
     */
    public BigDecimal getCurrentBalance() {
        return gameService.getPlayer().getCash();
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

    // Watchlist operations

    /**
     * Toggles the given stock on or off the player's watchlist and shows a
     * toast confirming the result.
     *
     * @param symbol the ticker symbol of the stock to toggle
     */
    public void toggleWatchlist(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            LOGGER.log(Level.WARNING, "Watchlist toggle rejected — blank symbol");
            return;
        }
        boolean isWatched = gameService.getPlayer().isOnWatchlist(symbol);
        String label = buildWatchlistLabel(symbol);
        try {
            if (isWatched) {
                gameService.removeFromWatchlist(symbol);
                toastService.show(
                        MessageFormat.format(LanguageManager.get("toast.watchlist.removed"), label),
                        ToastType.SUCCESS);
            } else {
                gameService.addToWatchlist(symbol);
                toastService.show(
                        MessageFormat.format(LanguageManager.get("toast.watchlist.added"), label),
                        ToastType.SUCCESS);
            }
        } catch (IllegalArgumentException e) {
            String errorKey = isWatched
                    ? "toast.watchlist.removeFailed"
                    : "toast.watchlist.addFailed";
            toastService.show(LanguageManager.get(errorKey), ToastType.ERROR);
            LOGGER.log(Level.WARNING, "Watchlist toggle failed", e);
        }
    }

    /**
     * Removes the given stock from the player's watchlist and shows a success
     * toast.
     *
     * @param symbol the ticker symbol of the stock to remove
     */
    public void removeFromWatchlist(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            LOGGER.log(Level.WARNING, "Watchlist remove rejected — blank symbol");
            return;
        }
        String label = buildWatchlistLabel(symbol);
        try {
            gameService.removeFromWatchlist(symbol);
            toastService.show(
                    MessageFormat.format(LanguageManager.get("toast.watchlist.removed"), label),
                    ToastType.SUCCESS);
        } catch (IllegalArgumentException e) {
            toastService.show(LanguageManager.get("toast.watchlist.removeFailed"), ToastType.ERROR);
            LOGGER.log(Level.WARNING, "Watchlist remove failed", e);
        }
    }

    /**
     * Builds the display label used in watchlist toast messages.
     *
     * @param symbol the ticker symbol to build a label for
     * @return the display label
     */
    private String buildWatchlistLabel(String symbol) {
        Exchange exchange = gameService.getExchange();
        if (exchange.hasStock(symbol)) {
            return symbol + " – " + exchange.getStock(symbol).getCompany();
        }
        return symbol;
    }

    /**
     * Updates the note for the given stock in the player's watchlist.
     *
     * @param symbol  the ticker symbol of the watchlist entry to update
     * @param newNote the new note text
     */
    public void updateWatchlistNote(String symbol, String newNote) {
        if (symbol == null || symbol.isBlank()) {
            LOGGER.log(Level.WARNING, "Watchlist note update rejected — blank symbol");
            return;
        }
        try {
            gameService.updateWatchlistNote(symbol, newNote);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Watchlist note update failed", e);
        }
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
        BigDecimal balanceBefore = gameService.getPlayer().getCash();
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
            BigDecimal balanceAfter = gameService.getPlayer().getCash();
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
        BigDecimal balanceBefore = gameService.getPlayer().getCash();
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
            BigDecimal balanceAfter = gameService.getPlayer().getCash();
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