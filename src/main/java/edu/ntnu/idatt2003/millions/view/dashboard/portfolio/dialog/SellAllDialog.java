package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;

/**
 * Dialog for confirming a "sell all" action.
 * Quantity is locked to the full position - no user input allowed.
 */
public class SellAllDialog extends AbstractSellDialog {

    public SellAllDialog(Share share, PortfolioController controller) {
        super(share, controller);
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("dialog.sellAll.title");
    }

    /**
     * Static quantity display - the user cannot change the quantity here.
     */
    @Override
    protected VBox buildQuantitySection() {
        StyledText label = StyledText.detailLabel(LanguageManager.get("dialog.quantity.label"));

        Label quantity = new Label(MessageFormat.format(
                LanguageManager.get("dialog.quantity.allShares"),
                NUMBER_FORMAT.format(share.getQuantity())));
        quantity.getStyleClass().add("modal-section-value");

        return new VBox(4, label, quantity);
    }

    @Override
    protected void updateSummary() {
        summaryBox.clear();
        hideError();
        renderSummary(share.getQuantity());
    }

    @Override
    protected void onConfirm() {
        if (onConfirmCallback != null) {
            onConfirmCallback.accept(share.getQuantity());
        }
    }
}