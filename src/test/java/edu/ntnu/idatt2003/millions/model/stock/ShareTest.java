package edu.ntnu.idatt2003.millions.model.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Share} class.
 * <p>
 * This test class verifies that a share is correctly constructed
 * and that its methods return the expected values.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class ShareTest {

    private Stock stock;
    private Share share;

    @BeforeEach
    void setUp() {
        stock = new Stock("NKE", "Nike, Inc",
                new ArrayList<>(List.of(new BigDecimal("120.00"))));
        share = new Share(stock, new BigDecimal("10"), new BigDecimal("90.00"));
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    @Nested
    @DisplayName("Share()")
    class Constructor {

        @Test
        @DisplayName("Should throw exception when stock is null")
        void throwsExceptionWhenStockIsNull() {
            // Arrange
            BigDecimal quantity = new BigDecimal("10");
            BigDecimal purchasePrice = new BigDecimal("90.00");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Share(null, quantity, purchasePrice));
        }

        @Test
        @DisplayName("Should throw exception when quantity is null")
        void throwsExceptionWhenQuantityIsNull() {
            // Arrange
            BigDecimal purchasePrice = new BigDecimal("90.00");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Share(stock, null, purchasePrice));
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void throwsExceptionWhenQuantityIsNegative() {
            // Arrange
            BigDecimal quantity = new BigDecimal("-1");
            BigDecimal purchasePrice = new BigDecimal("90.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Share(stock, quantity, purchasePrice));
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void throwsExceptionWhenQuantityIsZero() {
            // Arrange
            BigDecimal purchasePrice = new BigDecimal("90.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Share(stock, BigDecimal.ZERO, purchasePrice));
        }

        @Test
        @DisplayName("Should throw exception when purchase price is null")
        void throwsExceptionWhenPurchasePriceIsNull() {
            // Arrange
            BigDecimal quantity = new BigDecimal("10");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Share(stock, quantity, null));
        }

        @Test
        @DisplayName("Should throw exception when purchase price is negative")
        void throwsExceptionWhenPurchasePriceIsNegative() {
            // Arrange
            BigDecimal quantity = new BigDecimal("10");
            BigDecimal purchasePrice = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Share(stock, quantity, purchasePrice));
        }
    }

    @Nested
    @DisplayName("getStock()")
    class GetStock {

        @Test
        @DisplayName("Should return the stock associated with the share")
        void returnsCorrectStock() {
            // Act
            Stock result = share.getStock();
            // Assert
            assertEquals(stock, result);
        }
    }

    @Nested
    @DisplayName("getQuantity()")
    class GetQuantity {

        @Test
        @DisplayName("Should return the correct quantity")
        void returnsCorrectQuantity() {
            // Act
            BigDecimal quantity = share.getQuantity();
            // Assert
            assertEquals(new BigDecimal("10"), quantity);
        }
    }

    @Nested
    @DisplayName("getPurchasePrice()")
    class GetPurchasePrice {

        @Test
        @DisplayName("Should return the correct purchase price")
        void returnsCorrectPurchasePrice() {
            // Act
            BigDecimal price = share.getPurchasePrice();
            // Assert
            assertEquals(new BigDecimal("90.00"), price);
        }
    }

    @Nested
    @DisplayName("getReturnPercent()")
    class GetReturnPercent {

        @Test
        @DisplayName("Should return positive percent when current price exceeds purchase price")
        void returnsPositivePercentForGain() {
            // Arrange — stock price=120, qty=10, purchasePrice=90
            // cost=900, currentValue=1200, returnNative=300
            // returnPercent = 300×100/900 = 33.333... → HALF_UP 2dp = 33.33
            // Act
            BigDecimal result = share.getReturnPercent();
            // Assert
            assertBigDecimalEquals(new BigDecimal("33.33"), result);
        }

        @Test
        @DisplayName("Should return negative percent when current price is below purchase price")
        void returnsNegativePercentForLoss() {
            // Arrange — stock price=80, qty=10, purchasePrice=100
            // cost=1000, currentValue=800, returnNative=−200
            // returnPercent = −200×100/1000 = −20.00
            Stock losingStock = new Stock("AAPL", "Apple Inc.",
                    new ArrayList<>(List.of(new BigDecimal("80.00"))));
            Share losingShare = new Share(losingStock, new BigDecimal("10"), new BigDecimal("100.00"));
            // Act
            BigDecimal result = losingShare.getReturnPercent();
            // Assert
            assertBigDecimalEquals(new BigDecimal("-20.00"), result);
        }

        @Test
        @DisplayName("Should return zero when cost basis is zero")
        void returnsZeroWhenCostBasisIsZero() {
            // Arrange — purchasePrice=0, qty=10: cost=0 → zero-cost guard activates
            Share zeroCostShare = new Share(stock, new BigDecimal("10"), BigDecimal.ZERO);
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, zeroCostShare.getReturnPercent());
        }
    }

    @Nested
    @DisplayName("mergedWith()")
    class MergedWith {

        @Test
        @DisplayName("Should throw exception when merging shares of different stocks")
        void throwsExceptionWhenSymbolsDiffer() {
            // Arrange
            Stock otherStock = new Stock("AAPL", "Apple Inc.",
                    new ArrayList<>(List.of(new BigDecimal("200.00"))));
            Share otherShare = new Share(otherStock, new BigDecimal("5"), new BigDecimal("180.00"));
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> share.mergedWith(otherShare));
        }
    }
}