// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;

/**
 * Immutable snapshot of the calculated cost for borrowing a specific principal
 * against a {@link LoanOffer}.
 *
 * @param principal            the amount the player wishes to borrow
 * @param weeklyInterestAmount interest charged each week (principal × weeklyRate)
 * @param totalInterest        total interest over the full term
 * @param totalRepayment       principal plus total interest
 */
public record LoanPreview(
        BigDecimal principal,
        BigDecimal weeklyInterestAmount,
        BigDecimal totalInterest,
        BigDecimal totalRepayment
) {}
