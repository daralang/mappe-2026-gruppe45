package edu.ntnu.idatt2003.millions.service.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RealizedReturnsService}.
 * Covers all six public methods — gains, losses, net realized, total tax, total commission,
 * and sales count — including empty-archive zeros, profitable/loss sale paths, multi-sale
 * aggregation, and USD currency conversion via {@link FixedRateCurrencyConverter}.
 * Sales are added directly to the player's {@code TransactionArchive} (bypassing
 * {@link Sale#commit}) so tests control archive state without full game-flow setup.
 * All tests follow the AAA pattern.
 */
class RealizedReturnsServiceTest {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");

    private RealizedReturnsService service;
    private CurrencyConverter converter;
    private Player player;

    @BeforeEach
    void setUp() {
        service = new RealizedReturnsService();
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

    /**
     * Creates a profitable NOK Sale: salePrice=200, qty=10, purchasePrice=100.
     * profit = total − purchaseCost = 1687.5 − 1005 = 682.5 NOK
     * commission = 2000×0.01 = 20 NOK; tax = 975×0.30 = 292.5 NOK
     */
    private static Sale profitableNokSale() {
        Stock stock = nokStock("200");
        return new Sale(share(stock, "10", "100"), 1);
    }

    /**
     * Creates a loss NOK Sale: salePrice=50, qty=10, purchasePrice=100.
     * profit = 495 − 1005 = −510 NOK; commission = 500×0.01 = 5 NOK; tax = 0
     */
    private static Sale lossNokSale() {
        Stock stock = nokStock("50");
        return new Sale(share(stock, "10", "100"), 1);
    }

    /**
     * Creates a profitable USD Sale: salePrice=200, qty=10, purchasePrice=100.
     * profit = 682.5 USD → 682.5×9.21 = 6285.825 NOK
     * commission = 20 USD → 20×9.21 = 184.2 NOK
     * tax = 292.5 USD → 292.5×9.21 = 2693.925 NOK
     */
    private static Sale profitableUsdSale() {
        Stock stock = usdStock("200");
        return new Sale(share(stock, "10", "100"), 1);
    }

    @Nested
    @DisplayName("getGainsInNok()")
    class GetGainsInNok {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act
            BigDecimal gains = service.getGainsInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, gains);
        }

        @Test
        @DisplayName("Should return the profit of a single profitable sale in NOK")
        void returnsProfitOfSingleProfitableSale() {
            // Arrange — profitable NOK sale: profit=682.5 NOK
            player.getTransactionArchive().add(profitableNokSale());

            // Act
            BigDecimal gains = service.getGainsInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("682.5"), gains);
        }

        @Test
        @DisplayName("Should return zero when all sales are losses")
        void returnsZeroWhenAllSalesAreLosses() {
            // Arrange — loss sale: profit=−510, excluded from gains
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal gains = service.getGainsInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, gains);
        }

        @Test
        @DisplayName("Should sum gains across multiple profitable sales in NOK")
        void sumsGainsAcrossMultipleProfitableSales() {
            // Arrange — two profitable NOK sales: 682.5 + 682.5 = 1365 NOK
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(new Sale(share(nokStock("200"), "10", "100"), 2));

            // Act
            BigDecimal gains = service.getGainsInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1365"), gains);
        }

        @Test
        @DisplayName("Should convert USD profit to NOK for a profitable USD sale")
        void convertsUsdProfitToNok() {
            // Arrange — profitable USD sale: profit=682.5 USD → 682.5×9.21=6285.825 NOK
            player.getTransactionArchive().add(profitableUsdSale());

            // Act
            BigDecimal gains = service.getGainsInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("6285.825"), gains);
        }
    }

    @Nested
    @DisplayName("getLossesInNok()")
    class GetLossesInNok {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act
            BigDecimal losses = service.getLossesInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, losses);
        }

        @Test
        @DisplayName("Should return the absolute loss of a single losing sale as a positive value in NOK")
        void returnsAbsoluteLossAsPositiveValue() {
            // Arrange — loss NOK sale: profit=−510; absolute loss=510 NOK
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal losses = service.getLossesInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("510"), losses);
        }

        @Test
        @DisplayName("Should return zero when all sales are profitable")
        void returnsZeroWhenAllSalesAreProfitable() {
            // Arrange — profitable sale: profit=682.5, excluded from losses
            player.getTransactionArchive().add(profitableNokSale());

            // Act
            BigDecimal losses = service.getLossesInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, losses);
        }
    }

    @Nested
    @DisplayName("getNetRealizedInNok()")
    class GetNetRealizedInNok {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act
            BigDecimal net = service.getNetRealizedInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, net);
        }

        @Test
        @DisplayName("Should return gains minus losses when archive has both")
        void returnsGainsMinusLosses() {
            // Arrange — profitable sale: gains=682.5; loss sale: losses=510
            // net = 682.5 − 510 = 172.5 NOK
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal net = service.getNetRealizedInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("172.5"), net);
        }

        @Test
        @DisplayName("Should return a negative net when losses exceed gains")
        void returnsNegativeNetWhenLossesExceedGains() {
            // Arrange — two loss sales: losses=1020; no gains; net=0−1020=−1020 NOK
            player.getTransactionArchive().add(lossNokSale());
            player.getTransactionArchive().add(new Sale(share(nokStock("50"), "10", "100"), 2));

            // Act
            BigDecimal net = service.getNetRealizedInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-1020"), net);
        }
    }

    @Nested
    @DisplayName("getTotalTaxPaidInNok()")
    class GetTotalTaxPaidInNok {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act
            BigDecimal tax = service.getTotalTaxPaidInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, tax);
        }

        @Test
        @DisplayName("Should return 30% of gain as tax for a single profitable sale in NOK")
        void returnsTaxOfSingleProfitableSale() {
            // Arrange — profitable NOK sale: gain=975, tax=975×0.30=292.5 NOK
            player.getTransactionArchive().add(profitableNokSale());

            // Act
            BigDecimal tax = service.getTotalTaxPaidInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("292.5"), tax);
        }

        @Test
        @DisplayName("Should return zero tax for a loss sale")
        void returnsZeroTaxForLossSale() {
            // Arrange — loss sale: tax=0 (no tax on losses)
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal tax = service.getTotalTaxPaidInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, tax);
        }

        @Test
        @DisplayName("Should sum tax across profitable and loss sales, including only profitable tax")
        void sumsTaxAcrossMixedSales() {
            // Arrange — profitable: tax=292.5; loss: tax=0; total=292.5 NOK
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal tax = service.getTotalTaxPaidInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("292.5"), tax);
        }

        @Test
        @DisplayName("Should convert USD tax to NOK for a USD sale")
        void convertsUsdTaxToNok() {
            // Arrange — profitable USD sale: tax=292.5 USD → 292.5×9.21=2693.925 NOK
            player.getTransactionArchive().add(profitableUsdSale());

            // Act
            BigDecimal tax = service.getTotalTaxPaidInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("2693.925"), tax);
        }
    }

    @Nested
    @DisplayName("getTotalSaleCommissionInNok()")
    class GetTotalSaleCommissionInNok {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act
            BigDecimal commission = service.getTotalSaleCommissionInNok(player, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, commission);
        }

        @Test
        @DisplayName("Should return 1% of gross as commission for a single sale in NOK")
        void returnsOnePercentGrossAsCommission() {
            // Arrange — profitable NOK sale: gross=2000, commission=2000×0.01=20 NOK
            player.getTransactionArchive().add(profitableNokSale());

            // Act
            BigDecimal commission = service.getTotalSaleCommissionInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("20"), commission);
        }

        @Test
        @DisplayName("Should sum commission across both profitable and loss sales")
        void sumsCommissionAcrossMixedSales() {
            // Arrange — profitable: commission=20; loss: commission=5; total=25 NOK
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(lossNokSale());

            // Act
            BigDecimal commission = service.getTotalSaleCommissionInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("25"), commission);
        }

        @Test
        @DisplayName("Should convert USD commission to NOK for a USD sale")
        void convertsUsdCommissionToNok() {
            // Arrange — profitable USD sale: commission=20 USD → 20×9.21=184.2 NOK
            player.getTransactionArchive().add(profitableUsdSale());

            // Act
            BigDecimal commission = service.getTotalSaleCommissionInNok(player, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("184.2"), commission);
        }
    }

    @Nested
    @DisplayName("getSalesCount()")
    class GetSalesCount {

        @Test
        @DisplayName("Should return zero when the archive is empty")
        void returnsZeroForEmptyArchive() {
            // Act & Assert
            assertEquals(0, service.getSalesCount(player));
        }

        @Test
        @DisplayName("Should return one for a single sale in the archive")
        void returnsOneForSingleSale() {
            // Arrange
            player.getTransactionArchive().add(profitableNokSale());

            // Act & Assert
            assertEquals(1, service.getSalesCount(player));
        }

        @Test
        @DisplayName("Should count multiple sales correctly")
        void countsMultipleSalesCorrectly() {
            // Arrange — one profitable and one loss sale
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(lossNokSale());

            // Act & Assert
            assertEquals(2, service.getSalesCount(player));
        }

        @Test
        @DisplayName("Should not count purchases towards the sales count")
        void doesNotCountPurchases() {
            // Arrange — one sale and one purchase; only the sale should count
            player.getTransactionArchive().add(profitableNokSale());
            player.getTransactionArchive().add(
                    new Purchase(share(nokStock("100"), "5", "100"), 1));

            // Act & Assert
            assertEquals(1, service.getSalesCount(player));
        }
    }
}
