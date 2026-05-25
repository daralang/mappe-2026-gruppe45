package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.service.TransactionPreviewService;

import java.math.BigDecimal;

/**
 * Immutable value object representing the computed values for a
 * hypothetical transaction (a "what would happen if..." preview).
 *
 * <p>Produced by {@link TransactionPreviewService} and consumed by
 * the view layer to display gross, commission, tax, total, and the
 * resulting balance without performing the transaction.</p>
 *
 * <p>For purchases, {@code tax} is zero and {@code profit},
 * {@code profitPercent}, and {@code profitInNok} are null. For sales,
 * {@code profit} contains the gain or loss in the stock's native
 * currency, and {@code profitInNok} contains the same value converted
 * to NOK (null when the stock is already priced in NOK).</p>
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