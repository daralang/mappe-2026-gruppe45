package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;

/**
 * Thrown when the net proceeds from a forced share sale are insufficient to cover the
 * player's outstanding obligations for the current week (weekly interest plus any
 * maturing loan principals).
 *
 * <p>This is a recoverable domain rule violation. No state is mutated when this
 * exception is thrown. Callers should catch it and ask the player to select
 * additional shares to sell.
 */
public class InsufficientSaleProceedsException extends Exception {

    private final BigDecimal shortfall;

    /**
     * Creates an exception carrying the shortfall amount.
     *
     * @param shortfall the difference between required obligations and net sale proceeds;
     *                  always positive when this exception is thrown
     */
    public InsufficientSaleProceedsException(BigDecimal shortfall) {
        super("Net sale proceeds are " + shortfall + " NOK short of required obligations.");
        this.shortfall = shortfall;
    }

    /**
     * Creates an exception with a descriptive message and a chained cause.
     * Use this constructor when wrapping a lower-level exception.
     *
     * @param message a human-readable description of the shortfall
     * @param cause   the lower-level exception that triggered this one
     */
    public InsufficientSaleProceedsException(String message, Throwable cause) {
        super(message, cause);
        this.shortfall = null;
    }

    /** @return the amount by which the sale proceeds fall short, or null if not set */
    public BigDecimal getShortfall() {
        return shortfall;
    }
}
