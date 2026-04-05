package edu.ntnu.idatt2003.millions.factory;

import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link TransactionFactory} class.
 * <p>
 * This test class verifies the behaviour of TransactionFactory, including
 * correct creation of purchase and sale transactions, and validation of input.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class TransactionFactoryTest {

    private Share share;

    @BeforeEach
    void setUp() {
        Stock stock = new Stock("DIS", "The Walt Disney Company",
                new ArrayList<>(List.of(new BigDecimal("100.00"))));
        share = new Share(stock, new BigDecimal("5"), new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("createPurchase()")
    class CreatePurchase {

        @Test
        @DisplayName("Returns a Purchase transaction")
        void returnsInstanceOfPurchase() {
            // Act
            Transaction transaction = TransactionFactory.createPurchase(share, 1);
            // Assert
            assertInstanceOf(Purchase.class, transaction);
        }

        @Test
        @DisplayName("Returns transaction with correct share")
        void returnsTransactionWithCorrectShare() {
            // Act
            Transaction transaction = TransactionFactory.createPurchase(share, 1);
            // Assert
            assertEquals(share, transaction.getShare());
        }

        @Test
        @DisplayName("Returns transaction with correct week")
        void returnsTransactionWithCorrectWeek() {
            // Act
            Transaction transaction = TransactionFactory.createPurchase(share, 3);
            // Assert
            assertEquals(3, transaction.getWeek());
        }

        @Test
        @DisplayName("Throws NullPointerException when share is null")
        void throwsExceptionWhenShareIsNull() {
            assertThrows(NullPointerException.class,
                    () -> TransactionFactory.createPurchase(null, 1));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when week is less than 1")
        void throwsExceptionWhenWeekIsLessThanOne() {
            assertThrows(IllegalArgumentException.class,
                    () -> TransactionFactory.createPurchase(share, 0));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> TransactionFactory.createPurchase(share, -1));
        }
    }

    @Nested
    @DisplayName("createSale()")
    class CreateSale {

        @Test
        @DisplayName("Returns a Sale transaction")
        void returnsInstanceOfSale() {
            // Act
            Transaction transaction = TransactionFactory.createSale(share, 1);
            // Assert
            assertInstanceOf(Sale.class, transaction);
        }

        @Test
        @DisplayName("Returns transaction with correct share")
        void returnsTransactionWithCorrectShare() {
            // Act
            Transaction transaction = TransactionFactory.createSale(share, 1);
            // Assert
            assertEquals(share, transaction.getShare());
        }

        @Test
        @DisplayName("Returns transaction with correct week")
        void returnsTransactionWithCorrectWeek() {
            // Act
            Transaction transaction = TransactionFactory.createSale(share, 3);
            // Assert
            assertEquals(3, transaction.getWeek());
        }

        @Test
        @DisplayName("Throws NullPointerException when share is null")
        void throwsExceptionWhenShareIsNull() {
            assertThrows(NullPointerException.class,
                    () -> TransactionFactory.createSale(null, 1));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when week is less than 1")
        void throwsExceptionWhenWeekIsLessThanOne() {
            assertThrows(IllegalArgumentException.class,
                    () -> TransactionFactory.createSale(share, 0));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when week is negative")
        void throwsExceptionWhenWeekIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> TransactionFactory.createSale(share, -1));
        }
    }
}