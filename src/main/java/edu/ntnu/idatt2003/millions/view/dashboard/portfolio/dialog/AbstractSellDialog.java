package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Currency;
import java.util.function.Consumer;

/**
 * Abstract base class for sale dialogs. Encapsulates the shared logic
 * for selling shares: summary rows (gross, commission, tax, payout),
 * profit/loss info, and balance updates.
 *
 * <p>Subclasses define how the user picks the quantity to sell:
 * {@link SellDialog} allows free input, while {@link SellAllDialog}
 * locks the quantity to the full position.</p>
 */
public abstract class AbstractSellDialog extends TransactionDialog {

    protected static final Currency NOK = Currency.getInstance("NOK");

    protected final Share share;
    protected Consumer<BigDecimal> onConfirmCallback;

    protected AbstractSellDialog(Share share, TradeController controller) {
        super(share.getStock(), controller);
        this.share = share;
    }

    /**
     * Sets the callback to invoke when the user confirms the sale.
     * The callback receives the chosen quantity.
     *
     * @param callback the confirmation callback
     */
    public void setOnConfirm(Consumer<BigDecimal> callback) {
        this.onConfirmCallback = callback;
    }

    @Override
    protected String getStockHint() {
        return MessageFormat.format(LanguageManager.get("dialog.stock.salesPriceHint"),
                MoneyFormatter.format(stock.getSalesPrice()),
                stock.getCurrency().getCurrencyCode());
    }

    @Override
    protected String getBalanceAfterLabel() {
        return LanguageManager.get("dialog.balance.afterSell");
    }

    @Override
    protected String getConfirmButtonText() {
        return LanguageManager.get("dialog.button.confirmSell");
    }

    @Override
    protected String getConfirmButtonStyleClass() {
        return "modal-button-danger";
    }

    /**
     * Renders the summary based on the given quantity. Used by both
     * SellDialog and SellAllDialog. Delegates calculation to the
     * controller's preview service.
     *
     * @param quantity the quantity to render summary for
     */
    protected void renderSummary(BigDecimal quantity) {
        TransactionPreview preview = controller.previewSell(share, quantity);

        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"),
                MoneyFormatter.format(preview.gross()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionSell"),
                "\u2212" + MoneyFormatter.format(preview.commission()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("dialog.summary.tax"),
                "\u2212" + MoneyFormatter.format(preview.tax()) + " " + currencyCode());
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalReceived"),
                MoneyFormatter.format(preview.total()) + " " + currencyCode());
        if (!stock.getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + MoneyFormatter.format(preview.totalInNok()) + " NOK");
        }

        renderProfitLoss(preview);
        renderBalanceAfter(preview);

        setConfirmEnabled(true);
    }

    private void renderProfitLoss(TransactionPreview preview) {
        boolean positive = preview.profit().signum() >= 0;
        String sign = positive ? "+" : "\u2212";
        String pctSign = positive ? "+" : "";

        String label = LanguageManager.get(positive ? "dialog.profit.gain" : "dialog.profit.loss");
        String primaryValue = sign + MoneyFormatter.format(preview.profit().abs()) + " " + currencyCode()
                + " (" + pctSign + preview.profitPercent().toPlainString() + "%)";

        String secondaryValue = null;
        if (!stock.getCurrency().equals(NOK) && preview.profitInNok() != null) {
            secondaryValue = "= " + sign + MoneyFormatter.format(preview.profitInNok().abs()) + " NOK";
        }
        setTransactionInfo(label, primaryValue, secondaryValue, positive);
    }

    private void renderBalanceAfter(TransactionPreview preview) {
        balanceAfterValue.setText(
                MoneyFormatter.format(preview.balanceAfter()) + " NOK");
        balanceAfterValue.getStyleClass().removeAll("positive", "negative");
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