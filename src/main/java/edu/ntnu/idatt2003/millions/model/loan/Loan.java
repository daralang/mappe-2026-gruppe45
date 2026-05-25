// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An active loan taken by a player against a {@link LoanOffer}.
 *
 * @param offer        the offer this loan was issued under
 * @param principal    the amount borrowed; must be positive
 * @param takenAtWeek  the game week when the loan was taken; must be non-negative
 */
public record Loan(LoanOffer offer, BigDecimal principal, int takenAtWeek) {

    public Loan {
        Objects.requireNonNull(offer, "Offer cannot be null");
        Objects.requireNonNull(principal, "Principal cannot be null");
        if (principal.signum() <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (principal.compareTo(offer.maxPrincipal()) > 0) {
            throw new IllegalArgumentException(
                    "Principal " + principal + " exceeds offer maximum " + offer.maxPrincipal());
        }
        if (takenAtWeek < 0) {
            throw new IllegalArgumentException("takenAtWeek must be non-negative");
        }
    }

    /** Weekly interest charged on this loan (principal × weekly rate, rounded to 2 dp). */
    public BigDecimal weeklyInterest() {
        return principal.multiply(offer.weeklyInterestRate()).setScale(2, RoundingMode.HALF_UP);
    }

    /** Total interest over the full term (weeklyInterest × termWeeks). */
    public BigDecimal totalInterest() {
        return weeklyInterest().multiply(BigDecimal.valueOf(offer.termWeeks()));
    }

    /** Total amount to repay (principal + totalInterest). */
    public BigDecimal totalRepayment() {
        return principal.add(totalInterest());
    }

    /**
     * Number of weeks elapsed since the loan was taken.
     *
     * <p>Precondition: {@code currentWeek >= takenAtWeek}. Behaviour for earlier
     * weeks is undefined (the result will be negative).
     *
     * @param currentWeek the current game week
     * @return number of weeks since the loan was taken
     */
    public int weeksElapsed(int currentWeek) {
        return currentWeek - takenAtWeek;
    }

    /**
     * Number of weeks remaining in the loan term, floored at zero.
     *
     * @param currentWeek the current game week
     * @return weeks left in the term, or zero if the loan is at or past its due date
     */
    public int weeksRemaining(int currentWeek) {
        return Math.max(0, offer.termWeeks() - weeksElapsed(currentWeek));
    }

    /**
     * Interest already paid up to {@code currentWeek} (weeklyInterest × weeksElapsed).
     *
     * <p>Precondition: {@code currentWeek >= takenAtWeek}. Behaviour for earlier
     * weeks is undefined (the result will be negative).
     *
     * @param currentWeek the current game week
     * @return total interest charged so far
     */
    public BigDecimal interestPaid(int currentWeek) {
        return weeklyInterest().multiply(BigDecimal.valueOf(weeksElapsed(currentWeek)));
    }

    /**
     * Interest that would be saved by repaying now (weeklyInterest × weeksRemaining).
     *
     * @param currentWeek the current game week
     * @return interest avoided by early repayment
     */
    public BigDecimal interestSaved(int currentWeek) {
        return weeklyInterest().multiply(BigDecimal.valueOf(weeksRemaining(currentWeek)));
    }

    /**
     * Returns true when this loan's principal is due — that is, when
     * {@code currentWeek >= takenAtWeek + termWeeks}.
     * Returns true for the due week and all subsequent weeks;
     * preventing duplicate settlement is the caller's responsibility.
     *
     * @param currentWeek the current game week
     * @return true if the loan term has expired
     */
    public boolean isDueThisWeek(int currentWeek) {
        return weeksRemaining(currentWeek) == 0;
    }
}
