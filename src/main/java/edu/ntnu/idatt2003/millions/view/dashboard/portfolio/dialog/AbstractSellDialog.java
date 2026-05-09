package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;

import java.math.BigDecimal;
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

    protected final Share share;
    protected Consumer<BigDecimal> onConfirmCallback;

    protected AbstractSellDialog(Share share, PortfolioController controller) {
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
        return "Salgspris per andel: "
                + NUMBER_FORMAT.format(stock.getSalesPrice()) + " NOK";
    }

    @Override
    protected BigDecimal getInitialQuantity() {
        return share.getQuantity();
    }

    @Override
    protected String getBalanceAfterLabel() {
        return "Etter salg";
    }

    @Override
    protected String getConfirmButtonText() {
        return "Bekreft salg";
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

        addSummaryRow("Bruttoverdi",
                NUMBER_FORMAT.format(preview.gross()) + " NOK");
        addSummaryRow("Kurtasje (1%)",
                "\u2212" + NUMBER_FORMAT.format(preview.commission()) + " NOK");
        addSummaryRow("Skatt (30% av gevinst)",
                "\u2212" + NUMBER_FORMAT.format(preview.tax()) + " NOK");
        addSummaryTotal("Du mottar",
                NUMBER_FORMAT.format(preview.total()) + " NOK");

        renderProfitLoss(preview);
        renderBalanceAfter(preview);

        setConfirmEnabled(true);
    }

    private void renderProfitLoss(TransactionPreview preview) {
        boolean positive = preview.profit().signum() >= 0;
        String sign = positive ? "+" : "\u2212";
        String pctSign = positive ? "+" : "";
        String message = (positive ? "Gevinst" : "Tap") + " på dette salget: "
                + sign + NUMBER_FORMAT.format(preview.profit().abs()) + " NOK ("
                + pctSign + preview.profitPercent().toPlainString() + "%)";
        setTransactionInfo(message, positive);
    }

    private void renderBalanceAfter(TransactionPreview preview) {
        balanceAfterValue.setText(
                NUMBER_FORMAT.format(preview.balanceAfter()) + " NOK");
        balanceAfterValue.getStyleClass().removeAll("positive", "negative");
        balanceAfterValue.getStyleClass().add("positive");
    }

    @Override
    protected void onConfirm() {
        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            showError("Ugyldig antall");
            return;
        }
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(quantity);
        }
    }
}