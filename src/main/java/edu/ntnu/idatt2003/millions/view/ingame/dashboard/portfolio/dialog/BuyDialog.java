package edu.ntnu.idatt2003.millions.view.ingame.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Dialog for buying shares of a stock.
 */
public class BuyDialog extends TransactionDialog {

    private static final Currency NOK = Currency.getInstance("NOK");

    private Consumer<BigDecimal> onConfirmCallback;

    public BuyDialog(Stock stock, TradeController controller) {
        super(stock, controller);
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("dialog.buy.title");
    }

    @Override
    protected String getStockHint() {
        return MessageFormat.format(LanguageManager.get("dialog.stock.priceHint"),
                MoneyFormatter.format(stock.getSalesPrice()),
                stock.getCurrency().getCurrencyCode());
    }

    @Override
    protected String getBalanceAfterLabel() {
        return LanguageManager.get("dialog.balance.afterBuy");
    }

    @Override
    protected String getConfirmButtonText() {
        return LanguageManager.get("dialog.button.confirmBuy");
    }

    @Override
    protected String getConfirmButtonStyleClass() {
        return "modal-button-primary";
    }

    @Override
    protected void updateSummary() {
        summaryBox.clear();
        hideError();

        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            renderEmptySummary();
            setConfirmEnabled(false);
            return;
        }

        TransactionPreview preview = controller.previewBuy(stock, quantity);

        Optional<String> policyError = controller.validateTransactionInput(quantity, preview.totalInNok());
        if (policyError.isPresent()) {
            renderEmptySummary();
            showError(LanguageManager.get(policyError.get()));
            setConfirmEnabled(false);
            return;
        }

        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"),
                MoneyFormatter.format(preview.gross()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionBuy"),
                MoneyFormatter.format(preview.commission()) + " " + currencyCode());
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalCost"),
                MoneyFormatter.format(preview.total()) + " " + currencyCode());
        if (!stock.getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + MoneyFormatter.format(preview.totalInNok()) + " NOK");
        }

        balanceAfterValue.setText(
                MoneyFormatter.format(preview.balanceAfter()) + " NOK");

        if (preview.balanceAfter().signum() < 0) {
            balanceAfterValue.getStyleClass().removeAll("positive", "negative");
            balanceAfterValue.getStyleClass().add("negative");
            showError(LanguageManager.get("dialog.error.insufficientFunds"));
            setConfirmEnabled(false);
        } else {
            balanceAfterValue.getStyleClass().removeAll("positive", "negative");
            setConfirmEnabled(true);
        }
    }

    private void renderEmptySummary() {
        String zero = MoneyFormatter.format(BigDecimal.ZERO) + " " + currencyCode();
        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"), zero);
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionBuy"), zero);
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalCost"), zero);
        if (!stock.getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + MoneyFormatter.format(BigDecimal.ZERO) + " NOK");
        }
        balanceAfterValue.getStyleClass().removeAll("positive", "negative");
        balanceAfterValue.setText(
                MoneyFormatter.format(controller.getCurrentBalance()) + " NOK");
    }

    /**
     * Sets the callback to invoke when the user confirms the purchase.
     * The callback receives the chosen quantity.
     *
     * @param callback the confirmation callback
     */
    public void setOnConfirm(Consumer<BigDecimal> callback) {
        this.onConfirmCallback = callback;
    }

    @Override
    protected void onConfirm() {
        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            showError(LanguageManager.get("dialog.quantity.invalid"));
            return;
        }
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(quantity);
        }
    }
}