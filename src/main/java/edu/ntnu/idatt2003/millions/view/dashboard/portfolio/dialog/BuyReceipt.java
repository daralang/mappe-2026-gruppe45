package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.util.LanguageManager;

import java.math.BigDecimal;

/**
 * Receipt shown after a successful purchase.
 */
public class BuyReceipt extends TransactionReceipt {

    public BuyReceipt(
            Transaction transaction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        super(transaction, balanceBefore, balanceAfter);
    }

    @Override
    protected String getTitle() {
        return LanguageManager.get("receipt.buy.title");
    }

    @Override
    protected String getPriceLabel() {
        return LanguageManager.get("receipt.meta.price");
    }

    @Override
    protected BigDecimal getPrice() {
        return transaction.getShare().getPurchasePrice();
    }

    @Override
    protected void renderSummary() {
        BigDecimal gross = transaction.getCalculator().calculateGross();
        BigDecimal commission = transaction.getCalculator().calculateCommission();
        BigDecimal total = transaction.getCalculator().calculateTotal();

        summaryBox.addRow(LanguageManager.get("receipt.summary.gross"),
                NUMBER_FORMAT.format(gross) + " NOK");
        summaryBox.addRow(LanguageManager.get("receipt.summary.commissionBuy"),
                NUMBER_FORMAT.format(commission) + " NOK");
        summaryBox.addTotal(LanguageManager.get("receipt.summary.totalCost"),
                NUMBER_FORMAT.format(total) + " NOK");
    }
}