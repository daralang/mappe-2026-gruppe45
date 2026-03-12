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
 * Unit tests for the {@link Portfolio} class.
 * <p>
 *   This test class verifies the behaviour of the Portfolio model, including adding, removing,
 *   retrieving and filtering shares.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
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
    }
}