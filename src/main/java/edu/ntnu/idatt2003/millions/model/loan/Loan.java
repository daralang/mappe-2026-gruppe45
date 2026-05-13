package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An active loan taken by a player against a {@link LoanOffer}.
 *
 * @param offer     the offer this loan was issued under
 * @param principal the amount borrowed; must be positive
 */
public record Loan(LoanOffer offer, BigDecimal principal) {

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
    }
}
