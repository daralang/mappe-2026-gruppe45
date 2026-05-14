package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;

/**
 * Thrown when a loan application would cause the player's total outstanding debt to exceed
 * {@link edu.ntnu.idatt2003.millions.model.player.Player#MAX_DEBT_RATIO} of their current
 * net worth.
 *
 * <p>This is a recoverable domain rule violation. No state is mutated when this exception
 * is thrown. Callers should catch it and inform the player that their borrowing capacity
 * has been reached.
 */
public class ExcessiveDebtException extends Exception {

    private final BigDecimal debtAfter;
    private final BigDecimal capacity;

    /**
     * Creates an exception carrying the computed debt and capacity figures.
     *
     * @param debtAfter the total debt the player would have after the rejected loan
     * @param capacity  the maximum permitted debt at the time of the request
     */
    public ExcessiveDebtException(BigDecimal debtAfter, BigDecimal capacity) {
        super("Loan would bring total debt to " + debtAfter
                + " NOK, exceeding the capacity of " + capacity + " NOK.");
        this.debtAfter = debtAfter;
        this.capacity = capacity;
    }

    /**
     * Creates an exception with a descriptive message and a chained cause.
     * Use this constructor when wrapping a lower-level exception.
     *
     * @param message a human-readable description of the violation
     * @param cause   the lower-level exception that triggered this one
     */
    public ExcessiveDebtException(String message, Throwable cause) {
        super(message, cause);
        this.debtAfter = null;
        this.capacity = null;
    }

    /** @return the total debt the player would have held after the rejected loan, or null if not set */
    public BigDecimal getDebtAfter() {
        return debtAfter;
    }

    /** @return the maximum permitted debt at the time of the rejected request, or null if not set */
    public BigDecimal getCapacity() {
        return capacity;
    }
}
