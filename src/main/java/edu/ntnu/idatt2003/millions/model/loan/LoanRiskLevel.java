package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;

/**
 * Risk profile of a {@link LoanOffer}.
 * Drives presentational choices in the UI (pill colours, sort order) and
 * the minimum collateral ratio required to qualify for a given principal.
 *
 * <ul>
 *   <li>{@code LOW} - safe baseline loan, modest amount, low rate</li>
 *   <li>{@code MEDIUM} - larger amount and a shorter term at a higher rate</li>
 *   <li>{@code HIGH} - maximum leverage at the highest rate and shortest term</li>
 * </ul>
 */
public enum LoanRiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    /**
     * Returns the minimum collateral ratio for this risk level.
     * The player's net worth must be at least {@code principal × collateralRatio()}
     * to qualify for a loan of that size.
     *
     * @return collateral ratio as a decimal (e.g. {@code 0.10} = 10 %)
     */
    public BigDecimal collateralRatio() {
        return switch (this) {
            case LOW    -> new BigDecimal("0.10");
            case MEDIUM -> new BigDecimal("0.20");
            case HIGH   -> new BigDecimal("0.30");
        };
    }
}