package edu.ntnu.idatt2003.millions.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Player} class.
 * <p>
 *   This test class verifies the behaviour of the Player model, including
 *   construction, money management, and associated portfolio and archive.
 * </p>
 * <p>
 *   All tests follow the AAA pattern.
 * </p>
 */
class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player("Alva", new BigDecimal("1000.00"));
    }

    @Nested
    @DisplayName("Player()")
    class Constructor {

        @Test
        @DisplayName("Should return correct name when valid player created")
        void returnsCorrectName() {
            // Act
            String name = player.getName();
            // Assert
            assertEquals("Alice", name);
        }

        @Test
        @DisplayName("Should return correct starting money when valid player created")
        void returnsCorrectStartingMoney() {
            // Act
            BigDecimal startingMoney = player.getStartingMoney();
            // Assert
            assertEquals(new BigDecimal("1000.00"), startingMoney);
        }

        @Test
        @DisplayName("Should set current balance equal to starting money")
        void setsMoneyEqualToStartingMoney() {
            // Act
            BigDecimal money = player.getMoney();
            // Assert
            assertEquals(new BigDecimal("1000.00"), money);
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void throwsExceptionWhenNameIsNull() {
            // Arrange
            BigDecimal startingMoney = new BigDecimal("1000.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player(null, startingMoney));
        }

        @Test
        @DisplayName("Should throw exception when name is blank")
        void throwsExceptionWhenNameIsBlank() {
            // Arrange
            BigDecimal startingMoney = new BigDecimal("1000.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("", startingMoney));
        }

        @Test
        @DisplayName("Should throw exception when starting money is null")
        void throwsExceptionWhenStartingMoneyIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("Alice", null));
        }

        @Test
        @DisplayName("Should throw exception when starting money is negative")
        void throwsExceptionWhenStartingMoneyIsNegative() {
            // Arrange
            BigDecimal negativeMoney = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("Alice", negativeMoney));
        }

        @Test
        @DisplayName("Should initialize with an empty portfolio")
        void initializesWithEmptyPortfolio() {
            // Act & Assert
            assertTrue(player.getPortfolio().getShares().isEmpty());
        }

        @Test
        @DisplayName("Should initialize with an empty transaction archive")
        void initializesWithEmptyTransactionArchive() {
            // Act & Assert
            assertTrue(player.getTransactionArchive().isEmpty());
        }
    }

    @Nested
    @DisplayName("addMoney()")
    class AddMoney {

        @Test
        @DisplayName("Should increase balance by the given amount")
        void increasesBalance() {
            // Arrange
            BigDecimal amount = new BigDecimal("500.00");
            // Act
            player.addMoney(amount);
            // Assert
            assertEquals(new BigDecimal("1500.00"), player.getMoney());
        }

        @Test
        @DisplayName("Should throw exception when amount is negative")
        void throwsExceptionWhenAmountIsNegative() {
            // Arrange
            BigDecimal negativeAmount = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.addMoney(negativeAmount));
        }
    }

    @Nested
    @DisplayName("withdrawMoney()")
    class WithdrawMoney {

        @Test
        @DisplayName("Should decrease balance by the given amount")
        void decreasesBalance() {
            // Arrange
            BigDecimal amount = new BigDecimal("500.00");
            // Act
            player.withdrawMoney(amount);
            // Assert
            assertEquals(new BigDecimal("500.00"), player.getMoney());
        }

        @Test
        @DisplayName("Should allow withdrawing entire balance")
        void allowsWithdrawingEntireBalance() {
            // Act
            player.withdrawMoney(new BigDecimal("1000.00"));
            // Assert
            assertEquals(BigDecimal.ZERO, player.getMoney());
        }

        @Test
        @DisplayName("Should throw exception when withdrawing more than current balance")
        void throwsExceptionWhenInsufficientFunds() {
            // Arrange
            BigDecimal amount = new BigDecimal("2000.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.withdrawMoney(amount));
        }

        @Test
        @DisplayName("Should throw exception when withdrawing a negative amount")
        void throwsExceptionWhenAmountIsNegative() {
            // Arrange
            BigDecimal negativeAmount = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    player.withdrawMoney(negativeAmount));
        }
    }
}