package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;

import java.math.BigDecimal;
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
        return "Kjøp aksjer";
    }

    @Override
    protected String getStockHint() {
        return "Pris per andel: " + NUMBER_FORMAT.format(stock.getSalesPrice()) + " NOK";
    }

    @Override
    protected BigDecimal getInitialQuantity() {
        return BigDecimal.ONE;
    }

    @Override
    protected String getBalanceAfterLabel() {
        return "Etter kjøp";
    }

    @Override
    protected String getConfirmButtonText() {
        return "Bekreft kjøp";
    }

    @Override
    protected String getConfirmButtonStyleClass() {
        return "modal-button-primary";
    }

    @Override
    protected void updateSummary() {
        summaryBox.getChildren().clear();
        hideError();

        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            setConfirmEnabled(false);
            balanceAfterValue.setText("");
            return;
        }

        TransactionPreview preview = controller.previewBuy(stock, quantity);

        addSummaryRow("Bruttoverdi",
                NUMBER_FORMAT.format(preview.gross()) + " NOK");
        addSummaryRow("Kurtasje (0,5%)",
                NUMBER_FORMAT.format(preview.commission()) + " NOK");
        addSummaryTotal("Totalkostnad",
                NUMBER_FORMAT.format(preview.total()) + " NOK");

        balanceAfterValue.setText(
                NUMBER_FORMAT.format(preview.balanceAfter()) + " NOK");

        if (preview.balanceAfter().signum() < 0) {
            balanceAfterValue.getStyleClass().removeAll("positive", "negative");
            balanceAfterValue.getStyleClass().add("negative");
            showError("Du har ikke nok penger til dette kjøpet.");
            setConfirmEnabled(false);
        } else {
            balanceAfterValue.getStyleClass().removeAll("positive", "negative");
            balanceAfterValue.getStyleClass().add("positive");
            setConfirmEnabled(true);
        }
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
            showError("Ugyldig antall");
            return;
        }
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(quantity);
        }
    }
}