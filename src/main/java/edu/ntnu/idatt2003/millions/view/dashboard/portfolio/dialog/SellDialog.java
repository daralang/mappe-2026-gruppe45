package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;

/**
 * Dialog for selling a user-specified quantity of shares.
 */
public class SellDialog extends AbstractSellDialog {

    public SellDialog(Share share, PortfolioController controller) {
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
        Label label = new Label(LanguageManager.get("dialog.quantity.label"));
        label.getStyleClass().add("detail-label");

        Label owned = new Label(MessageFormat.format(
                LanguageManager.get("dialog.quantity.owned"),
                NUMBER_FORMAT.format(share.getQuantity())));
        owned.getStyleClass().add("detail-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox labelRow = new HBox(label, spacer, owned);

        quantityInput.getStyleClass().add("modal-input");
        quantityInput.setText(getInitialQuantity().toPlainString());
        quantityInput.textProperty().addListener(
                (obs, oldVal, newVal) -> updateSummary());
        HBox.setHgrow(quantityInput, Priority.ALWAYS);

        Button sellAll = new Button(LanguageManager.get("dialog.button.sellAll"));
        sellAll.getStyleClass().add("modal-button");
        sellAll.setOnAction(e ->
                quantityInput.setText(share.getQuantity().toPlainString()));

        HBox inputRow = new HBox(8, quantityInput, sellAll);

        return new VBox(6, labelRow, inputRow);
    }

    @Override
    protected void updateSummary() {
        summaryBox.clear();
        hideError();

        BigDecimal quantity = getQuantity();
        if (quantity == null) {
            setConfirmEnabled(false);
            balanceAfterValue.setText("");
            setTransactionInfo(null, false);
            return;
        }

        if (quantity.compareTo(share.getQuantity()) > 0) {
            setConfirmEnabled(false);
            balanceAfterValue.setText("");
            setTransactionInfo(null, false);
            showError(MessageFormat.format(
                    LanguageManager.get("dialog.quantity.notEnoughShares"),
                    NUMBER_FORMAT.format(share.getQuantity())));
            return;
        }

        renderSummary(quantity);
    }
}