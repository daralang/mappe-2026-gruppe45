package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Share} class.
 * <p>
 *   This test class verifies that a share is correctly constructed
 *   and that its methods return the expected values.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
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