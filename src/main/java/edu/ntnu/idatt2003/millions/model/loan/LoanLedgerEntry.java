package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An immutable record of a single loan-related money movement.
 *
 * <p>The {@code amount} field is signed:
 * <ul>
 *   <li>positive for {@link LoanLedgerEntryType#DISBURSEMENT} (cash in)</li>
 *   <li>negative for {@link LoanLedgerEntryType#INTEREST} and
 *       {@link LoanLedgerEntryType#REPAYMENT} (cash out)</li>
 * </ul>
 *
 * @param week   the game week in which the movement occurred; must be &gt;= 1
 * @param loan   the loan this entry belongs to; must not be null
 * @param type   the kind of movement; must not be null
 * @param amount the signed amount in NOK; must not be null
 */
public record LoanLedgerEntry(int week, Loan loan, LoanLedgerEntryType type, BigDecimal amount) {

    public LoanLedgerEntry {
        if (week < 1) throw new IllegalArgumentException("week must be >= 1");
        Objects.requireNonNull(loan,   "loan cannot be null");
        Objects.requireNonNull(type,   "type cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
    }
}
