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
 * Unit tests for the {@link Exchange} class.
 * <p>
 * This test class verifies the behaviour of the Exchange model, including
 * construction, buying and selling shares, advancing weeks, and stock lookup.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class ExchangeTest {

    private Exchange exchange;
    private Player player;
    private Stock stock;

    @BeforeEach
    void setUp() {
        stock = new Stock("DIS", "The Walt Disney Company",
                new ArrayList<>(List.of(new BigDecimal("100.00"))));
        exchange = new Exchange("NYSE", new ArrayList<>(List.of(stock)));
        player = new Player("Alva", new BigDecimal("10000.00"));
    }

    @Nested
    @DisplayName("Exchange()")
    class Constructor {

        @Test
        @DisplayName("Should return correct name when valid exchange created")
        void returnsCorrectName() {
            // Act & Assert
            assertEquals("NYSE", exchange.getName());
        }

        @Test
        @DisplayName("Should start at week 1 when created")
        void startsAtWeekOne() {
            // Act & Assert
            assertEquals(1, exchange.getWeek());
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            // Arrange
            List<Stock> stocks = new ArrayList<>(List.of(stock));
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Exchange(null, stocks));
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        void throwsExceptionWhenNameIsBlank() {
            // Arrange
            List<Stock> stocks = new ArrayList<>(List.of(stock));
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Exchange("", stocks));
        }

        @Test
        @DisplayName("Should throw exception when stock list is null")
        void throwsExceptionWhenStockListIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Exchange("NYSE", null));
        }

        @Test
        @DisplayName("Should throw exception when stock list is empty")
        void throwsExceptionWhenStockListIsEmpty() {
            // Arrange
            List<Stock> emptyList = new ArrayList<>();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Exchange("NYSE", emptyList));
        }

        @Test
        @DisplayName("Should throw exception when stock list contains null")
        void throwsExceptionWhenStockListContainsNull() {
            // Arrange
            List<Stock> stocks = new ArrayList<>();
            stocks.add(null);
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Exchange("NYSE", stocks));
        }

        @Test
        @DisplayName("Should throw exception when stock list contains duplicate symbols")
        void throwsExceptionWhenStockListContainsDuplicateSymbols() {
            // Arrange
            Stock duplicate = new Stock("DIS", "Disney Copy",
                    new ArrayList<>(List.of(new BigDecimal("100.00"))));
            List<Stock> stocks = new ArrayList<>(List.of(stock, duplicate));
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Exchange("NYSE", stocks));
        }
    }

    @Nested
    @DisplayName("getStock()")
    class GetStock {

        @Test
        @DisplayName("Should return correct stock when valid symbol given")
        void returnsCorrectStock() {
            // Act & Assert
            assertEquals(stock, exchange.getStock("DIS"));
        }

        @Test
        @DisplayName("Should throw exception when symbol is null")
        void throwsExceptionWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.getStock(null));
        }

        @Test
        @DisplayName("Should throw exception when symbol is blank")
        void throwsExceptionWhenSymbolIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.getStock(""));
        }

        @Test
        @DisplayName("Should throw exception when symbol is not found")
        void throwsExceptionWhenSymbolNotFound() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.getStock("NKE"));
        }
    }

    @Nested
    @DisplayName("hasStock()")
    class HasStock {

        @Test
        @DisplayName("Should return true when stock exists")
        void returnsTrueWhenStockExists() {
            // Act & Assert
            assertTrue(exchange.hasStock("DIS"));
        }

        @Test
        @DisplayName("Should return false when stock does not exist")
        void returnsFalseWhenStockDoesNotExist() {
            // Act & Assert
            assertFalse(exchange.hasStock("NKE"));
        }

        @Test
        @DisplayName("Should return false when symbol is null")
        void returnsFalseWhenSymbolIsNull() {
            // Act & Assert
            assertFalse(exchange.hasStock(null));
        }

        @Test
        @DisplayName("Should return false when symbol is blank")
        void returnsFalseWhenSymbolIsBlank() {
            // Act & Assert
            assertFalse(exchange.hasStock(""));
        }
    }

    @Nested
    @DisplayName("findStocks()")
    class FindStocks {

        @Test
        @DisplayName("Should return matching stocks when search term matches symbol")
        void returnsMatchingStocksForSymbol() {
            // Act
            List<Stock> result = exchange.findStocks("DIS");
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(stock));
        }

        @Test
        @DisplayName("Should return matching stocks when search term matches company name")
        void returnsMatchingStocksForCompanyName() {
            // Act
            List<Stock> result = exchange.findStocks("Disney");
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(stock));
        }

        @Test
        @DisplayName("Should return empty list when no stocks match search term")
        void returnsEmptyListWhenNoMatch() {
            // Act
            List<Stock> result = exchange.findStocks("Nike");
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should be case insensitive")
        void isCaseInsensitive() {
            // Act
            List<Stock> result = exchange.findStocks("dis");
            // Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should throw exception when search term is null")
        void throwsExceptionWhenSearchTermIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.findStocks(null));
        }

        @Test
        @DisplayName("Should throw exception when search term is blank")
        void throwsExceptionWhenSearchTermIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.findStocks(""));
        }
    }

    @Nested
    @DisplayName("buy()")
    class Buy {

        @Test
        @DisplayName("Should return committed purchase transaction after buy")
        void returnsCommittedPurchase() {
            // Act
            Transaction transaction = exchange.buy("DIS", new BigDecimal("5"), player);
            // Assert
            assertTrue(transaction.isCommitted());
            assertInstanceOf(Purchase.class, transaction);
        }

        @Test
        @DisplayName("Should add share to player portfolio after buy")
        void addsShareToPortfolio() {
            // Act
            exchange.buy("DIS", new BigDecimal("5"), player);
            // Assert
            assertFalse(player.getPortfolio().getShares().isEmpty());
        }

        @Test
        @DisplayName("Should deduct cost from player balance after buy")
        void deductsCostFromBalance() {
            // Arrange
            BigDecimal balanceBefore = player.getMoney();
            // Act
            exchange.buy("DIS", new BigDecimal("5"), player);
            // Assert
            assertTrue(player.getMoney().compareTo(balanceBefore) < 0);
        }

        @Test
        @DisplayName("Should throw exception when symbol is null")
        void throwsExceptionWhenSymbolIsNull() {
            // Arrange
            BigDecimal quantity = new BigDecimal("5");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.buy(null, quantity, player));
        }

        @Test
        @DisplayName("Should throw exception when quantity is null")
        void throwsExceptionWhenQuantityIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.buy("DIS", null, player));
        }

        @Test
        @DisplayName("Should throw exception when quantity is zero")
        void throwsExceptionWhenQuantityIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.buy("DIS", BigDecimal.ZERO, player));
        }

        @Test
        @DisplayName("Should throw exception when quantity is negative")
        void throwsExceptionWhenQuantityIsNegative() {
            // Arrange
            BigDecimal negativeQuantity = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.buy("DIS", negativeQuantity, player));
        }

        @Test
        @DisplayName("Should throw exception when player is null")
        void throwsExceptionWhenPlayerIsNull() {
            // Arrange
            BigDecimal quantity = new BigDecimal("5");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.buy("DIS", quantity, null));
        }

        @Test
        @DisplayName("Should throw exception when player has insufficient funds")
        void throwsExceptionWhenInsufficientFunds() {
            // Arrange
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            BigDecimal quantity = new BigDecimal("5");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    exchange.buy("DIS", quantity, poorPlayer));
        }
    }

    @Nested
    @DisplayName("sell()")
    class Sell {

        private Share share;

        @BeforeEach
        void setUp() {
            share = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
            player.getPortfolio().addShare(share);
        }

        @Test
        @DisplayName("Should return committed sale transaction after sell")
        void returnsCommittedSale() {
            // Act
            Transaction transaction = exchange.sell(share, player);
            // Assert
            assertTrue(transaction.isCommitted());
            assertInstanceOf(Sale.class, transaction);
        }

        @Test
        @DisplayName("Should remove share from player portfolio after sell")
        void removesShareFromPortfolio() {
            // Act
            exchange.sell(share, player);
            // Assert
            assertFalse(player.getPortfolio().contains(share));
        }

        @Test
        @DisplayName("Should add payout to player balance after sell")
        void addsPayoutToBalance() {
            // Arrange
            BigDecimal balanceBefore = player.getMoney();
            // Act
            exchange.sell(share, player);
            // Assert
            assertTrue(player.getMoney().compareTo(balanceBefore) > 0);
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.sell(null, player));
        }

        @Test
        @DisplayName("Should throw exception when player is null")
        void throwsExceptionWhenPlayerIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    exchange.sell(share, null));
        }

        @Test
        @DisplayName("Should throw exception when share is not in player portfolio")
        void throwsExceptionWhenShareNotInPortfolio() {
            // Arrange
            Share otherShare = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    exchange.sell(otherShare, player));
        }
    }

    @Nested
    @DisplayName("advance()")
    class Advance {

        @Test
        @DisplayName("Should increment week by one when advance is called")
        void incrementsWeek() {
            // Act
            exchange.advance();
            // Assert
            assertEquals(2, exchange.getWeek());
        }

        @Test
        @DisplayName("Should update stock price when advance is called")
        void updatesStockPrice() {
            // Arrange
            BigDecimal priceBefore = stock.getSalesPrice();
            // Act
            exchange.advance();
            // Assert
            assertNotEquals(priceBefore, stock.getSalesPrice());
        }

        @Test
        @DisplayName("Should increment week correctly after multiple advances")
        void incrementsWeekCorrectlyAfterMultipleAdvances() {
            // Act
            exchange.advance();
            exchange.advance();
            exchange.advance();
            // Assert
            assertEquals(4, exchange.getWeek());
        }
    }

    @Nested
    @DisplayName("getLosers()")
    class GetLosers {

        private Stock gainer;
        private Stock loser;

        @BeforeEach
        void setUp() {
            gainer = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("100.00"), new BigDecimal("120.00"))));
            loser = new Stock("AKL", "Alva Company",
                    new ArrayList<>(List.of(new BigDecimal("100.00"), new BigDecimal("80.00"))));
            exchange = new Exchange("Oslo Børs", new ArrayList<>(List.of(gainer, loser)));
        }

        @Test
        @DisplayName("Should return stocks with negative price change")
        void returnsStocksWithNegativePriceChange() {
            //Act
            List<Stock> result = exchange.getLosers(10);
            //Assert
            assertTrue(result.contains(loser));
            assertFalse(result.contains(gainer));
        }

        @Test
        @DisplayName("Should return list with restrict at limit")
        void returnsListWithRestrictAtLimit() {
            //Act
            List<Stock> result = exchange.getLosers(1);
            //Assert
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return empty list when no stocks have lost")
        void returnsEmptyListWhenNoLoserStocks() {
            // Arrange
            Stock flat = new  Stock("MR", "Majid, Inc",
                    new ArrayList<>(List.of(new BigDecimal("100.00"),
                            new BigDecimal("120.00"))));
            exchange = new Exchange("Stockholm Börs", new ArrayList<>(List.of(flat)));
            // Act & Assert
            assertTrue(exchange.getLosers(20).isEmpty());
        }

        @Test
        @DisplayName("Should throw exceptions when limit is zero")
        void throwsExceptionWhenLimitIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> exchange.getLosers(0));
        }

        @Test
        @DisplayName("Should throw exception when limit is negative")
        void throwsExceptionWhenLimitIsNegative() {
            //Act & Assert
            assertThrows(IllegalArgumentException.class, () -> exchange.getLosers(-1));
        }
    }

    @Nested
    @DisplayName("getGainers()")
    class GetGainers {

        private Stock gainer;
        private Stock loser;

        @BeforeEach
        void setUp() {
            gainer = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("100.00"), new BigDecimal("120.00"))));
            loser = new Stock("AKL", "Alva Company",
                    new ArrayList<>(List.of(new BigDecimal("100.00"), new BigDecimal("80.00"))));
            exchange = new Exchange("Oslo Børs", new ArrayList<>(List.of(gainer, loser)));
        }

        @Test
        @DisplayName("Should return stocks with positive price change")
        void returnsStocksWithPositivePriceChange() {
            //Act
            List<Stock> result = exchange.getGainers(10);
            //Assert
            assertTrue(result.contains(gainer));
            assertFalse(result.contains(loser));
        }

        @Test
        @DisplayName("Should return list restrict at limit")
        void  returnsListWithRestrictAtLimit() {
            //Act
            List<Stock> result = exchange.getGainers(1);
            //Assert
            assertEquals(1, result.size());
        }
        @Test
        @DisplayName("Should return empty list when no stocks have gained")
        void returnsEmptyListWhenNoGainers() {
            //Arrange
            Stock flat = new Stock("MR", "Majid, Inc",
                    new ArrayList<>(List.of(new BigDecimal("100.00"), new BigDecimal("50.00"))));
            exchange = new Exchange("Stockholm Börs", new ArrayList<>(List.of(flat)));
            //Act & Assert
            assertTrue(exchange.getGainers(10).isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when limit is zero")
        void throwsExceptionWhenLimitIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> exchange.getGainers(0));
        }

        @Test
        @DisplayName("Should throw exception when limit is negative")
        void throwsExceptionWhenLimitIsNegative() {
            //Act & Assert
            assertThrows(IllegalArgumentException.class, () -> exchange.getGainers(-1));
        }
    }
}
