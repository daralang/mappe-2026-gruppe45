// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Classifies a single entry in the player's loan ledger.
 *
 * <ul>
 *   <li>{@link #DISBURSEMENT} — principal paid out when a loan is taken</li>
 *   <li>{@link #INTEREST}     — weekly interest deducted from the player's balance</li>
 *   <li>{@link #REPAYMENT}    — principal withdrawn when the player repays a loan</li>
 * </ul>
 */
public enum LoanLedgerEntryType {
    DISBURSEMENT,
    INTEREST,
    REPAYMENT
}
