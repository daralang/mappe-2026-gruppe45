package edu.ntnu.idatt2003.millions.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
            assertThrows(NullPointerException.class, () ->
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
            assertThrows(NullPointerException.class, () ->
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
            assertThrows(NullPointerException.class, () ->
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
            assertThrows(NullPointerException.class, () ->
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

    @Nested
    @DisplayName("getHistoricalPrices()")
    class GetHistoricalPrices {

        @Test
        @DisplayName("Should return all registered prices")
        void returnsAllRegisteredPrices() {
            //Arrange
            Stock multiPriceStock = new Stock("AKL", "Alva Company",
                    new ArrayList<>(List.of(
                            new BigDecimal("100.00"),
                            new BigDecimal("130.00"),
                            new BigDecimal("160.00"))));
            // Act
            List<BigDecimal> result = multiPriceStock.getHistoricalPrices();
            //Assert
            assertEquals(3, result.size());
            assertTrue(result.contains(new BigDecimal("100.00")));
            assertTrue(result.contains(new BigDecimal("130.00")));
        }

        @Test
        @DisplayName("Should return a copy and not the original list")
        void returnsCopyOfOriginalList() {
            //Act
            List<BigDecimal> result = stock.getHistoricalPrices();
            result.clear();
            //Assert
            assertEquals(1, stock.getHistoricalPrices().size());
        }

        @Test
        @DisplayName("Should not return prices that were never recorded")
        void returnsNotPricesThatWereNeverRecorded() {
            //Act
            List<BigDecimal> result = stock.getHistoricalPrices();
            //Assert
            assertFalse(result.contains(new BigDecimal("99.00")));
        }
    }

    @Nested
    @DisplayName("getHighestPrice()")
    class GetHighestPrice {

        @Test
        @DisplayName("Should return the highest price when multiple prices exist")
        void returnsHighestPrice() {
            //Arrange
            Stock multiPriceStock = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(
                            new BigDecimal("100.00"),
                            new BigDecimal("140.00"),
                            new BigDecimal("120.00"))));
            //Act
            BigDecimal result = multiPriceStock.getHighestPrice();
            //Assert
            assertEquals(0, new BigDecimal("140.00").compareTo(result));
        }

        @Test
        @DisplayName("Should return the only price when one price exists")
        void returnsOnlyPriceInHighest() {
            //Act
            BigDecimal result = stock.getHighestPrice();
            //Assert
            assertEquals(0, new BigDecimal("100.00").compareTo(result));
        }

        @Test
        @DisplayName("Should not return the lowest price as highest")
        void returnNotLowestPriceAsHighest() {
            //Arrange
            Stock multiPriceStock = new Stock("AKL", "Alva Company",
                    new ArrayList<>(List.of(
                            new BigDecimal("100.00"),
                            new BigDecimal("160.00"),
                            new BigDecimal("130.00"))));
            //Act
            BigDecimal result = multiPriceStock.getHighestPrice();
            //Assert
            assertNotEquals(0,new BigDecimal("100.00").compareTo(result));
        }
    }
    @Nested
    @DisplayName("getLowestPrice()")
    class GetLowestPrice {

        @Test
        @DisplayName("Should return the lowest price when multiple prices exists")
        void returnsLowestPrice() {
            //Arrange
            Stock multiPriceStock = new Stock("AKL", "Alva Company",
                    new ArrayList<>(List.of(
                            new BigDecimal("120.00"),
                            new BigDecimal("150.00"),
                            new BigDecimal("160.00"))));
            //Act
            BigDecimal result = multiPriceStock.getLowestPrice();
            //Assert
            assertEquals(0, new BigDecimal("120.00").compareTo(result));
        }

        @Test
        @DisplayName("Should return the only price when one price exists")
        void returnsOnlyPriceInLowest() {
            //Act
            BigDecimal result = stock.getLowestPrice();
            // Assert
            assertEquals(0, new BigDecimal("100.00").compareTo(result));
        }
    }
    @Nested
    @DisplayName("getLatestPriceChange()")
    class GetLatestPriceChange {

        @Test
        @DisplayName("Should return positive change when the latest price is higher")
        void returnsPositiveChangeWhenPriceIsHigher() {
            //Arrange
            stock.addNewSalesPrice(new BigDecimal("130.00"));
            // Act
            BigDecimal result = stock.getLatestPriceChange();
            //Assert
            assertEquals(new BigDecimal("30.00"), result);
        }

        @Test
        @DisplayName("Should return negative change when the latest price is lower")
        void returnsNegativeChangeWhenPriceIsLower() {
            //Arrange
            stock.addNewSalesPrice(new BigDecimal("90.00"));
            //Act
            BigDecimal result = stock.getLatestPriceChange();
            //Assert
            assertEquals(0, new BigDecimal("-10.00").compareTo(result));
        }

        @Test
        @DisplayName("Should return zero when only one price exists")
        void returnsZeroWhenOnlyOnePriceExists() {
            // Act
            BigDecimal result = stock.getLatestPriceChange();
            //Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(result));
        }

        @Test
        @DisplayName("Should return zero when latest and previous price are equal")
        void returnsZeroWhenPriceAreEqual() {
            // Arrange
            stock.addNewSalesPrice(new BigDecimal("100.00"));
            //Act
            BigDecimal result = stock.getLatestPriceChange();
            //Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(result));
        }
    }
}