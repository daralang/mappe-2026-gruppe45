package edu.ntnu.idatt2003.millions.model.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RandomPriceSimulator}.
 */
@DisplayName("RandomPriceSimulator")
class RandomPriceSimulatorTest {

    private RandomPriceSimulator simulator;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        simulator = new RandomPriceSimulator();
    }

    @Nested
    @DisplayName("nextPrice()")
    class NextPrice {

        @Test
        @DisplayName("Should return a price within ±10% of the current price")
        void returnsNewPriceWithinBounds() {
            // Arrange
            BigDecimal current = new BigDecimal("100.00");
            BigDecimal lower = new BigDecimal("90.00");
            BigDecimal upper = new BigDecimal("110.00");

            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                BigDecimal next = simulator.nextPrice(current);
                assertTrue(
                        next.compareTo(lower) >= 0 && next.compareTo(upper) <= 0,
                        "Price out of ±10% bounds: " + next);
            }
        }

        @Test
        @DisplayName("Should return a price with exactly two decimal places")
        void returnsPriceWithTwoDecimalPlaces() {
            // Arrange
            BigDecimal current = new BigDecimal("100.00");

            // Act & Assert
            for (int i = 0; i < 100; i++) {
                BigDecimal next = simulator.nextPrice(current);
                assertEquals(2, next.scale(), "Expected scale 2, got: " + next);
            }
        }

        @Test
        @DisplayName("Should never return a price below the minimum floor of 0.01")
        void neverReturnsPriceBelowMinimum() {
            // Arrange
            BigDecimal current = new BigDecimal("100.00");
            BigDecimal minPrice = new BigDecimal("0.01");

            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                BigDecimal next = simulator.nextPrice(current);
                assertTrue(
                        next.compareTo(minPrice) >= 0,
                        "Price fell below minimum: " + next);
            }
        }

        @Test
        @DisplayName("Should return the minimum floor price when input is at the minimum")
        void floorsPriceAtMinimumForTinyInput() {
            // Arrange
            BigDecimal tinyPrice = new BigDecimal("0.01");
            BigDecimal expected = new BigDecimal("0.01");

            // Act & Assert — at 0.01, both ±10% round back to 0.01
            for (int i = 0; i < 100; i++) {
                BigDecimal next = simulator.nextPrice(tinyPrice);
                assertEquals(0, expected.compareTo(next),
                        "Expected 0.01 for tiny input, got: " + next);
            }
        }
    }
}
