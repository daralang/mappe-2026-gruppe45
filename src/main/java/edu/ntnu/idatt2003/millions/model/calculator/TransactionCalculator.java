// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.calculator;

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
     * @return the gross transaction value
     */
    BigDecimal calculateGross();

    /**
     * Calculates the commission fee for the transaction.
     *
     * @return the commission amount
     */
    BigDecimal calculateCommission();

    /**
     * Calculates the tax applied to the transaction.
     *
     * @return the tax amount
     */
    BigDecimal calculateTax();

    /**
     * Calculates the total amount the player pays or receives for the transaction.
     *
     * @return the total transaction amount
     */
    BigDecimal calculateTotal();
}
