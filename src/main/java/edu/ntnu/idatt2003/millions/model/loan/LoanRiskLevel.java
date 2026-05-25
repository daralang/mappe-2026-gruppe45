// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Risk profile of a {@link LoanOffer}.
 * Drives presentational choices in the UI (pill colours, sort order) and
 * the minimum collateral ratio required to qualify for a given principal.
 *
 * <ul>
 *   <li>{@code LOW} - safe baseline loan, modest amount, low rate</li>
 *   <li>{@code MEDIUM} - larger amount and a shorter term at a higher rate</li>
 *   <li>{@code HIGH} - maximum leverage at the highest rate and shortest term</li>
 * </ul>
 */
public enum LoanRiskLevel {
    LOW,
    MEDIUM,
    HIGH;
}