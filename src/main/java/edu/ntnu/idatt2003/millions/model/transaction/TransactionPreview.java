// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.service.TransactionPreviewService;

import java.math.BigDecimal;

/**
 * Immutable value object representing the computed values for a
 * hypothetical transaction (a "what would happen if..." preview).
 *
 * <p>Produced by {@link TransactionPreviewService}; consumed by the view layer
 * to show a preview without performing the transaction.</p>
 *
 * <p>For purchases, {@code tax} is zero and {@code profit},
 * {@code profitPercent}, and {@code profitInNok} are null. For sales,
 * {@code profit} contains the gain or loss in the stock's native
 * currency, and {@code profitInNok} contains the same value converted
 * to NOK (null when the stock is already priced in NOK).</p>
 *
 * @param gross          gross transaction value in the stock's native currency
 * @param commission     commission charged on the transaction in native currency
 * @param tax            tax on realized profit in native currency; zero for purchases
 * @param total          net amount paid or received in native currency
 * @param totalInNok     total converted to NOK; null when native currency is already NOK
 * @param balanceAfter   projected player balance after the transaction in NOK
 * @param profit         realized gain or loss in native currency; null for purchases
 * @param profitPercent  profit as a fraction of original purchase cost; null for purchases
 * @param profitInNok    profit converted to NOK; null for purchases or when native currency is already NOK
 */
public record TransactionPreview(
        BigDecimal gross,
        BigDecimal commission,
        BigDecimal tax,
        BigDecimal total,
        BigDecimal totalInNok,
        BigDecimal balanceAfter,
        BigDecimal profit,
        BigDecimal profitPercent,
        BigDecimal profitInNok
) {}