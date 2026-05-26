// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanPreview;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Stateless service for computing loan cost previews.
 * All methods are pure calculations — no model state is mutated.
 */
public class LoanPreviewService {

    /**
     * Computes a full cost breakdown for borrowing {@code principal} on {@code offer}.
     *
     * @param offer     the loan product
     * @param principal the desired loan amount; must be positive
     * @return an immutable preview of the repayment schedule
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if principal is not positive
     */
    public LoanPreview preview(LoanOffer offer, BigDecimal principal) {
        Objects.requireNonNull(offer, "Offer cannot be null");
        Objects.requireNonNull(principal, "Principal cannot be null");
        if (principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }

        BigDecimal weeklyInterestAmount = principal
                .multiply(offer.weeklyInterestRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalInterest = weeklyInterestAmount
                .multiply(BigDecimal.valueOf(offer.termWeeks()));
        BigDecimal totalRepayment = principal.add(totalInterest);

        return new LoanPreview(principal, weeklyInterestAmount, totalInterest, totalRepayment);
    }
}
