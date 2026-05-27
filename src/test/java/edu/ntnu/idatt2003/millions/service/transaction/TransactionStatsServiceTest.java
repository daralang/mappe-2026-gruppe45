package edu.ntnu.idatt2003.millions.service.transaction;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
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
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionStatsService}.
 * Covers per-transaction display values via {@link TransactionStatsService#getStats} and
 * aggregate summary values via {@link TransactionStatsService#getSummary}, including the
 * polymorphic purchase-vs-sale dispatch that the instanceof→polymorphism refactor touched.
 * All tests follow the AAA pattern.
 */
class TransactionStatsServiceTest {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");

    private TransactionStatsService service;
    private CurrencyConverter converter;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        service = new TransactionStatsService();
        converter = new FixedRateCurrencyConverter();
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    private static Stock nokStock(String price) {
        return new Stock("TST", "Test Co.", List.of(new BigDecimal(price)), NOK);
    }

    private static Stock usdStock(String price) {
        return new Stock("TST", "Test Co.", List.of(new BigDecimal(price)), USD);
    }

    private static Share share(Stock stock, String qty, String purchasePrice) {
        return new Share(stock, new BigDecimal(qty), new BigDecimal(purchasePrice));
    }

    @Nested
    @DisplayName("getStats()")
    class GetStats {

        @Nested
        @DisplayName("purchase")
        class PurchasePaths {

            @Test
            @DisplayName("Should return 0.5% of gross as commission for a purchase")
            void returnsHalfPercentCommissionForPurchase() {
                // Arrange - spec: purchase commission = 0.5% of gross
                // price=100, qty=10: gross=1000; commission=1000×0.005=5
                Stock stock = nokStock("100");
                Share sh = share(stock, "10", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("5"), stats.commissionNative());
            }

            @Test
            @DisplayName("Should return zero tax for a purchase")
            void returnsZeroTaxForPurchase() {
                // Arrange - spec: no tax is applied on purchases
                Stock stock = nokStock("100");
                Share sh = share(stock, "10", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertBigDecimalEquals(BigDecimal.ZERO, stats.taxNative());
                assertBigDecimalEquals(BigDecimal.ZERO, stats.taxNok());
            }

            @Test
            @DisplayName("Should return a negative amountNok for a purchase")
            void returnsNegativeAmountNokForPurchase() {
                // Arrange - spec: purchase is an outflow; gross=1000, commission=5, total=1005
                // signedTotal = −1005 → amountNok = −1005 (NOK→NOK identity)
                Stock stock = nokStock("100");
                Share sh = share(stock, "10", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("-1005"), stats.amountNok());
            }

            @Test
            @DisplayName("Should return purchasePrice as pricePerShare for a purchase")
            void returnsPurchasePriceAsPerShareForPurchase() {
                // Arrange
                Stock stock = nokStock("120");
                Share sh = share(stock, "5", "120");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert - spec: purchase price is the per-share price for buys
                assertBigDecimalEquals(new BigDecimal("120"), stats.pricePerShare());
            }

            @Test
            @DisplayName("Should expose the stock's native currency code for a purchase")
            void returnsNativeCurrencyCodeForPurchase() {
                // Arrange
                Stock stock = nokStock("100");
                Share sh = share(stock, "1", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertEquals("NOK", stats.nativeCurrencyCode());
            }
        }

        @Nested
        @DisplayName("profitable sale")
        class ProfitableSalePaths {

            @Test
            @DisplayName("Should return 1% of gross as commission for a sale")
            void returnsOnePercentCommissionForSale() {
                // Arrange - spec: sale commission = 1% of gross
                // salePrice=200, qty=10: gross=2000; commission=2000×0.01=20
                Stock stock = nokStock("200");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("20"), stats.commissionNative());
            }

            @Test
            @DisplayName("Should return 30% of gain as tax for a profitable sale")
            void returnsThirtyPercentTaxOnGainForProfitableSale() {
                // Arrange - spec: tax = 30% of (gross − commission − purchaseCost)
                // salePrice=200, qty=10, purchasePrice=100
                // purchaseCost = 1000 + 1000×0.005 = 1005
                // gross=2000, commission=20, gain=2000−20−1005=975
                // tax = 975×0.30 = 292.5
                Stock stock = nokStock("200");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("292.5"), stats.taxNative());
                assertBigDecimalEquals(new BigDecimal("292.5"), stats.taxNok());
            }

            @Test
            @DisplayName("Should return a positive amountNok for a profitable sale")
            void returnsPositiveAmountNokForProfitableSale() {
                // Arrange - spec: sale is an inflow
                // total = gross − commission − tax = 2000 − 20 − 292.5 = 1687.5
                Stock stock = nokStock("200");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("1687.5"), stats.amountNok());
            }

            @Test
            @DisplayName("Should return gross divided by quantity as pricePerShare for a sale")
            void returnsGrossDividedByQuantityAsPerShareForSale() {
                // Arrange - gross=2000, qty=10 → pricePerShare=200
                Stock stock = nokStock("200");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("200"), stats.pricePerShare());
            }
        }

        @Nested
        @DisplayName("loss sale")
        class LossSalePaths {

            @Test
            @DisplayName("Should return zero tax when sale results in a loss")
            void returnsZeroTaxWhenSaleIsALoss() {
                // Arrange - spec: no tax is levied when gain is zero or negative
                // salePrice=50, qty=10, purchasePrice=100
                // purchaseCost=1005; gross=500; commission=5; gain=500−5−1005=−510 → tax=0
                Stock stock = nokStock("50");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(BigDecimal.ZERO, stats.taxNative());
                assertBigDecimalEquals(BigDecimal.ZERO, stats.taxNok());
            }

            @Test
            @DisplayName("Should return a positive amountNok even when a sale is a loss")
            void returnsPositiveAmountNokForLossSale() {
                // Arrange - total = 500 − 5 − 0 = 495 NOK (inflow even at a loss)
                Stock stock = nokStock("50");
                Share sh = share(stock, "10", "100");
                Transaction sale = new Sale(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(sale, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("495"), stats.amountNok());
            }
        }

        @Nested
        @DisplayName("currency conversion")
        class CurrencyConversion {

            @Test
            @DisplayName("Should convert commission from USD to NOK for a purchase")
            void convertsUsdPurchaseCommissionToNok() {
                // Arrange - price=100 USD, qty=5, purchasePrice=100 USD
                // commissionNative = 500×0.005 = 2.5 USD
                // commissionNok = 2.5×9.21 = 23.025 NOK
                Stock stock = usdStock("100");
                Share sh = share(stock, "5", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("2.5"), stats.commissionNative());
                assertBigDecimalEquals(new BigDecimal("23.025"), stats.commissionNok());
            }

            @Test
            @DisplayName("Should convert amountNok from USD to NOK and expose USD currency code for a purchase")
            void convertsUsdPurchaseAmountToNokAndExposesCurrencyCode() {
                // Arrange - signedTotal = −502.5 USD; amountNok = −502.5×9.21 = −4628.025 NOK
                Stock stock = usdStock("100");
                Share sh = share(stock, "5", "100");
                Transaction purchase = new Purchase(sh, 1);

                // Act
                TransactionStatsService.TransactionStats stats = service.getStats(purchase, converter);

                // Assert
                assertBigDecimalEquals(new BigDecimal("-4628.025"), stats.amountNok());
                assertEquals("USD", stats.nativeCurrencyCode());
            }
        }

        @Nested
        @DisplayName("null inputs")
        class NullInputs {

            @Test
            @DisplayName("Should throw NullPointerException when transaction is null")
            void throwsWhenTransactionIsNull() {
                // Act & Assert
                assertThrows(NullPointerException.class,
                        () -> service.getStats(null, converter));
            }

            @Test
            @DisplayName("Should throw NullPointerException when converter is null")
            void throwsWhenConverterIsNull() {
                // Arrange
                Stock stock = nokStock("100");
                Transaction purchase = new Purchase(share(stock, "1", "100"), 1);

                // Act & Assert
                assertThrows(NullPointerException.class,
                        () -> service.getStats(purchase, null));
            }
        }
    }

    @Nested
    @DisplayName("getSummary()")
    class GetSummary {

        @Test
        @DisplayName("Should return all zeros for an empty transaction list")
        void returnsAllZerosForEmptyList() {
            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(), converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, summary.purchasesNok());
            assertBigDecimalEquals(BigDecimal.ZERO, summary.salesNok());
            assertBigDecimalEquals(BigDecimal.ZERO, summary.totalNok());
        }

        @Test
        @DisplayName("Should accumulate purchase amountNok into purchasesNok as a non-positive value")
        void accumulatesPurchaseAsNonPositive() {
            // Arrange - amountNok for purchase = −1005 (see getStats purchase tests)
            Stock stock = nokStock("100");
            Transaction purchase = new Purchase(share(stock, "10", "100"), 1);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(purchase), converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-1005"), summary.purchasesNok());
            assertBigDecimalEquals(BigDecimal.ZERO, summary.salesNok());
        }

        @Test
        @DisplayName("Should accumulate sale amountNok into salesNok as a non-negative value")
        void accumulatesSaleAsNonNegative() {
            // Arrange - amountNok for profitable sale = +1687.5
            Stock stock = nokStock("200");
            Transaction sale = new Sale(share(stock, "10", "100"), 1);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(sale), converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, summary.purchasesNok());
            assertBigDecimalEquals(new BigDecimal("1687.5"), summary.salesNok());
        }

        @Test
        @DisplayName("Should compute totalNok as purchasesNok plus salesNok for mixed list")
        void totalNokIsSumOfPurchasesAndSalesForMixedList() {
            // Arrange - purchase: −1005; profitable sale: +1687.5; total: 682.5
            Stock buyStock = nokStock("100");
            Transaction purchase = new Purchase(share(buyStock, "10", "100"), 1);

            Stock sellStock = nokStock("200");
            Transaction sale = new Sale(share(sellStock, "10", "100"), 1);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(purchase, sale), converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-1005"), summary.purchasesNok());
            assertBigDecimalEquals(new BigDecimal("1687.5"), summary.salesNok());
            assertBigDecimalEquals(new BigDecimal("682.5"), summary.totalNok());
        }

        @Test
        @DisplayName("Should sum multiple purchase amounts correctly")
        void sumsMultiplePurchasesCorrectly() {
            // Arrange - two purchases each −1005 → purchasesNok = −2010
            Stock stock = nokStock("100");
            Transaction p1 = new Purchase(share(stock, "10", "100"), 1);
            Transaction p2 = new Purchase(share(stock, "10", "100"), 2);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(p1, p2), converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-2010"), summary.purchasesNok());
            assertBigDecimalEquals(BigDecimal.ZERO, summary.salesNok());
        }

        @Test
        @DisplayName("Should not count purchase amounts in salesNok")
        void doesNotCountPurchasesInSalesNok() {
            // Arrange
            Stock stock = nokStock("100");
            Transaction purchase = new Purchase(share(stock, "10", "100"), 1);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(purchase), converter);

            // Assert - purchase amounts must not leak into salesNok
            assertBigDecimalEquals(BigDecimal.ZERO, summary.salesNok());
        }

        @Test
        @DisplayName("Should not count sale amounts in purchasesNok")
        void doesNotCountSalesInPurchasesNok() {
            // Arrange
            Stock stock = nokStock("200");
            Transaction sale = new Sale(share(stock, "10", "100"), 1);

            // Act
            TransactionStatsService.TransactionSummary summary =
                    service.getSummary(List.of(sale), converter);

            // Assert - sale amounts must not leak into purchasesNok
            assertBigDecimalEquals(BigDecimal.ZERO, summary.purchasesNok());
        }

        @Test
        @DisplayName("Should throw NullPointerException when transaction list is null")
        void throwsWhenTransactionListIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.getSummary(null, converter));
        }
    }
}
