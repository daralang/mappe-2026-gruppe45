package edu.ntnu.idatt2003.millions.view.ingame.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;

/**
 * Dialog for selling a user-specified quantity of shares.
 */
public class SellDialog extends AbstractSellDialog {

    public SellDialog(Share share, TradeController controller) {
        super(share, controller);
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("dialog.sell.title");
    }

    /**
     * Quantity section with editable input, "Du eier"-info, and a
     * "Selg alt"-shortcut that fills the input with the full position.
     */
    @Override
    protected VBox buildQuantitySection() {
        StyledText label = StyledText.detailLabel(LanguageManager.get("dialog.quantity.label"));

        StyledText owned = StyledText.detailLabel(MessageFormat.format(
                LanguageManager.get("dialog.quantity.owned"),
                MoneyFormatter.format(share.getQuantity())));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox labelRow = new HBox(label, spacer, owned);

        quantityInput.setText(getInitialQuantity().toPlainString());
        quantityInput.textProperty().addListener(
                (obs, oldVal, newVal) -> updateSummary());

        Button sellAll = new Button(LanguageManager.get("dialog.button.sellAll"));
        sellAll.getStyleClass().add("modal-button");
        sellAll.setOnAction(e ->
                quantityInput.setText(share.getQuantity().stripTrailingZeros().toPlainString()));

        HBox stepperRow = buildStepperRow();
        HBox.setHgrow(stepperRow, Priority.ALWAYS);
        HBox inputRow = new HBox(8, stepperRow, sellAll);

        return new VBox(6, labelRow, inputRow);
    }

    @Override
    protected void updateSummary() {
        summaryBox.clear();
        hideError();

        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            renderEmptySummary();
            setConfirmEnabled(false);
            setTransactionInfo(null, null, null, false);
            return;
        }

        if (quantity.compareTo(share.getQuantity()) > 0) {
            renderEmptySummary();
            setConfirmEnabled(false);
            setTransactionInfo(null, null, null, false);
            showError(MessageFormat.format(
                    LanguageManager.get("dialog.quantity.notEnoughShares"),
                    MoneyFormatter.format(share.getQuantity())));
            return;
        }

        TransactionPreview preview = controller.previewSell(share, quantity);
        Optional<String> policyError = controller.validateTransactionInput(quantity, preview.totalInNok());
        if (policyError.isPresent()) {
            renderEmptySummary();
            setTransactionInfo(null, null, null, false);
            showError(LanguageManager.get(policyError.get()));
            setConfirmEnabled(false);
            return;
        }

        renderSummary(quantity);
    }

    private void renderEmptySummary() {
        String zero = MoneyFormatter.format(BigDecimal.ZERO) + " " + currencyCode();
        summaryBox.addRow(LanguageManager.get("dialog.summary.gross"), zero);
        summaryBox.addRow(LanguageManager.get("dialog.summary.commissionSell"), zero);
        summaryBox.addRow(LanguageManager.get("dialog.summary.tax"), zero);
        summaryBox.addTotal(LanguageManager.get("dialog.summary.totalReceived"), zero);
        if (!stock.getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + MoneyFormatter.format(BigDecimal.ZERO) + " NOK");
        }
        balanceAfterValue.getStyleClass().removeAll("positive", "negative");
        balanceAfterValue.setText(
                MoneyFormatter.format(controller.getCurrentBalance()) + " NOK");
    }
}