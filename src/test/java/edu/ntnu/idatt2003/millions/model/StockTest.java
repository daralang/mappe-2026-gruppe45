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
 * Unit tests for the {@link Stock} class.
 * <p>
 * This test class verifies that a stock is correctly constructed
 * and that its methods return the expected values.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
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
    @DisplayName("Stock()")
    class Constructor {

        private final List<BigDecimal> prices = new ArrayList<>(List.of(new BigDecimal("100.00")));

        @Test
        @DisplayName("Should throw exception when symbol is null")
        void throwsExceptionWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock(null, "Nike, Inc", prices));
        }

        @Test
        @DisplayName("Should throw exception when symbol is blank")
        void throwsExceptionWhenSymbolIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock("", "Nike, Inc", prices));
        }

        @Test
        @DisplayName("Should throw exception when company is null")
        void throwsExceptionWhenCompanyIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock("NKE", null, prices));
        }

        @Test
        @DisplayName("Should throw exception when company is blank")
        void throwsExceptionWhenCompanyIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock("NKE", "", prices));
        }

        @Test
        @DisplayName("Should throw exception when price list is empty")
        void throwsExceptionWhenPriceListIsEmpty() {
            // Arrange
            List<BigDecimal> emptyList = new ArrayList<>();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock("NKE", "Nike, Inc", emptyList));
        }

        @Test
        @DisplayName("Should throw exception when price list is null")
        void throwsExceptionWhenPriceListIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Stock("NKE", "Nike, Inc", null));
        }
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

        @Test
        @DisplayName("Should throw exception when price is null")
        void throwsExceptionWhenPriceIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    stock.addNewSalesPrice(null));
        }

        @Test
        @DisplayName("Should throw exception when price is negative")
        void throwsExceptionWhenPriceIsNegative() {
            // Arrange
            BigDecimal negativePrice = new BigDecimal("-10.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    stock.addNewSalesPrice(negativePrice));
        }
    }
}