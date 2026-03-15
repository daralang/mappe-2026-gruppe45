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
 * Unit tests for the {@link TransactionArchive} class.
 * <p>
 * This test class verifies the behaviour of the TransactionArchive model, including
 * adding transactions, retrieving by week and type, and counting distinct weeks.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class TransactionArchiveTest {

    private TransactionArchive archive;
    private Share share;

    @BeforeEach
    void setUp() {
        archive = new TransactionArchive();
        share = new Share(
                new Stock("DIS", "The Walt Disney Company",
                        new ArrayList<>(List.of(new BigDecimal("100.00")))),
                new BigDecimal("10"), new BigDecimal("50.00"));
    }

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("Should return true when transaction is added for the first time")
        void returnsTrueWhenTransactionIsNew() {
            // Arrange
            Purchase purchase = new Purchase(share, 1);
            // Act
            boolean actual = archive.add(purchase);
            // Assert
            assertTrue(actual);
        }

        @Test
        @DisplayName("Should return false when transaction already exists in archive")
        void returnsFalseWhenTransactionAlreadyExists() {
            // Arrange
            Purchase purchase = new Purchase(share, 1);
            archive.add(purchase);
            // Act
            boolean actual = archive.add(purchase);
            // Assert
            assertFalse(actual);
        }

        @Test
        @DisplayName("Should throw exception when transaction is null")
        void throwsExceptionWhenTransactionIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    archive.add(null));
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmpty {

        @Test
        @DisplayName("Should return true when archive is empty")
        void returnsTrueWhenArchiveIsEmpty() {
            // Act & Assert
            assertTrue(archive.isEmpty());
        }

        @Test
        @DisplayName("Should return false when archive has transactions")
        void returnsFalseWhenArchiveHasTransactions() {
            // Arrange
            archive.add(new Purchase(share, 1));
            // Act & Assert
            assertFalse(archive.isEmpty());
        }
    }

    @Nested
    @DisplayName("getTransactions()")
    class GetTransactions {

        @Test
        @DisplayName("Should return only transactions from the given week")
        void returnsOnlyTransactionsFromGivenWeek() {
            // Arrange
            Purchase week1 = new Purchase(share, 1);
            Purchase week2 = new Purchase(share, 2);
            archive.add(week1);
            archive.add(week2);
            // Act
            List<Transaction> result = archive.getTransactions(1);
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(week1));
            assertFalse(result.contains(week2));
        }

        @Test
        @DisplayName("Should return empty list when no transactions exist for given week")
        void returnsEmptyListWhenNoTransactionsForWeek() {
            // Arrange
            archive.add(new Purchase(share, 1));
            // Act
            List<Transaction> result = archive.getTransactions(2);
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when week is zero")
        void throwsExceptionWhenWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getTransactions(0));
        }

        @Test
        @DisplayName("Should throw exception when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getTransactions(-1));
        }

        @Test
        @DisplayName("Should throw exception when week is greater than 52")
        void throwsExceptionWhenWeekIsGreaterThan52() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getTransactions(53));
        }
    }

    @Nested
    @DisplayName("getPurchases()")
    class GetPurchases {

        @Test
        @DisplayName("Should return only purchases from the given week")
        void returnsOnlyPurchasesFromGivenWeek() {
            // Arrange
            Share share2 = new Share(
                    new Stock("NKE", "Nike, Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00")))),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            Purchase purchase = new Purchase(share, 1);
            Sale sale = new Sale(share2, 1);
            archive.add(purchase);
            archive.add(sale);
            // Act
            List<Purchase> result = archive.getPurchases(1);
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(purchase));
        }

        @Test
        @DisplayName("Should return empty list when no purchases exist for given week")
        void returnsEmptyListWhenNoPurchasesForWeek() {
            // Arrange
            archive.add(new Purchase(share, 1));
            // Act
            List<Purchase> result = archive.getPurchases(2);
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when week is zero")
        void throwsExceptionWhenWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getPurchases(0));
        }

        @Test
        @DisplayName("Should throw exception when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getPurchases(-1));
        }

        @Test
        @DisplayName("Should throw exception when week is greater than 52")
        void throwsExceptionWhenWeekIsGreaterThan52() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getPurchases(53));
        }
    }

    @Nested
    @DisplayName("getSales()")
    class GetSales {

        @Test
        @DisplayName("Should return only sales from the given week")
        void returnsOnlySalesFromGivenWeek() {
            // Arrange
            Share share2 = new Share(
                    new Stock("NKE", "Nike, Inc",
                            new ArrayList<>(List.of(new BigDecimal("100.00")))),
                    new BigDecimal("10"), new BigDecimal("50.00"));
            Purchase purchase = new Purchase(share, 1);
            Sale sale = new Sale(share2, 1);
            archive.add(purchase);
            archive.add(sale);
            // Act
            List<Sale> result = archive.getSales(1);
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.contains(sale));
        }

        @Test
        @DisplayName("Should return empty list when no sales exist for given week")
        void returnsEmptyListWhenNoSalesForWeek() {
            // Arrange
            archive.add(new Purchase(share, 1));
            // Act
            List<Sale> result = archive.getSales(2);
            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when week is zero")
        void throwsExceptionWhenWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getSales(0));
        }

        @Test
        @DisplayName("Should throw exception when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getSales(-1));
        }

        @Test
        @DisplayName("Should throw exception when week is greater than 52")
        void throwsExceptionWhenWeekIsGreaterThan52() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getSales(53));
        }
    }

    @Nested
    @DisplayName("countDistinctWeeks()")
    class CountDistinctWeeks {

        @Test
        @DisplayName("Should return zero when archive is empty")
        void returnsZeroWhenArchiveIsEmpty() {
            // Act & Assert
            assertEquals(0, archive.countDistinctWeeks());
        }

        @Test
        @DisplayName("Should return correct count when transactions span multiple weeks")
        void returnsCorrectCountForMultipleWeeks() {
            // Arrange
            archive.add(new Purchase(share, 1));
            archive.add(new Purchase(share, 2));
            // Act & Assert
            assertEquals(2, archive.countDistinctWeeks());
        }

        @Test
        @DisplayName("Should count week only once when multiple transactions exist in same week")
        void countsWeekOnlyOnceForMultipleTransactions() {
            // Arrange
            archive.add(new Purchase(share, 1));
            archive.add(new Purchase(share, 1));
            // Act & Assert
            assertEquals(1, archive.countDistinctWeeks());
        }
    }
}