package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionPreview;
import edu.ntnu.idatt2003.millions.util.LanguageManager;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Receipt shown after a successful purchase.
 */
public class BuyReceipt extends TransactionReceipt {

    private static final Currency NOK = Currency.getInstance("NOK");

    private final TransactionPreview preview;

    public BuyReceipt(
            Transaction transaction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            TransactionPreview preview) {
        super(transaction, balanceBefore, balanceAfter);
        this.preview = preview;
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
        summaryBox.addRow(LanguageManager.get("receipt.summary.gross"),
                NUMBER_FORMAT.format(preview.gross()) + " " + currencyCode());
        summaryBox.addRow(LanguageManager.get("receipt.summary.commissionBuy"),
                NUMBER_FORMAT.format(preview.commission()) + " " + currencyCode());
        summaryBox.addTotal(LanguageManager.get("receipt.summary.totalCost"),
                NUMBER_FORMAT.format(preview.total()) + " " + currencyCode());
        if (!transaction.getShare().getStock().getCurrency().equals(NOK)) {
            summaryBox.addConversion("= " + NUMBER_FORMAT.format(preview.totalInNok()) + " NOK");
        }
    }
}
