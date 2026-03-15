package edu.ntnu.idatt2003.millions.model;

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
            assertThrows(IllegalArgumentException.class, () ->
                    new Share(null, quantity, purchasePrice));
        }

        @Test
        @DisplayName("Should throw exception when quantity is null")
        void throwsExceptionWhenQuantityIsNull() {
            // Arrange
            BigDecimal purchasePrice = new BigDecimal("90.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
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
        @DisplayName("Should throw exception when purchase price is null")
        void throwsExceptionWhenPurchasePriceIsNull() {
            // Arrange
            BigDecimal quantity = new BigDecimal("10");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
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
}