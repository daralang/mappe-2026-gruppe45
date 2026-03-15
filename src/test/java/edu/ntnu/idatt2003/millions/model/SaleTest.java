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
 * Unit tests for the {@link Sale} class.
 * <p>
 * This test class verifies the behaviour of the Sale model,
 * including construction and committing a sale transaction.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class SaleTest {

    private Player player;
    private Share share;

    @BeforeEach
    void setUp() {
        player = new Player("Alva", new BigDecimal("10000.00"));
        share = new Share(
                new Stock("DIS", "The Walt Disney Company",
                        new ArrayList<>(List.of(new BigDecimal("100.00")))),
                new BigDecimal("10"), new BigDecimal("50.00"));
        player.getPortfolio().addShare(share);
    }

    @Nested
    @DisplayName("Sale()")
    class Constructor {

        @Test
        @DisplayName("Should return correct share when valid sale created")
        void returnsCorrectShare() {
            // Arrange
            Sale sale = new Sale(share, 1);
            // Act & Assert
            assertEquals(share, sale.getShare());
        }

        @Test
        @DisplayName("Should return correct week when valid sale created")
        void returnsCorrectWeek() {
            // Arrange
            Sale sale = new Sale(share, 1);
            // Act & Assert
            assertEquals(1, sale.getWeek());
        }

        @Test
        @DisplayName("Should not be committed when created")
        void isNotCommittedWhenCreated() {
            // Arrange
            Sale sale = new Sale(share, 1);
            // Act & Assert
            assertFalse(sale.isCommitted());
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Sale(null, 1));
        }

        @Test
        @DisplayName("Should throw exception when week is zero")
        void throwsExceptionWhenWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Sale(share, 0));
        }

        @Test
        @DisplayName("Should throw exception when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Sale(share, -1));
        }

        @Test
        @DisplayName("Should throw exception when week is greater than 52")
        void throwsExceptionWhenWeekIsGreaterThan52() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Sale(share, 53));
        }
    }

    @Nested
    @DisplayName("commit()")
    class Commit {

        private Sale sale;

        // Arrange-helpers for WhenShareNotInPortfolio-tests
        private final Share otherShare = new Share(
                new Stock("NKE", "Nike, Inc",
                        new ArrayList<>(List.of(new BigDecimal("100.00")))),
                new BigDecimal("10"), new BigDecimal("50.00"));
        private final Sale otherSale = new Sale(otherShare, 1);

        @BeforeEach
        void setUp() {
            sale = new Sale(share, 1);
        }

        @Test
        @DisplayName("Should throw exception when player is null")
        void throwsExceptionWhenPlayerIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    sale.commit(null));
        }

        @Test
        @DisplayName("Should mark transaction as committed after commit")
        void marksTransactionAsCommitted() {
            // Act
            sale.commit(player);
            // Assert
            assertTrue(sale.isCommitted());
        }

        @Test
        @DisplayName("Should remove share from player portfolio after commit")
        void removesShareFromPortfolio() {
            // Act
            sale.commit(player);
            // Assert
            assertFalse(player.getPortfolio().contains(share));
        }

        @Test
        @DisplayName("Should add total payout to player balance after commit")
        void addsTotalPayoutToBalance() {
            // Arrange
            BigDecimal expectedBalance = new BigDecimal("10000.00")
                    .add(sale.getCalculator().calculateTotal());
            // Act
            sale.commit(player);
            // Assert
            assertEquals(0, expectedBalance.compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("Should add transaction to archive after commit")
        void addsTransactionToArchive() {
            // Act
            sale.commit(player);
            // Assert
            assertFalse(player.getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when transaction is already committed")
        void throwsExceptionWhenAlreadyCommitted() {
            // Arrange
            sale.commit(player);
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    sale.commit(player));
        }

        @Test
        @DisplayName("Should throw exception when share is not in player portfolio")
        void throwsExceptionWhenShareNotInPortfolio() {
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    otherSale.commit(player));
        }

        @Test
        @DisplayName("Should not change portfolio when share is not in portfolio")
        void doesNotChangePortfolioWhenShareNotInPortfolio() {
            // Arrange
            int portfolioSizeBefore = player.getPortfolio().getShares().size();
            // Act
            try {
                otherSale.commit(player);
            } catch (IllegalStateException _) {
                // for test purposes
            }
            // Assert
            assertEquals(portfolioSizeBefore, player.getPortfolio().getShares().size());
        }

        @Test
        @DisplayName("Should not change player balance when share is not in portfolio")
        void doesNotChangeBalanceWhenShareNotInPortfolio() {
            // Arrange
            BigDecimal balanceBefore = player.getMoney();
            // Act
            try {
                otherSale.commit(player);
            } catch (IllegalStateException _) {
                // for test purposes
            }
            // Assert
            assertEquals(0, balanceBefore.compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("Should not add transaction to archive when share is not in portfolio")
        void doesNotAddTransactionToArchiveWhenShareNotInPortfolio() {
            // Act
            try {
                otherSale.commit(player);
            } catch (IllegalStateException _) {
                // for test purposes
            }
            // Assert
            assertTrue(player.getTransactionArchive().isEmpty());
        }
    }
}
