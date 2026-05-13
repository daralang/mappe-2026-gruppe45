package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.calculator.PurchaseCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

/**
 * Stateless read service that derives the values displayed in the
 * transactions tab — both per-transaction figures for the history
 * table and aggregated totals for the summary card.
 *
 * <p>Sits alongside {@code PlayerStatsService}, {@code PortfolioService}
 * and {@code RealizedReturnsService}: like them, it holds no state, takes
 * the model objects it needs as method arguments, and returns plain values
 * the view can render. This is the pattern SoC.md prescribes for derived
 * values that don't naturally live on a single domain object — the
 * computation crosses {@link Transaction}, {@link Share}, the calculator
 * tied to its subclass, and the active {@link CurrencyConverter}.</p>
 *
 * <p>Two pieces of irregularity are encapsulated here so the view doesn't
 * have to know about them:</p>
 * <ul>
 *   <li><b>Frozen vs. recomputed values.</b> {@link Sale} freezes its
 *       financial figures at construction so they survive serialisation,
 *       while {@link Purchase} keeps its {@code calculator} as a transient
 *       field which is null after loading a saved game. A fresh
 *       {@link PurchaseCalculator} is built from the share to recover
 *       commission and total for purchases.</li>
 *   <li><b>Currency conversion.</b> Native-currency values are converted
 *       to NOK so the table can stay denominated in one currency. Both
 *       the native and the NOK value are returned so views can show NOK
 *       in the main cell and the native amount in a tooltip.</li>
 * </ul>
 */
public class TransactionStatsService {

    /** Currency every NOK-denominated value is converted to. */
    private static final Currency NOK = Currency.getInstance("NOK");

    /** Scale used when dividing gross by quantity to recover per-share price. */
    private static final int PRICE_SCALE = 4;

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

        if (transaction instanceof Sale sale) {
            return statsForSale(sale, share, nativeCurrency, converter);
        }
        return statsForPurchase(share, nativeCurrency, converter);
    }

    /**
     * Aggregates a list of transactions into Kjøp / Salg / Total figures
     * in NOK, ready to be rendered in the summary card.
     *
     * <p>The aggregation reuses {@link #getStats} per transaction so the
     * summary stays consistent with the table: a transaction's
     * {@code amountNok} is already negative for purchases and positive
     * for sales. The summary therefore exposes {@code purchasesNok} as
     * a non-positive sum of outflows, {@code salesNok} as a non-negative
     * sum of inflows, and {@code totalNok} as their sum — i.e. the net
     * cash flow over the range.</p>
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
            TransactionStats stats = getStats(transaction, converter);
            if (transaction instanceof Purchase) {
                purchasesNok = purchasesNok.add(stats.amountNok());
            } else {
                salesNok = salesNok.add(stats.amountNok());
            }
        }

        BigDecimal totalNok = purchasesNok.add(salesNok);
        return new TransactionSummary(purchasesNok, salesNok, totalNok);
    }

    /**
     * Builds the stats record for a sale, reading the frozen gross,
     * commission, tax and total directly off the sale.
     *
     * @param sale            the sale to read values from
     * @param share           the underlying share, used for quantity
     * @param nativeCurrency  the stock's native currency
     * @param converter       the active currency converter
     * @return populated stats record
     */
    private TransactionStats statsForSale(Sale sale, Share share,
                                          Currency nativeCurrency,
                                          CurrencyConverter converter) {
        BigDecimal pricePerShare = sale.getGross()
                .divide(share.getQuantity(), PRICE_SCALE, RoundingMode.HALF_UP);
        BigDecimal commissionNok = converter.convert(sale.getCommission(), nativeCurrency, NOK);
        BigDecimal taxNok = converter.convert(sale.getTax(), nativeCurrency, NOK);
        BigDecimal amountNok = converter.convert(sale.getTotal(), nativeCurrency, NOK);

        return new TransactionStats(
                share.getQuantity(),
                pricePerShare,
                commissionNok,
                taxNok,
                amountNok,
                nativeCurrency.getCurrencyCode(),
                sale.getCommission(),
                sale.getTax());
    }

    /**
     * Builds the stats record for a purchase, reconstructing commission
     * and total from a fresh {@link PurchaseCalculator} since the
     * transaction's own calculator is transient and may be null after
     * a saved game is loaded. Tax is always zero for purchases, and the
     * NOK amount is negated to signal an outflow.
     *
     * @param share          the share that was purchased
     * @param nativeCurrency the stock's native currency
     * @param converter      the active currency converter
     * @return populated stats record
     */
    private TransactionStats statsForPurchase(Share share,
                                              Currency nativeCurrency,
                                              CurrencyConverter converter) {
        PurchaseCalculator calculator = new PurchaseCalculator(share);
        BigDecimal commissionNative = calculator.calculateCommission();
        BigDecimal totalNative = calculator.calculateTotal();

        BigDecimal commissionNok = converter.convert(commissionNative, nativeCurrency, NOK);
        BigDecimal amountNok = converter.convert(totalNative, nativeCurrency, NOK).negate();

        return new TransactionStats(
                share.getQuantity(),
                share.getPurchasePrice(),
                commissionNok,
                BigDecimal.ZERO,
                amountNok,
                nativeCurrency.getCurrencyCode(),
                commissionNative,
                BigDecimal.ZERO);
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