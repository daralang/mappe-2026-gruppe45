package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Dialog for buying shares of a stock.
 */
public class BuyDialog extends TransactionDialog {

    private Consumer<BigDecimal> onConfirmCallback;

    public BuyDialog(Stock stock, PortfolioController controller) {
        super(stock, controller);
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("dialog.buy.title");
    }

    @Override
    protected String getStockHint() {
        return MessageFormat.format(LanguageManager.get("dialog.stock.priceHint"),
                NUMBER_FORMAT.format(stock.getSalesPrice()),
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

        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"),
                NUMBER_FORMAT.format(preview.gross()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionBuy"),
                NUMBER_FORMAT.format(preview.commission()) + " " + currencyCode());
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalCost"),
                NUMBER_FORMAT.format(preview.total()) + " " + currencyCode());

        balanceAfterValue.setText(
                NUMBER_FORMAT.format(preview.balanceAfter()) + " NOK");

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
        String zero = NUMBER_FORMAT.format(BigDecimal.ZERO) + " " + currencyCode();
        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"), zero);
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionBuy"), zero);
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalCost"), zero);
        balanceAfterValue.getStyleClass().removeAll("positive", "negative");
        balanceAfterValue.setText(
                NUMBER_FORMAT.format(controller.getCurrentBalance()) + " NOK");
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