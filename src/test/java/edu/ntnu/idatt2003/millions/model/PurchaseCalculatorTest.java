package edu.ntnu.idatt2003.millions.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PurchaseCalculatorTest {
    private Share share;
    private PurchaseCalculator calculator;

    @BeforeEach
    void setUp() {
        share = new Share(
                new Stock("DCL", "Dara Inc", new ArrayList<>(List.of(new BigDecimal("70")))),
                new BigDecimal("10"), new BigDecimal("30"));
        calculator = new PurchaseCalculator(share);
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal calculated) {
        assertEquals(0, expected.compareTo(calculated),
                "Expected " + expected.toPlainString() + " to be equal to " + calculated.toPlainString());
    }

    @Nested
    @DisplayName("calculateGross()")
    class CalculateGross {

        @Test
        @DisplayName("Should return purchase price times quantity")
        void returnsPurchasePriceTimesQuantity() {
            // Arrange
            BigDecimal expected = new BigDecimal("30").multiply(new BigDecimal("10"));
            // Act
            BigDecimal calculated = calculator.calculateGross();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }
    }

    @Nested
    @DisplayName("calculateCommission()")
    class CalculateCommission {

        @Test
        @DisplayName("Should return 0.5% of gross value")
        void returnsPointFivePercentOfGross() {
            // Arrange
            BigDecimal gross = new BigDecimal("30").multiply(new BigDecimal("10"));
            BigDecimal expected = gross.multiply(new BigDecimal("0.005"));
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
        @DisplayName("Should always return zero for purchases")
        void returnsZero() {
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, calculator.calculateTax());
        }
    }

    @Nested
    @DisplayName("calculateTotal()")
    class CalculateTotal {

        @Test
        @DisplayName("Should return gross plus commission")
        void returnsGrossPlusCommission() {
            // Arrange
            BigDecimal gross = new BigDecimal("30").multiply(new BigDecimal("10"));
            BigDecimal commission = gross.multiply(new BigDecimal("0.005"));
            BigDecimal expected = gross.add(commission);
            // Act
            BigDecimal calculated = calculator.calculateTotal();
            // Assert
            assertBigDecimalEquals(expected, calculated);
        }
    }
}
