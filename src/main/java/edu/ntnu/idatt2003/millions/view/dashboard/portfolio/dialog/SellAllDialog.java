package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

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
        return "Selg alt";
    }

    /**
     * Static quantity display - the user cannot change the quantity here.
     */
    @Override
    protected VBox buildQuantitySection() {
        Label label = new Label("Antall andeler");
        label.getStyleClass().add("modal-section-label");

        Label quantity = new Label(
                NUMBER_FORMAT.format(share.getQuantity())
                        + " (alle dine andeler)");
        quantity.getStyleClass().add("modal-section-value");

        return new VBox(4, label, quantity);
    }

    @Override
    protected void updateSummary() {
        summaryBox.getChildren().clear();
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