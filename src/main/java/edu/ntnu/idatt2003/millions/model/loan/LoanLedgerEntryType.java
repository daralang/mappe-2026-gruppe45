package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Classifies a single entry in the player's loan ledger.
 *
 * <ul>
 *   <li>DISBURSEMENT — principal paid out when a loan is taken</li>
 *   <li>INTEREST     — weekly interest deducted from the player's balance</li>
 *   <li>REPAYMENT    — principal withdrawn when the player repays a loan</li>
 * </ul>
 */
public enum LoanLedgerEntryType {
    DISBURSEMENT,
    INTEREST,
    REPAYMENT
}
