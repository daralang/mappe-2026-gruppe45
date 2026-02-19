package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;

/**
 * Interface for calculating the financial components of a stock transaction.
 *
 * <p>Implementing classes are responsible for providing calculation logic
 * specific to their transaction type (e.g. purchase or sale).</p>
 */
public interface TransactionCalculator {

    /**
     * Calculates the gross value of the transaction.
     *
     * @return the gross value as a {@link BigDecimal}
     */
    BigDecimal calculateGross();

    /**
     * Calculates the commission fee for the transaction.
     *
     * @return the commission amount as a {@link BigDecimal}
     */
    BigDecimal calculateCommission();

    /**
     * Calculates the tax applied to the transaction.
     *
     * @return the tax amount as a {@link BigDecimal}
     */
    BigDecimal calculateTax();

    /**
     * Calculates the total cost or profit of the transaction,
     * typically the sum of gross, commission, and tax.
     *
     * @return the total amount as a {@link BigDecimal}
     */
    BigDecimal calculateTotal();
}
