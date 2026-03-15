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
 * Unit tests for the {@link Purchase} class.
 * <p>
 * This test class verifies the behaviour of the Purchase model,
 * including construction and committing a purchase transaction.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class PurchaseTest {

    private Player player;
    private Share share;

    @BeforeEach
    void setUp() {
        player = new Player("Alva", new BigDecimal("10000.00"));
        share = new Share(
                new Stock("DIS", "The Walt Disney Company",
                        new ArrayList<>(List.of(new BigDecimal("100.00")))),
                new BigDecimal("10"), new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("Purchase()")
    class Constructor {

        @Test
        @DisplayName("Should return correct share when valid purchase created")
        void returnsCorrectShare() {
            // Arrange
            Purchase purchase = new Purchase(share, 1);
            // Act & Assert
            assertEquals(share, purchase.getShare());
        }

        @Test
        @DisplayName("Should return correct week when valid purchase created")
        void returnsCorrectWeek() {
            // Arrange
            Purchase purchase = new Purchase(share, 1);
            // Act & Assert
            assertEquals(1, purchase.getWeek());
        }

        @Test
        @DisplayName("Should not be committed when created")
        void isNotCommittedWhenCreated() {
            // Arrange
            Purchase purchase = new Purchase(share, 1);
            // Act & Assert
            assertFalse(purchase.isCommitted());
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Purchase(null, 1));
        }

        @Test
        @DisplayName("Should throw exception when week is zero")
        void throwsExceptionWhenWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Purchase(share, 0));
        }

        @Test
        @DisplayName("Should throw exception when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Purchase(share, -1));
        }

        @Test
        @DisplayName("Should throw exception when week is greater than 52")
        void throwsExceptionWhenWeekIsGreaterThan52() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Purchase(share, 53));
        }
    }

    @Nested
    @DisplayName("commit()")
    class Commit {

        private Purchase purchase;

        @BeforeEach
        void setUp() {
            // Arrange
            purchase = new Purchase(share, 1);
        }

        @Test
        @DisplayName("Should throw exception when player is null")
        void throwsExceptionWhenPlayerIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    purchase.commit(null));
        }

        @Test
        @DisplayName("Should mark transaction as committed after commit")
        void marksTransactionAsCommitted() {
            // Act
            purchase.commit(player);
            // Assert
            assertTrue(purchase.isCommitted());
        }

        @Test
        @DisplayName("Should add share to player portfolio after commit")
        void addsShareToPortfolio() {
            // Act
            purchase.commit(player);
            // Assert
            assertTrue(player.getPortfolio().contains(share));
        }

        @Test
        @DisplayName("Should deduct total cost from player balance after commit")
        void deductsTotalCostFromBalance() {
            // Arrange
            BigDecimal expectedBalance = new BigDecimal("10000.00")
                    .subtract(purchase.getCalculator().calculateTotal());
            // Act
            purchase.commit(player);
            // Assert
            assertEquals(0, expectedBalance.compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("Should add transaction to archive after commit")
        void addsTransactionToArchive() {
            // Act
            purchase.commit(player);
            // Assert
            assertFalse(player.getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when transaction is already committed")
        void throwsExceptionWhenAlreadyCommitted() {
            // Arrange
            purchase.commit(player);
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    purchase.commit(player));
        }

        @Test
        @DisplayName("Should throw exception when player has insufficient funds")
        void throwsExceptionWhenInsufficientFunds() {
            // Arrange
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    purchase.commit(poorPlayer));
        }

        @Test
        @DisplayName("Should not add share to portfolio when player has insufficient funds")
        void doesNotAddShareToPortfolioWhenInsufficientFunds() {
            // Arrange
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            // Act
            try {
                purchase.commit(poorPlayer);
            } catch (IllegalArgumentException _) {
                // for test purposes
            }
            // Assert
            assertFalse(poorPlayer.getPortfolio().contains(share));
        }

        @Test
        @DisplayName("Should not change player balance when player has insufficient funds")
        void doesNotChangeBalanceWhenInsufficientFunds() {
            // Arrange
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            BigDecimal balanceBefore = poorPlayer.getMoney();
            // Act
            try {
                purchase.commit(poorPlayer);
            } catch (IllegalArgumentException _) {
                // for test purposes
            }
            // Assert
            assertEquals(0, balanceBefore.compareTo(poorPlayer.getMoney()));
        }

        @Test
        @DisplayName("Should not add transaction to archive when player has insufficient funds")
        void doesNotAddTransactionToArchiveWhenInsufficientFunds() {
            // Arrange
            Player poorPlayer = new Player("Bob", new BigDecimal("1.00"));
            // Act
            try {
                purchase.commit(poorPlayer);
            } catch (IllegalArgumentException _) {
                // for test purposes
            }
            // Assert
            assertTrue(poorPlayer.getTransactionArchive().isEmpty());
        }
    }
}