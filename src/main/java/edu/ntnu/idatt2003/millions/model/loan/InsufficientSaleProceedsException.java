package edu.ntnu.idatt2003.millions.model.loan;

/**
 * Thrown when the net proceeds from a forced share sale are insufficient
 * to cover the outstanding weekly interest obligation.
 */
public class InsufficientSaleProceedsException extends Exception {

    public InsufficientSaleProceedsException(String message) {
        super(message);
    }
}
