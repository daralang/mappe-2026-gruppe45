// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

/**
 * Stateless read service that derives the values displayed in the
 * transactions tab — both per-transaction figures for the history
 * table and aggregated totals for the summary card.
 *
 * <p>Native-currency values are converted to NOK so the table stays denominated
 * in one currency. Both native and NOK figures are returned so views can show
 * NOK in the cell and the native amount in a tooltip without recomputing.</p>
 */
public class TransactionStatsService {

    private static final Currency NOK = Currency.getInstance("NOK");

    /**
     * Computes the display values for a single transaction row.
     *
     * @param transaction the transaction to derive values from
     * @param converter   the active currency converter
     * @return computed values ready to be rendered in the transactions table
     */
    public TransactionStats getStats(Transaction transaction, CurrencyConverter converter) {
        Share share = transaction.getShare();
        Currency nativeCurrency = share.getStock().getCurrency();

        BigDecimal commissionNative = transaction.getCommissionNative();
        BigDecimal taxNative = transaction.getTaxNative();
        BigDecimal commissionNok = converter.convert(commissionNative, nativeCurrency, NOK);
        BigDecimal taxNok = converter.convert(taxNative, nativeCurrency, NOK);
        BigDecimal amountNok = converter.convert(transaction.getSignedTotalNative(), nativeCurrency, NOK);

        return new TransactionStats(
                share.getQuantity(),
                transaction.getPricePerShare(),
                commissionNok,
                taxNok,
                amountNok,
                nativeCurrency.getCurrencyCode(),
                commissionNative,
                taxNative);
    }

    /**
     * Aggregates a list of transactions into Kjøp / Salg / Total figures
     * in NOK, ready to be rendered in the summary card.
     *
     * <p>{@code amountNok} is negative for purchases and positive for sales.
     * The summary therefore exposes {@code purchasesNok} as a non-positive
     * sum of outflows, {@code salesNok} as a non-negative sum of inflows,
     * and {@code totalNok} as their sum — the net cash flow over the range.</p>
     *
     * <p>If the input list is empty all three values are zero. Filtering
     * by week range is the caller's responsibility; this service holds
     * no view-state.</p>
     *
     * @param transactions the transactions to aggregate
     * @param converter    the active currency converter
     * @return summary totals in NOK
     */
    public TransactionSummary getSummary(List<Transaction> transactions, CurrencyConverter converter) {
        BigDecimal purchasesNok = BigDecimal.ZERO;
        BigDecimal salesNok = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            BigDecimal amount = getStats(transaction, converter).amountNok();
            purchasesNok = purchasesNok.add(amount.min(BigDecimal.ZERO));
            salesNok = salesNok.add(amount.max(BigDecimal.ZERO));
        }

        BigDecimal totalNok = purchasesNok.add(salesNok);
        return new TransactionSummary(purchasesNok, salesNok, totalNok);
    }

    /**
     * Display values for one transaction row.
     *
     * <p>Carries both NOK and native-currency figures for commission and
     * tax so the view can render NOK in the cell and the native amount
     * in a tooltip without recomputing or rounding twice.</p>
     *
     * @param quantity            shares bought or sold
     * @param pricePerShare       per-share price at transaction time,
     *                            in native currency
     * @param commissionNok       commission converted to NOK
     * @param taxNok              tax converted to NOK; zero for purchases
     * @param amountNok           signed net amount in NOK — negative for
     *                            purchases, positive for sales
     * @param nativeCurrencyCode  ISO code of the stock's native currency
     * @param commissionNative    commission in native currency, for tooltips
     * @param taxNative           tax in native currency, for tooltips;
     *                            zero for purchases
     */
    public record TransactionStats(
            BigDecimal quantity,
            BigDecimal pricePerShare,
            BigDecimal commissionNok,
            BigDecimal taxNok,
            BigDecimal amountNok,
            String nativeCurrencyCode,
            BigDecimal commissionNative,
            BigDecimal taxNative
    ) {}

    /**
     * Aggregate totals for the transactions tab summary card.
     *
     * <p>{@code purchasesNok} is non-positive (sum of outflows from
     * purchases), {@code salesNok} is non-negative (sum of inflows
     * from sales), and {@code totalNok} is simply {@code purchasesNok +
     * salesNok} — the net cash flow over the aggregated range.</p>
     *
     * @param purchasesNok total purchase outflow, NOK (non-positive)
     * @param salesNok     total sale inflow, NOK (non-negative)
     * @param totalNok     net cash flow, NOK
     */
    public record TransactionSummary(
            BigDecimal purchasesNok,
            BigDecimal salesNok,
            BigDecimal totalNok
    ) {}
}