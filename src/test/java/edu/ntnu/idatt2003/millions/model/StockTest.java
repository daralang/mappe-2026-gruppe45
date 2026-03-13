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
 * Unit tests for the {@link Stock} class.
 * <p>
 *   This test class verifies that a stock is correctly constructed
 *   and that its methods return the expected values.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
 * </p>
 */
class StockTest {

    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("NKE", "Nike, Inc",
                new ArrayList<>(List.of(new BigDecimal("100.00"))));
    }

    @Nested
    @DisplayName("getSymbol()")
    class GetSymbol {

        @Test
        @DisplayName("Should return the correct symbol")
        void returnsCorrectSymbol() {
            // Act
            String symbol = stock.getSymbol();
            // Assert
            assertEquals("NKE", symbol);
        }
    }

    @Nested
    @DisplayName("getCompany()")
    class GetCompany {

        @Test
        @DisplayName("Should return the correct company name")
        void returnsCorrectCompany() {
            // Act
            String company = stock.getCompany();
            // Assert
            assertEquals("Nike, Inc", company);
        }
    }

    @Nested
    @DisplayName("getSalesPrice()")
    class GetSalesPrice {

        @Test
        @DisplayName("Should return the latest price when multiple prices exist")
        void returnsLatestPrice() {
            // Arrange
            Stock multiPriceStock = new Stock("NKE", "Nike, Inc",
                    new ArrayList<>(List.of(new BigDecimal("130.00"), new BigDecimal("150.00"))));
            // Act
            BigDecimal result = multiPriceStock.getSalesPrice();
            // Assert
            assertEquals(new BigDecimal("150.00"), result);
        }
    }

    @Nested
    @DisplayName("addNewSalesPrice()")
    class AddNewSalesPrice {

        @Test
        @DisplayName("Should update the sales price when a new price is added")
        void updatesSalesPrice() {
            // Act
            stock.addNewSalesPrice(new BigDecimal("110.00"));
            // Assert
            assertEquals(new BigDecimal("110.00"), stock.getSalesPrice());
        }
    }
}