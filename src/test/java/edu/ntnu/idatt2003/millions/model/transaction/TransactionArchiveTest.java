package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

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

    @Nested
    @DisplayName("getRealizedGainsByCurrency()")
    class GetRealizedGainsByCurrency {

        @Test
        @DisplayName("Should return empty map when archive is empty")
        void returnsEmptyMapWhenArchiveIsEmpty() {
            // Act & Assert
            assertTrue(archive.getRealizedGainsByCurrency().isEmpty());
        }

        @Test
        @DisplayName("Should include profitable sale and exclude losing sale")
        void includesOnlyProfitableSales() {
            // Arrange
            Player player = new Player("Alva", new BigDecimal("100000.00"));
            Share gainShare = makeShare("DIS", new BigDecimal("200.00"), new BigDecimal("100.00"));
            Share lossShare = makeShare("NKE", new BigDecimal("50.00"), new BigDecimal("100.00"));
            player.getPortfolio().addShare(gainShare);
            player.getPortfolio().addShare(lossShare);
            Sale gainSale = new Sale(gainShare, 1);
            Sale lossSale = new Sale(lossShare, 1);
            gainSale.commit(player);
            lossSale.commit(player);
            archive.add(gainSale);
            archive.add(lossSale);
            // Act
            Map<Currency, BigDecimal> result = archive.getRealizedGainsByCurrency();
            // Assert
            assertEquals(1, result.size());
            assertTrue(result.containsKey(Currency.getInstance("USD")));
        }

        @Test
        @DisplayName("Realized gain reflects sale-time price, not current price")
        void usesConstructionTimePriceNotCurrentPrice() {
            // Arrange — sale at price 200.00
            Player player = new Player("Alva", new BigDecimal("100000.00"));
            Share gainShare = makeShare("DIS", new BigDecimal("200.00"), new BigDecimal("100.00"));
            player.getPortfolio().addShare(gainShare);
            Sale sale = new Sale(gainShare, 1);
            BigDecimal profitAtSaleTime = sale.getProfit();
            sale.commit(player);
            archive.add(sale);

            // Stock price advances to a much higher value after the sale
            gainShare.getStock().addNewSalesPrice(new BigDecimal("999.00"));

            // A live SalesCalculator would now compute a higher profit
            BigDecimal liveProfit = new SalesCalculator(gainShare).calculateProfit();
            assertNotEquals(0, profitAtSaleTime.compareTo(liveProfit),
                    "Pre-condition: live price should produce a different profit");

            // Act
            Map<Currency, BigDecimal> result = archive.getRealizedGainsByCurrency();

            // Assert — archive must reflect the frozen sale-time value, not the live price
            assertEquals(0, profitAtSaleTime.compareTo(result.get(Currency.getInstance("USD"))));
        }
    }

    @Nested
    @DisplayName("getRealizedLossesByCurrency()")
    class GetRealizedLossesByCurrency {

        @Test
        @DisplayName("Should return absolute loss as a positive number")
        void returnsLossAsPositive() {
            // Arrange
            Player player = new Player("Alva", new BigDecimal("100000.00"));
            Share lossShare = makeShare("NKE", new BigDecimal("50.00"), new BigDecimal("100.00"));
            player.getPortfolio().addShare(lossShare);
            Sale sale = new Sale(lossShare, 1);
            sale.commit(player);
            archive.add(sale);
            // Act
            Map<Currency, BigDecimal> result = archive.getRealizedLossesByCurrency();
            // Assert
            assertFalse(result.isEmpty());
            assertTrue(result.get(Currency.getInstance("USD")).signum() > 0);
        }
    }

    @Nested
    @DisplayName("getSalesCount()")
    class GetSalesCount {

        @Test
        @DisplayName("Should return zero when archive has no sales")
        void returnsZeroWhenNoSales() {
            // Arrange
            archive.add(new Purchase(share, 1));
            // Act & Assert
            assertEquals(0, archive.getSalesCount());
        }

        @Test
        @DisplayName("Should count only sales, not purchases")
        void countsOnlySales() {
            // Arrange
            Player player = new Player("Alva", new BigDecimal("100000.00"));
            Share shareA = makeShare("DIS", new BigDecimal("150.00"), new BigDecimal("100.00"));
            Share shareB = makeShare("NKE", new BigDecimal("150.00"), new BigDecimal("100.00"));
            player.getPortfolio().addShare(shareA);
            player.getPortfolio().addShare(shareB);
            Sale sale1 = new Sale(shareA, 1);
            Sale sale2 = new Sale(shareB, 1);
            sale1.commit(player);
            sale2.commit(player);
            archive.add(new Purchase(share, 1));
            archive.add(sale1);
            archive.add(sale2);
            // Act & Assert
            assertEquals(2, archive.getSalesCount());
        }
    }

    @Nested
    @DisplayName("getTransactionsInRange()")
    class GetTransactionsInRange {

        @Test
        @DisplayName("Should throw IllegalArgumentException when fromWeek is zero")
        void throwsWhenFromWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getTransactionsInRange(0, 5));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when fromWeek is negative")
        void throwsWhenFromWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.getTransactionsInRange(-1, 5));
        }

        @Test
        @DisplayName("Should return only transactions within the requested week range")
        void returnsOnlyTransactionsInRange() {
            // Arrange — transactions at weeks 1, 2, 3; request [2, 3]
            // week 1 is below fromWeek=2 and must be excluded → 2 results
            Purchase week1 = new Purchase(share, 1);
            Purchase week2 = new Purchase(share, 2);
            Purchase week3 = new Purchase(share, 3);
            archive.add(week1);
            archive.add(week2);
            archive.add(week3);
            // Act
            List<Transaction> result = archive.getTransactionsInRange(2, 3);
            // Assert
            assertEquals(2, result.size());
            assertTrue(result.contains(week2));
            assertTrue(result.contains(week3));
            assertFalse(result.contains(week1));
        }

        @Test
        @DisplayName("Should return transactions in cross-week order, earlier weeks first")
        void returnsTransactionsInCrossWeekOrder() {
            // Arrange — add week 3 before week 1 (insertion order reversed) to prove
            // result order follows week number, not insertion order
            Purchase week3 = new Purchase(share, 3);
            Purchase week1 = new Purchase(share, 1);
            archive.add(week3);
            archive.add(week1);
            // Act
            List<Transaction> result = archive.getTransactionsInRange(1, 3);
            // Assert — week 1 transaction must appear before week 3 transaction
            assertTrue(result.indexOf(week1) < result.indexOf(week3));
        }

        @Test
        @DisplayName("Should return empty list when toWeek is less than fromWeek")
        void returnsEmptyListWhenRangeIsInverted() {
            // Arrange — archive has a transaction but inverted range [5, 3] matches none
            archive.add(new Purchase(share, 4));
            // Act & Assert
            assertTrue(archive.getTransactionsInRange(5, 3).isEmpty());
        }

        @Test
        @DisplayName("Should return a fresh mutable copy independent of archive state")
        void returnedListIsFreshMutableCopy() {
            // Arrange — one transaction in week 1
            archive.add(new Purchase(share, 1));
            // Act — retrieve and immediately clear the returned list
            List<Transaction> result = archive.getTransactionsInRange(1, 1);
            result.clear();
            // Assert — archive must be unaffected; a second call still returns 1 transaction
            assertEquals(1, archive.getTransactionsInRange(1, 1).size());
        }
    }

    @Nested
    @DisplayName("countPurchasesInRange()")
    class CountPurchasesInRange {

        @Test
        @DisplayName("Should throw IllegalArgumentException when fromWeek is invalid")
        void throwsWhenFromWeekIsInvalid() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.countPurchasesInRange(0, 5));
        }

        @Test
        @DisplayName("Should count only purchases, not sales, in the requested range")
        void countsOnlyPurchasesInRange() {
            // Arrange — 2 purchases (weeks 2, 3) + 1 sale (week 2) in range [2, 3]
            // only purchases count → expected result: 2
            Share saleShare = makeShare("NKE", new BigDecimal("100.00"), new BigDecimal("50.00"));
            archive.add(new Purchase(share, 2));
            archive.add(new Purchase(share, 3));
            archive.add(new Sale(saleShare, 2));
            // Act
            int result = archive.countPurchasesInRange(2, 3);
            // Assert
            assertEquals(2, result);
        }

        @Test
        @DisplayName("Should return zero when toWeek is less than fromWeek")
        void returnsZeroWhenRangeIsInverted() {
            // Arrange — archive has purchases but inverted range [4, 2] matches none
            archive.add(new Purchase(share, 3));
            // Act & Assert
            assertEquals(0, archive.countPurchasesInRange(4, 2));
        }
    }

    @Nested
    @DisplayName("countSalesInRange()")
    class CountSalesInRange {

        @Test
        @DisplayName("Should throw IllegalArgumentException when fromWeek is invalid")
        void throwsWhenFromWeekIsInvalid() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    archive.countSalesInRange(0, 5));
        }

        @Test
        @DisplayName("Should count only sales, not purchases, in the requested range")
        void countsOnlySalesInRange() {
            // Arrange — 1 purchase (week 2) + 2 sales (weeks 2, 3) in range [2, 3]
            // only sales count → expected result: 2
            Share saleShare1 = makeShare("NKE", new BigDecimal("100.00"), new BigDecimal("50.00"));
            Share saleShare2 = makeShare("AAPL", new BigDecimal("150.00"), new BigDecimal("80.00"));
            archive.add(new Purchase(share, 2));
            archive.add(new Sale(saleShare1, 2));
            archive.add(new Sale(saleShare2, 3));
            // Act
            int result = archive.countSalesInRange(2, 3);
            // Assert
            assertEquals(2, result);
        }

        @Test
        @DisplayName("Should return zero when toWeek is less than fromWeek")
        void returnsZeroWhenRangeIsInverted() {
            // Arrange — archive has sales but inverted range [4, 2] matches none
            Share saleShare = makeShare("NKE", new BigDecimal("100.00"), new BigDecimal("50.00"));
            archive.add(new Sale(saleShare, 3));
            // Act & Assert
            assertEquals(0, archive.countSalesInRange(4, 2));
        }
    }

    private static Share makeShare(String symbol, BigDecimal salesPrice, BigDecimal purchasePrice) {
        return new Share(
                new Stock(symbol, "Test Co",
                        new ArrayList<>(List.of(salesPrice))),
                new BigDecimal("10"), purchasePrice);
    }
}