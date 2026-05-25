// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Risk profile of a {@link LoanOffer}.
 * Drives presentational choices in the UI such as pill colours and sort order.
 *
 * <ul>
 *   <li>{@link #LOW}    — safe baseline loan, modest amount, low rate</li>
 *   <li>{@link #MEDIUM} — larger amount and a shorter term at a higher rate</li>
 *   <li>{@link #HIGH}   — maximum leverage at the highest rate and shortest term</li>
 * </ul>
 */
public enum LoanRiskLevel {
    LOW,
    MEDIUM,
    HIGH
}