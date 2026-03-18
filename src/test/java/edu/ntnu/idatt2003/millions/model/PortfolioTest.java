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
 * Unit tests for the {@link Portfolio} class.
 * <p>
 * This test class verifies the behaviour of the Portfolio model, including adding, removing,
 * retrieving and filtering shares.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class PortfolioTest {
    private Portfolio portfolio;
    private Share share;

    @BeforeEach
    void setUp() {
        // Arrange
        portfolio = new Portfolio();
        share = new Share(
                new Stock("DIS", "The Walt Disney Company", new ArrayList<>(List.of(new BigDecimal("100")))),
                new BigDecimal("10"), new BigDecimal("50"));
    }

    @Nested
    @DisplayName("addShare()")
    class AddShare {

        @Test
        @DisplayName("Should return true when share does not already exist in portfolio")
        void returnsTrueWhenShareIsNew() {
            // Act
            boolean actual = portfolio.addShare(share);
            // Assert
            assertTrue(actual);
            assertTrue(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share already exists in portfolio")
        void returnsFalseWhenShareAlreadyExists() {
            // Arrange
            portfolio.addShare(share);
            // Act
            boolean actual = portfolio.addShare(share);
            // Assert
            assertFalse(actual);
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.addShare(null));
        }
    }

    @Nested
    @DisplayName("removeShare()")
    class RemoveShare {

        @Test
        @DisplayName("Should return true when share exists in portfolio and is successfully removed")
        void returnsTrueWhenShareIsRemoved() {
            // Arrange
            portfolio.addShare(share);
            // Act
            boolean actual = portfolio.removeShare(share);
            // Assert
            assertTrue(actual);
            assertFalse(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share does not exist in portfolio")
        void returnsFalseWhenShareDoesNotExist() {
            // Act
            boolean actual = portfolio.removeShare(share);
            // Assert
            assertFalse(actual);
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.removeShare(null));
        }
    }

    @Nested
    @DisplayName("getShares()")
    class GetShares {

        @Test
        @DisplayName("Should return a copy of the share list")
        void returnsCopyOfShareList() {
            // Arrange
            portfolio.addShare(share);
            // Act
            List<Share> shares = portfolio.getShares();
            shares.clear();
            // Assert
            assertEquals(1, portfolio.getShares().size());
        }

        @Test
        @DisplayName("Should only return shares matching the given symbol")
        void returnsOnlyMatchingSymbols() {
            // Arrange
            Share nike = new Share(
                    new Stock("NKE", "NIKE, Inc", new ArrayList<>(List.of(new BigDecimal("100")))),
                    new BigDecimal("10"), new BigDecimal("50"));
            portfolio.addShare(share);
            portfolio.addShare(nike);
            // Act
            List<Share> result = portfolio.getShares("DIS");
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(share));
            assertFalse(result.contains(nike));
        }

        @Test
        @DisplayName("Should return empty list when portfolio is empty")
        void returnsEmptyListWhenPortfolioIsEmpty() {
            // Act & Assert
            assertTrue(portfolio.getShares().isEmpty());
        }

        @Test
        @DisplayName("Should return empty list when no shares match the given symbol")
        void returnsEmptyListWhenNoSymbolMatches() {
            // Arrange
            portfolio.addShare(share);
            // Act
            List<Share> result = portfolio.getShares("NKE");
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when null is passed as symbol")
        void throwsExceptionWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.getShares(null));
        }
    }

    @Nested
    @DisplayName("contains()")
    class Contains {

        @Test
        @DisplayName("Should return true when share exists in portfolio")
        void returnsTrueWhenShareExists() {
            // Arrange
            portfolio.addShare(share);
            // Act & Assert
            assertTrue(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should return false when share does not exist in portfolio")
        void returnsFalseWhenShareDoesNotExist() {
            // Act & Assert
            assertFalse(portfolio.contains(share));
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    portfolio.contains(null));
        }
    }

    @Nested
    @DisplayName("getNetWorth()")
    class GetNetWorth {

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void returnsZeroWhenPortfolioIsEmpty() {
            // Act & Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(portfolio.getNetWorth()));
        }

        @Test
        @DisplayName("Should return total sale value of all shares in portfolio")
        void returnsTotalSalesValueOfAllShares() {
            //Arrange
            portfolio.addShare(share);
            BigDecimal expected = new SalesCalculator(share).calculateTotal();
            // Act @ Assert
            assertEquals(0, expected.compareTo(portfolio.getNetWorth()));
        }

        @Test
        @DisplayName("Should sum sale value of multiple shares in portfolio")
        void sumsValueOfMultipleShares() {
            //Arrange
            Share share2 = new Share(
                    new Stock("DCL", "Dara, Inc", new ArrayList<>(List.of(new BigDecimal("50.00")))),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            portfolio.addShare(share);
            portfolio.addShare(share2);
            BigDecimal expected = new SalesCalculator(share).calculateTotal()
                    .add(new SalesCalculator(share2).calculateTotal());
            // Act & Assert
            assertEquals(0, expected.compareTo(portfolio.getNetWorth()));
        }
    }
}