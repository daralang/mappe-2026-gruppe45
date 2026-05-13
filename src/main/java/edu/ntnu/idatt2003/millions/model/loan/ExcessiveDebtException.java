package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Thrown when a loan would cause the player's total outstanding debt to exceed
 * {@link edu.ntnu.idatt2003.millions.model.player.Player#MAX_DEBT_RATIO} of their
 * current net worth.
 */
public class ExcessiveDebtException extends IllegalArgumentException {

    public ExcessiveDebtException(String message) {
        super(message);
    }
}
