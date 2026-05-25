// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable template describing one available loan product.
 *
 * <p>The interest rate is expressed as a per-week decimal (e.g. {@code 0.0015}
 * for 0.15% weekly).</p>
 *
 * @param id                 stable identifier used for i18n lookups; must not be null or blank
 * @param weeklyInterestRate per-week interest rate as a decimal, strictly positive
 * @param termWeeks          weeks until the principal is due, at least 1
 * @param maxPrincipal       largest principal that may be borrowed, strictly positive
 * @param riskLevel          risk classification used for sort and presentation; must not be null
 */
public record LoanOffer(String id, BigDecimal weeklyInterestRate,
        int termWeeks, BigDecimal maxPrincipal, LoanRiskLevel riskLevel) {

    public LoanOffer {
        Objects.requireNonNull(id, "Id cannot be null");
        Objects.requireNonNull(weeklyInterestRate, "Weekly interest rate cannot be null");
        Objects.requireNonNull(maxPrincipal, "Max principal cannot be null");
        Objects.requireNonNull(riskLevel, "Risk level cannot be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("Id cannot be blank");
        }
        if (weeklyInterestRate.signum() <= 0) {
            throw new IllegalArgumentException("Weekly interest rate must be greater than zero");
        }
        if (termWeeks < 1) {
            throw new IllegalArgumentException("Term must be at least 1 week");
        }
        if (maxPrincipal.signum() <= 0) {
            throw new IllegalArgumentException("Max principal must be greater than zero");
        }
    }
}