package edu.ntnu.idatt2003.millions.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link SalesCalculator} class.
 * <p>
 *   This test class verifies the financial calculations performed during a sale transaction,
 *   including gross value, commission, tax, and total payout.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
 * </p>
 */
class SalesCalculatorTest {

    private SalesCalculator calculator;

    @BeforeEach
    void setUp() {
        Share share = new Share(
                new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("100")))),
                new BigDecimal("10"), new BigDecimal("30"));
        calculator = new SalesCalculator(share);
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal calculated) {
        assertEquals(0, expected.compareTo(calculated),
                "Expected " + expected.toPlainString() + " to be equal to " + calculated.toPlainString());
    }

    @Nested
    @DisplayName("SalesCalculator()")
    class Constructor {

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            //noinspection DataFlowIssue
            assertThrows(IllegalArgumentException.class, () ->
                    new SalesCalculator(null));
        }
    }

    @Nested
    @DisplayName("calculateGross()")
    class CalculateGross {

        @Test
        @DisplayName("Should return sales price times quantity")
        void returnsSalesPriceTimesQuantity() {
            // Arrange
            BigDecimal expected = new BigDecimal("100").multiply(new BigDecimal("10"));
            // Act
            BigDecimal calculated = calculator.calculateGross();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }

        @Test
        @DisplayName("Should return zero when quantity is zero")
        void returnsZeroWhenQuantityIsZero() {
            // Arrange
            Share zeroShare = new Share(
                    new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("100")))),
                    new BigDecimal("0"), new BigDecimal("30"));
            SalesCalculator zeroCalculator = new SalesCalculator(zeroShare);
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, zeroCalculator.calculateGross());
        }
    }

    @Nested
    @DisplayName("calculateCommission()")
    class CalculateCommission {

        @Test
        @DisplayName("Should return 1% of gross value")
        void returnsOnePercentOfGross() {
            // Arrange
            BigDecimal gross = new BigDecimal("100").multiply(new BigDecimal("10"));
            BigDecimal expected = gross.multiply(new BigDecimal("0.01"));
            // Act
            BigDecimal calculated = calculator.calculateCommission();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }
    }

    @Nested
    @DisplayName("calculateTax()")
    class CalculateTax {

        @Test
        @DisplayName("Should return 30% of profit when sale is profitable")
        void returnsThirtyPercentOfProfitWhenProfitable() {
            // Arrange
            BigDecimal gross = new BigDecimal("100").multiply(new BigDecimal("10"));
            BigDecimal commission = gross.multiply(new BigDecimal("0.01"));
            BigDecimal purchaseCosts = new BigDecimal("30").multiply(new BigDecimal("10"))
                    .multiply(new BigDecimal("1.005"));
            BigDecimal profit = gross.subtract(commission).subtract(purchaseCosts);
            BigDecimal expected = profit.multiply(new BigDecimal("0.3"));
            // Act
            BigDecimal calculated = calculator.calculateTax();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }

        @Test
        @DisplayName("Should return zero when sale is at a loss")
        void returnsZeroWhenAtLoss() {
            // Arrange
            Share lossShare = new Share(
                    new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("10")))),
                    new BigDecimal("10"), new BigDecimal("100"));
            SalesCalculator lossCalculator = new SalesCalculator(lossShare);
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, lossCalculator.calculateTax());
        }

        @Test
        @DisplayName("Should return zero when quantity is zero")
        void returnsZeroWhenQuantityIsZero() {
            // Arrange
            Share zeroShare = new Share(
                    new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("100")))),
                    new BigDecimal("0"), new BigDecimal("30"));
            SalesCalculator zeroCalculator = new SalesCalculator(zeroShare);
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, zeroCalculator.calculateTax());
        }
    }

    @Nested
    @DisplayName("calculateTotal()")
    class CalculateTotal {

        @Test
        @DisplayName("Should return gross minus commission minus tax")
        void returnsGrossMinusCommissionMinusTax() {
            // Arrange
            BigDecimal gross = new BigDecimal("100").multiply(new BigDecimal("10"));
            BigDecimal commission = gross.multiply(new BigDecimal("0.01"));
            BigDecimal purchaseCosts = new BigDecimal("30").multiply(new BigDecimal("10"))
                    .multiply(new BigDecimal("1.005"));
            BigDecimal profit = gross.subtract(commission).subtract(purchaseCosts);
            BigDecimal tax = profit.multiply(new BigDecimal("0.3"));
            BigDecimal expected = gross.subtract(commission).subtract(tax);
            // Act
            BigDecimal calculated = calculator.calculateTotal();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }

        @Test
        @DisplayName("Should return zero when quantity is zero")
        void returnsZeroWhenQuantityIsZero() {
            // Arrange
            Share zeroShare = new Share(
                    new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("100")))),
                    new BigDecimal("0"), new BigDecimal("30"));
            SalesCalculator zeroCalculator = new SalesCalculator(zeroShare);
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, zeroCalculator.calculateTotal());
        }
    }
}