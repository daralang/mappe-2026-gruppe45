package edu.ntnu.idatt2003.millions.model;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
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
    @DisplayName("Sale(Share, int)")
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
    }

    @Nested
    @DisplayName("Sale(Share, int, BigDecimal)")
    class ConstructorWithSalesPrice {

        @Test
        @DisplayName("Should return correct share when created with sales price in NOK")
        void returnsCorrectShare() {
            // Arrange
            Sale sale = new Sale(share, 1, new BigDecimal("1050.00"));
            // Act & Assert
            assertEquals(share, sale.getShare());
        }

        @Test
        @DisplayName("Should return correct week when created with sales price in NOK")
        void returnsCorrectWeek() {
            // Arrange
            Sale sale = new Sale(share, 1, new BigDecimal("1050.00"));
            // Act & Assert
            assertEquals(1, sale.getWeek());
        }

        @Test
        @DisplayName("Should not be committed when created with sales price in NOK")
        void isNotCommittedWhenCreated() {
            // Arrange
            Sale sale = new Sale(share, 1, new BigDecimal("1050.00"));
            // Act & Assert
            assertFalse(sale.isCommitted());
        }

        @Test
        @DisplayName("Should throw exception when share is null")
        void throwsExceptionWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Sale(null, 1, new BigDecimal("1050.00")));
        }

        @Test
        @DisplayName("Should throw exception when sales price is null")
        void throwsExceptionWhenSalesPriceIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new Sale(share, 1, null));
        }

        @Test
        @DisplayName("Should use provided sales price in NOK for payout calculation")
        void usesProvidedSalesPriceForPayout() {
            // Arrange - 100 USD * 10.50 = 1050 NOK per aksje
            BigDecimal salesPriceInNok = new BigDecimal("1050.00");
            Sale sale = new Sale(share, 1, salesPriceInNok);
            BigDecimal expectedBalance = new BigDecimal("10000.00")
                    .add(sale.getCalculator().calculateTotal());
            // Act
            sale.commit(player);
            // Assert
            assertEquals(0, expectedBalance.compareTo(player.getMoney()));
        }

        @Test
        @DisplayName("Should add higher payout when sales price in NOK is higher than native price")
        void addsHigherPayoutWhenNokPriceIsHigher() {
            // Arrange
            Sale nativeSale = new Sale(share, 1); // bruker stock.getSalesPrice() = 100
            Sale nokSale = new Sale(share, 1, new BigDecimal("1050.00")); // konvertert NOK-pris
            // Assert
            assertTrue(nokSale.getCalculator().calculateTotal()
                    .compareTo(nativeSale.getCalculator().calculateTotal()) > 0);
        }
    }

    @Nested
    @DisplayName("commit()")
    class Commit {

        private Sale sale;

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