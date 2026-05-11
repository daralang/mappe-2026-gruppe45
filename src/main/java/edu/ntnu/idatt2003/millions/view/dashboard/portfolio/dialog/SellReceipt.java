package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Receipt shown after a successful sale, including realized profit/loss info.
 */
public class SellReceipt extends TransactionReceipt {

    private static final Currency NOK = Currency.getInstance("NOK");

    private final TransactionPreview preview;

    public SellReceipt(
            Transaction transaction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            TransactionPreview preview) {
        super(transaction, balanceBefore, balanceAfter);
        this.preview = preview;
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("receipt.sell.title");
    }

    @Override
    protected String getPriceLabel() {
        return LanguageManager.get("receipt.meta.salesPrice");
    }

    @Override
    protected BigDecimal getPrice() {
        return transaction.getShare().getStock().getSalesPrice();
    }

    @Override
    protected void renderSummary() {
        summaryBox.addRow(LanguageManager.get("receipt.summary.gross"),
                NUMBER_FORMAT.format(preview.gross()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("receipt.summary.commissionSell"),
                "−" + NUMBER_FORMAT.format(preview.commission()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("receipt.summary.tax"),
                "−" + NUMBER_FORMAT.format(preview.tax()) + " " + currencyCode());
        summaryBox.addTotal(LanguageManager.get("receipt.summary.totalReceived"),
                NUMBER_FORMAT.format(preview.total()) + " " + currencyCode());
        if (!transaction.getShare().getStock().getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + NUMBER_FORMAT.format(preview.totalInNok()) + " NOK");
        }
    }

    @Override
    protected VBox buildExtraContent() {
        BigDecimal profit = preview.profit();
        BigDecimal profitPercent = preview.profitPercent();

        boolean positive = profit.signum() >= 0;
        String sign = positive ? "+" : "−";
        String pctSign = positive ? "+" : "";
        String colorClass = positive ? "modal-info-positive" : "modal-info-negative";
        String valueClass = positive ? "positive" : "negative";

        Label labelNode = new Label(LanguageManager.get(
                positive ? "receipt.profit.gain" : "receipt.profit.loss"));
        Label primaryNode = new Label(
                sign + NUMBER_FORMAT.format(profit.abs()) + " " + currencyCode()
                + " (" + pctSign + profitPercent.toPlainString() + "%)");
        primaryNode.getStyleClass().add(valueClass);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        VBox infoBox = new VBox(2, new HBox(labelNode, spacer1, primaryNode));
        infoBox.getStyleClass().addAll("modal-info", colorClass);

        if (!transaction.getShare().getStock().getCurrency().equals(NOK)
                && preview.profitInNok() != null) {
            Label secondaryNode = new Label(
                    "= " + sign + NUMBER_FORMAT.format(preview.profitInNok().abs()) + " NOK");
            secondaryNode.getStyleClass().addAll("modal-summary-conversion-text", valueClass);
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            infoBox.getChildren().add(new HBox(spacer2, secondaryNode));
        }

        return infoBox;
    }
}
