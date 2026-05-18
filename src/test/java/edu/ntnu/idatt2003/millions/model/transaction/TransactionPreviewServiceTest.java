package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TransactionPreviewService}.
 * Covers {@link TransactionPreviewService#previewPurchase} and
 * {@link TransactionPreviewService#previewSale} including the partial-position branch,
 * currency conversion, and the loss path where tax must be zero.
 * All tests follow the AAA pattern.
 */
class TransactionPreviewServiceTest {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");

    private TransactionPreviewService service;
    private CurrencyConverter converter;
    private Player player;

    @BeforeEach
    void setUp() {
        service = new TransactionPreviewService();
        converter = new FixedRateCurrencyConverter();
        player = new Player("Test", new BigDecimal("10000"));
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
    @DisplayName("previewPurchase()")
    class PreviewPurchase {

        @Test
        @DisplayName("Should compute gross as salesPrice times quantity")
        void computesGrossAsSalesPriceTimesQuantity() {
            // Arrange - spec: gross = salesPrice × quantity = 100×10 = 1000
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1000"), preview.gross());
        }

        @Test
        @DisplayName("Should compute commission as 0.5% of gross for a purchase")
        void computesHalfPercentCommission() {
            // Arrange - gross=1000; commission=1000×0.005=5
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("5"), preview.commission());
        }

        @Test
        @DisplayName("Should return zero tax for a purchase")
        void returnsZeroTaxForPurchase() {
            // Arrange - spec: no tax on purchases
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, preview.tax());
        }

        @Test
        @DisplayName("Should compute total as gross plus commission")
        void computesTotalAsGrossPlusCommission() {
            // Arrange - total = 1000 + 5 = 1005
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1005"), preview.total());
        }

        @Test
        @DisplayName("Should compute balanceAfter by subtracting totalInNok from player balance")
        void subtractsNokTotalFromBalance() {
            // Arrange - balance=10000, totalInNok=1005 → balanceAfter=8995
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("8995"), preview.balanceAfter());
        }

        @Test
        @DisplayName("Should return null profit, profitPercent, and profitInNok for a purchase")
        void returnsNullProfitFieldsForPurchase() {
            // Arrange - spec: profit fields only apply to sales
            Stock stock = nokStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("10"), player, converter);

            // Assert
            assertNull(preview.profit());
            assertNull(preview.profitPercent());
            assertNull(preview.profitInNok());
        }

        @Test
        @DisplayName("Should convert total to NOK and adjust balance when stock is priced in USD")
        void convertsTotalToNokForUsdStock() {
            // Arrange - price=100 USD, qty=5: total=502.5 USD
            // totalInNok = 502.5 × 9.21 = 4628.025 NOK
            // balanceAfter = 10000 − 4628.025 = 5371.975 NOK
            Stock stock = usdStock("100");

            // Act
            TransactionPreview preview =
                    service.previewPurchase(stock, new BigDecimal("5"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("4628.025"), preview.totalInNok());
            assertBigDecimalEquals(new BigDecimal("5371.975"), preview.balanceAfter());
        }

        @Test
        @DisplayName("Should throw NullPointerException when stock is null")
        void throwsWhenStockIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.previewPurchase(null, BigDecimal.ONE, player, converter));
        }

        @Test
        @DisplayName("Should throw NullPointerException when quantity is null")
        void throwsWhenQuantityIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.previewPurchase(nokStock("100"), null, player, converter));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when quantity is negative")
        void throwsWhenQuantityIsNegative() {
            // Arrange - Share constructor rejects negative quantity
            Stock stock = nokStock("100");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.previewPurchase(stock, new BigDecimal("-1"), player, converter));
        }

        @Test
        @DisplayName("Should throw NullPointerException when player is null")
        void throwsWhenPlayerIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.previewPurchase(nokStock("100"), BigDecimal.ONE, null, converter));
        }
    }

    @Nested
    @DisplayName("previewSale()")
    class PreviewSale {

        @Test
        @DisplayName("Should compute tax as 30% of gain for a profitable full-position sale")
        void computesThirtyPercentTaxOnGainForProfitableSale() {
            // Arrange - spec: tax = 30% of (gross − commission − purchaseCost)
            // salePrice=200, qty=10, purchasePrice=100
            // purchaseCost=1005; gross=2000; commission=20; gain=975; tax=292.5
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("292.5"), preview.tax());
        }

        @Test
        @DisplayName("Should compute net total as gross minus commission minus tax for full position sale")
        void computesNetTotalForFullPositionSale() {
            // Arrange - total = 2000 − 20 − 292.5 = 1687.5
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1687.5"), preview.total());
        }

        @Test
        @DisplayName("Should compute profit as total minus original purchase cost")
        void computesProfitAsTotalMinusPurchaseCost() {
            // Arrange - profit = total − purchaseCost = 1687.5 − 1005 = 682.5
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("682.5"), preview.profit());
        }

        @Test
        @DisplayName("Should compute profitPercent as profit over purchaseCost times 100")
        void computesProfitPercent() {
            // Arrange - profitPercent = 682.5×100/1005 = 67.9% (1dp HALF_UP)
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("67.9"), preview.profitPercent());
        }

        @Test
        @DisplayName("Should add totalInNok to player balance for a NOK sale")
        void addsNokTotalToPlayerBalance() {
            // Arrange - balanceAfter = 10000 + 1687.5 = 11687.5
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("11687.5"), preview.balanceAfter());
        }

        @Test
        @DisplayName("Should return null profitInNok when stock is already NOK")
        void returnsNullProfitInNokForNokStock() {
            // Arrange - spec: profitInNok is null when stock currency equals NOK
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertNull(preview.profitInNok());
        }

        @Test
        @DisplayName("Should use the partial quantity when selling less than the full position")
        void usesPartialQuantityForPartialSale() {
            // Arrange - selling 5 of 10: partial purchaseCost=502.5, gross=1000,
            // commission=10, gain=487.5, tax=146.25, total=843.75
            Stock stock = nokStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("5"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("843.75"), preview.total());
            assertBigDecimalEquals(new BigDecimal("10843.75"), preview.balanceAfter());
        }

        @Test
        @DisplayName("Should return zero tax when a sale results in a loss")
        void returnsZeroTaxForLossingSale() {
            // Arrange - salePrice=50, qty=10, purchasePrice=100
            // purchaseCost=1005; gross=500; commission=5; gain=−510 → tax=0
            Stock stock = nokStock("50");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, preview.tax());
        }

        @Test
        @DisplayName("Should return a negative profit when a sale results in a loss")
        void returnsNegativeProfitForLossingSale() {
            // Arrange - total=495; profit = 495 − 1005 = −510
            Stock stock = nokStock("50");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-510"), preview.profit());
        }

        @Test
        @DisplayName("Should convert totalInNok and compute profitInNok for a USD stock sale")
        void convertsTotalAndProfitToNokForUsdStock() {
            // Arrange - salePrice=200 USD, qty=10, purchasePrice=100 USD
            // total=1687.5 USD → totalInNok=1687.5×9.21=15541.875 NOK
            // profit=682.5 USD → profitInNok=682.5×9.21=6285.825 NOK
            Stock stock = usdStock("200");
            Share sh = share(stock, "10", "100");

            // Act
            TransactionPreview preview =
                    service.previewSale(sh, new BigDecimal("10"), player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("15541.875"), preview.totalInNok());
            assertBigDecimalEquals(new BigDecimal("6285.825"), preview.profitInNok());
            assertBigDecimalEquals(new BigDecimal("25541.875"), preview.balanceAfter());
        }

        @Test
        @DisplayName("Should throw NullPointerException when share is null")
        void throwsWhenShareIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.previewSale(null, BigDecimal.ONE, player, converter));
        }

        @Test
        @DisplayName("Should throw NullPointerException when player is null")
        void throwsWhenPlayerIsNull() {
            // Arrange
            Stock stock = nokStock("100");
            Share sh = share(stock, "10", "100");

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.previewSale(sh, new BigDecimal("10"), null, converter));
        }
    }
}
