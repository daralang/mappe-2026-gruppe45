package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

/**
 * Receipt shown after a successful sale, including realized profit/loss info.
 */
public class SellReceipt extends TransactionReceipt {

    public SellReceipt(
            Transaction transaction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        super(transaction, balanceBefore, balanceAfter);
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
        BigDecimal gross = transaction.getCalculator().calculateGross();
        BigDecimal commission = transaction.getCalculator().calculateCommission();
        BigDecimal tax = transaction.getCalculator().calculateTax();
        BigDecimal total = transaction.getCalculator().calculateTotal();

        summaryBox.addRow(LanguageManager.get("receipt.summary.gross"),
                NUMBER_FORMAT.format(gross) + " NOK");
        summaryBox.addRow(LanguageManager.get("receipt.summary.commissionSell"),
                "\u2212" + NUMBER_FORMAT.format(commission) + " NOK");
        summaryBox.addRow(LanguageManager.get("receipt.summary.tax"),
                "\u2212" + NUMBER_FORMAT.format(tax) + " NOK");
        summaryBox.addTotal(LanguageManager.get("receipt.summary.totalReceived"),
                NUMBER_FORMAT.format(total) + " NOK");
    }

    @Override
    protected VBox buildExtraContent() {
        // Safe cast: SellReceipt is only constructed for Sale transactions,
        // which always use SalesCalculator.
        SalesCalculator calc = (SalesCalculator) transaction.getCalculator();
        BigDecimal profit = calc.calculateProfit();
        BigDecimal profitPercent = calc.calculateProfitPercent();

        boolean positive = profit.signum() >= 0;
        String sign = positive ? "+" : "\u2212";
        String pctSign = positive ? "+" : "";

        Label label = new Label(positive
                ? LanguageManager.get("receipt.profit.gain")
                : LanguageManager.get("receipt.profit.loss"));
        label.getStyleClass().add("modal-summary-label");

        Label value = new Label(
                sign + NUMBER_FORMAT.format(profit.abs()) + " NOK ("
                        + pctSign + profitPercent.toPlainString() + "%)");
        value.getStyleClass().add("modal-summary-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(label, spacer, value);
        row.getStyleClass().addAll(
                "modal-info",
                positive ? "modal-info-positive" : "modal-info-negative"
        );

        return new VBox(row);
    }
}