package edu.ntnu.idatt2003.millions.model.transaction;

import java.math.BigDecimal;

/**
 * Immutable value object representing the computed values for a
 * hypothetical transaction (a "what would happen if..." preview).
 *
 * <p>Produced by {@link TransactionPreviewService} and consumed by
 * the view layer to display gross, commission, tax, total, and the
 * resulting balance without performing the transaction.</p>
 *
 * <p>For purchases, {@code tax} is zero and {@code profit} and
 * {@code profitPercent} are null. For sales, {@code profit} contains
 * the gain or loss on the sale.</p>
 */
public record TransactionPreview(
        BigDecimal gross,
        BigDecimal commission,
        BigDecimal tax,
        BigDecimal total,
        BigDecimal balanceAfter,
        BigDecimal profit,
        BigDecimal profitPercent
) {}