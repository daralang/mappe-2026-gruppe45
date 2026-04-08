package edu.ntnu.idatt2003.millions.model;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Player} class.
 * <p>
 * This test class verifies the behaviour of the Player model, including
 * construction, money management, and associated portfolio and archive.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
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
            assertEquals("Alva", name);
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
            assertThrows(NullPointerException.class, () ->
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
            assertThrows(NullPointerException.class, () ->
                    new Player("Alva", null));
        }

        @Test
        @DisplayName("Should throw exception when starting money is negative")
        void throwsExceptionWhenStartingMoneyIsNegative() {
            // Arrange
            BigDecimal negativeMoney = new BigDecimal("-1");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new Player("Alva", negativeMoney));
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
            assertEquals(0, BigDecimal.ZERO.compareTo(player.getMoney()));
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

    @Nested
    @DisplayName("getNetWorth()")
    class GetNetWorth {

        @Test
        @DisplayName("Should return balance only when portfolio is empty")
        void returnsMoneyWhenPortfolioIsEmpty() {
            // Act & Assert
            assertEquals(0, player.getMoney().compareTo(player.getNetWorth()));
        }

        @Test
        @DisplayName("Should return balance plus portfolio value when portfolio has shares")
        void returnsMoneyAndPortfolioValue() {
            //Arrange
            Stock stock = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("1000.00"))));
            Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            BigDecimal expected = player.getMoney()
                    .add(player.getPortfolio().getNetWorth());
            //Act & Assert
            assertEquals(0, expected.compareTo(player.getNetWorth()));
        }

        @Test
        @DisplayName("Should return correct net worth after money is withdrawn")
        void returnsCorrectNetWorthAfterMoneyIsWithdrawn() {
            //Arrange
            player.withdrawMoney(new BigDecimal("500.00"));
            BigDecimal expected = player.getMoney().add(player.getPortfolio().getNetWorth());
            // Act & Assert
            assertEquals(0, expected.compareTo(player.getNetWorth()));
        }

        @Test
        @DisplayName("Should return correct net worth after share is removed from portfolio")
        void returnsCorrectNetWorthAfterShareRemoved() {
            //Arrange
            Stock stock = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("1000.00"))));
            Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            player.getPortfolio().removeShare(share);
            // Act & Assert
            assertEquals(0, player.getMoney().compareTo(player.getNetWorth()));
        }

        @Test
        @DisplayName("Should not retunr a value less than current money balance")
        void returnsNotValueLessBalance() {
            //Arrange
            Stock stock = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("1000.00"))));
            Share share = new Share(stock, new BigDecimal("10"), new BigDecimal("700.00"));
            player.getPortfolio().addShare(share);
            //Act
            BigDecimal result = player.getNetWorth();
            //Assert
            assertTrue(result.compareTo(player.getMoney()) >= 0);
        }
    }

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("Should return NOVICE when player has just started")
        void returnsNoviceStatusWhenPlayerHasJustStarted() {
            //Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus());
        }

        @Test
        @DisplayName("Should return NOVICE when player has traded less than 10 weeks")
        void returnsNoviceStatusWhenLessThan10Weeks() {
            //Arrange
            Stock stock = new Stock("DCL", "Dara, Inc",
                    new ArrayList<>(List.of(new BigDecimal("1000.00"))));
            Share share = new Share(stock, new BigDecimal("8"), new BigDecimal("100.00"));
            Purchase purchase = new Purchase(share, 1);
            purchase.commit(player);
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus());
            // Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus());
        }

        @Test
        @DisplayName("Should return NOVICE when player has traded 10 weeks but not increased net " +
                "worth by 20%")
        void returnNoviceStatusWhenEnoughWeeksNotEnoughGrowth() {
            // Arrange
            player = new Player("AKL", new BigDecimal("800.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = new Stock("MR" + week, "Majid Company" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            //Not enough net worth growth
            player.addMoney(new BigDecimal("50.00"));
            //Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, player.getStatus());
        }

        @Test
        @DisplayName("Should return INVESTOR when player has traded 10 weeks " +
                "and increased their net worth by 20%")
        void returnsInvestorWhenConditionsMet() {
            //Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = new Stock("MR" + week, "Majid Company" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            // Act & Assert
            assertEquals(PlayerStatusLevel.INVESTOR, player.getStatus());
        }

        @Test
        @DisplayName("Should not return INVESTOR when player has traded 10 weeks but not " +
                "increased their net worth by 20%")
        void returnsNotInvestorWhenNotEnoughGrowth() {
            //Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = new Stock("DCL", "Dara, Inc",
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("50.00"));
            //Act & Assert
            assertNotEquals(PlayerStatusLevel.INVESTOR, player.getStatus());
        }

        @Test
        @DisplayName("Should not return INVESTOR when player has increased net worth by 20% " +
                "but traded less than 10 weeks")
        void returnsNotInvestorWhenEnoughGrowthNotWeeks() {
            //Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 5; week++) {
                Stock stock = new Stock("MR" + week, "Majid Company" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            // Act & Assert
            assertNotEquals(PlayerStatusLevel.INVESTOR, player.getStatus());
        }

        @Test
        @DisplayName("Should return SPECULATOR when player has traded 20 weeks and doubled" +
                " net worth")
        void returnsSpeculatorWhenConditionsMet() {
            //Arrange
            player = new Player("DCL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 20; week++) {
                Stock stock = new Stock("MR" + week, "Majid Company" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("2000.00"));
            //Act & Arrange
            assertEquals(PlayerStatusLevel.SPECULATOR, player.getStatus());
        }

        @Test
        @DisplayName("Should not return SPECULATOR when player has traded 20 weeks " +
                "but not doubled their net worth")
        void returnsNotSpeculatorWhenNotEnoughGrowth() {
            //Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 20; week++) {
                Stock stock = new Stock("DCL" + week, "Dara, Inc" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("500.00"));
            //Act & Assert
            assertNotEquals(PlayerStatusLevel.SPECULATOR, player.getStatus());
        }

        @Test
        @DisplayName("Should not return SPECULATOR when player has doubled net worth " +
                "but traded less than 20 weeks")
        void returnsNotSpeculatorWhenNotEnoughWeeks() {
            //Arrange
            player = new Player("AKL", new BigDecimal("1000.00"));
            for (int week = 1; week <= 10; week++) {
                Stock stock = new Stock("DCL" + week, "Dara, Inc" + week,
                        new ArrayList<>(List.of((new BigDecimal("10.00")))));
                Share share = new Share(stock, new BigDecimal("1"), new BigDecimal("1.00"));
                Purchase purchase = new Purchase(share, week);
                purchase.commit(player);
            }
            player.addMoney(new BigDecimal("2000.00"));
            //Act & Assert
            assertNotEquals(PlayerStatusLevel.SPECULATOR, player.getStatus());
        }
    }

    @Nested
    @DisplayName("previousNetWorth")
    class PreviousNetWorth {

        @Test
        @DisplayName("Should return null when previousNetWorth has not been set")
        void returnsNullWhenNotSet() {
            assertNull(player.getPreviousNetWorth());
        }

        @Test
        @DisplayName("Should return the value that was set")
        void returnsValueWhenSet() {
            // Arrange
            BigDecimal expected = new BigDecimal("5000.00");

            // Act
            player.setPreviousNetWorth(expected);

            // Assert
            assertEquals(expected, player.getPreviousNetWorth());
        }

        @Test
        @DisplayName("Should update when set multiple times")
        void updatesWhenSetMultipleTimes() {
            // Arrange
            player.setPreviousNetWorth(new BigDecimal("5000.00"));

            // Act
            player.setPreviousNetWorth(new BigDecimal("7500.00"));

            // Assert
            assertEquals(new BigDecimal("7500.00"), player.getPreviousNetWorth());
        }
    }
}