package edu.ntnu.idatt2003.millions.service;

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
 * Unit tests for {@link PortfolioService}.
 * Verifies per-share queries ({@code getShareValueInNok}, {@code getShareReturnInNok},
 * {@code getLiquidationValueInNok}) and aggregate portfolio queries including
 * {@code getInvestedAmount}, {@code getWeeklyReturnInNok}, {@code getWeeklyReturnPercent},
 * and {@code getPositionCount}.  All tests follow the AAA pattern.
 */
class PortfolioServiceTest {

    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");

    private PortfolioService service;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        service = new PortfolioService();
        converter = new FixedRateCurrencyConverter();
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    /**
     * Creates a NOK stock whose sales price is the last element of {@code prices}
     * and whose weekly price change is {@code prices[last] − prices[last-1]}.
     */
    private static Stock nokStock(String symbol, String... prices) {
        List<BigDecimal> priceList = java.util.Arrays.stream(prices)
                .map(BigDecimal::new)
                .toList();
        return new Stock(symbol, symbol + " Co.", priceList, NOK);
    }

    private static Stock usdStock(String symbol, String... prices) {
        List<BigDecimal> priceList = java.util.Arrays.stream(prices)
                .map(BigDecimal::new)
                .toList();
        return new Stock(symbol, symbol + " Co.", priceList, USD);
    }

    private static Share share(Stock stock, String qty, String purchasePrice) {
        return new Share(stock, new BigDecimal(qty), new BigDecimal(purchasePrice));
    }

    private static Player player(String balance) {
        return new Player("Test", new BigDecimal(balance));
    }

    @Nested
    @DisplayName("getShareValueInNok()")
    class GetShareValueInNok {

        @Test
        @DisplayName("Should return salesPrice times quantity in NOK")
        void returnsSalesPriceTimesQuantityInNok() {
            // Arrange - price=120, qty=10: currentValue=1200 NOK
            Stock stock = nokStock("TST", "80", "100", "120");
            Share sh = share(stock, "10", "80");

            // Act
            BigDecimal value = service.getShareValueInNok(sh, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1200"), value);
        }

        @Test
        @DisplayName("Should convert USD position value to NOK")
        void convertsUsdPositionValueToNok() {
            // Arrange - price=100 USD, qty=5: currentValue=500 USD → 500×9.21=4605 NOK
            Stock stock = usdStock("TST", "100");
            Share sh = share(stock, "5", "100");

            // Act
            BigDecimal value = service.getShareValueInNok(sh, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("4605"), value);
        }
    }

    @Nested
    @DisplayName("getShareReturnInNok()")
    class GetShareReturnInNok {

        @Test
        @DisplayName("Should return currentValue minus cost in NOK for a position with gains")
        void returnsCurrentValueMinusCostForGain() {
            // Arrange - price=120, qty=10, purchasePrice=80
            // returnNative = (120−80)×10 = 400 NOK
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");

            // Act
            BigDecimal ret = service.getShareReturnInNok(sh, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("400"), ret);
        }

        @Test
        @DisplayName("Should return a negative value in NOK when the position is at a loss")
        void returnsNegativeForLossPosition() {
            // Arrange - price=80, qty=10, purchasePrice=100
            // returnNative = (80−100)×10 = −200 NOK
            Stock stock = nokStock("TST", "100", "80");
            Share sh = share(stock, "10", "100");

            // Act
            BigDecimal ret = service.getShareReturnInNok(sh, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-200"), ret);
        }
    }

    @Nested
    @DisplayName("getLiquidationValueInNok()")
    class GetLiquidationValueInNok {

        @Test
        @DisplayName("Should return net payout after commission and tax in NOK")
        void returnsNetPayoutAfterFeesInNok() {
            // Arrange - stock: price=120, qty=10, purchasePrice=80
            // purchaseCost = 80×10 + 80×10×0.005 = 800+4 = 804
            // gross=1200; commission=12; gain=1200−12−804=384; tax=384×0.3=115.2
            // liquidation = 1200−12−115.2 = 1072.8 NOK
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");

            // Act
            BigDecimal lv = service.getLiquidationValueInNok(sh, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1072.8"), lv);
        }
    }

    @Nested
    @DisplayName("getValue()")
    class GetValue {

        @Test
        @DisplayName("Should return total portfolio market value in NOK")
        void returnsTotalPortfolioMarketValueInNok() {
            // Arrange - one NOK share: price=120, qty=10 → currentValue=1200 NOK
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal value = service.getValue(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("1200"), value);
        }

        @Test
        @DisplayName("Should return zero for an empty portfolio")
        void returnsZeroForEmptyPortfolio() {
            // Arrange
            Player p = player("5000");

            // Act
            BigDecimal value = service.getValue(p, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, value);
        }
    }

    @Nested
    @DisplayName("getTotalReturnInNok()")
    class GetTotalReturnInNok {

        @Test
        @DisplayName("Should return the sum of unrealized returns across all positions in NOK")
        void returnsSumOfUnrealizedReturnsInNok() {
            // Arrange - price=120, qty=10, purchasePrice=80: returnNative=400 NOK
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal ret = service.getTotalReturnInNok(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("400"), ret);
        }
    }

    @Nested
    @DisplayName("getTotalReturnPercent()")
    class GetTotalReturnPercent {

        @Test
        @DisplayName("Should return totalReturnInNok over totalCost times 100")
        void returnsTotalReturnOverCostTimes100() {
            // Arrange - returnNative=400, costNok=800 → percent=400/800×100=50.00
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal pct = service.getTotalReturnPercent(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("50.00"), pct);
        }

        @Test
        @DisplayName("Should return zero for an empty portfolio")
        void returnsZeroForEmptyPortfolio() {
            // Arrange
            Player p = player("5000");

            // Act
            BigDecimal pct = service.getTotalReturnPercent(p, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, pct);
        }
    }

    @Nested
    @DisplayName("getInvestedAmount()")
    class GetInvestedAmount {

        @Test
        @DisplayName("Should return market value minus total unrealized return in NOK")
        void returnsMarketValueMinusTotalReturn() {
            // Arrange - marketValue=1200, totalReturn=400; investedAmount=1200−400=800
            // This equals the position cost basis: 80×10=800
            Stock stock = nokStock("TST", "80", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal invested = service.getInvestedAmount(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("800"), invested);
        }

        @Test
        @DisplayName("Should return zero for an empty portfolio")
        void returnsZeroForEmptyPortfolio() {
            // Arrange
            Player p = player("5000");

            // Act
            BigDecimal invested = service.getInvestedAmount(p, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, invested);
        }
    }

    @Nested
    @DisplayName("getWeeklyReturnInNok()")
    class GetWeeklyReturnInNok {

        @Test
        @DisplayName("Should return sum of latestPriceChange times quantity in NOK")
        void returnsPriceChangeTimesQuantityInNok() {
            // Arrange - prices=[80,100,120]: priceChange=120−100=20; weeklyReturn=20×10=200 NOK
            Stock stock = nokStock("TST", "80", "100", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal weekly = service.getWeeklyReturnInNok(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("200"), weekly);
        }

        @Test
        @DisplayName("Should return zero when stock has only one price point")
        void returnsZeroForSinglePricePoint() {
            // Arrange - only one price means no previous → priceChange=0
            Stock stock = nokStock("TST", "100");
            Share sh = share(stock, "10", "100");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal weekly = service.getWeeklyReturnInNok(p, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, weekly);
        }

        @Test
        @DisplayName("Should return a negative value when price fell this week")
        void returnsNegativeWhenPriceFell() {
            // Arrange - prices=[200,150]: priceChange=150−200=−50; weeklyReturn=−50×4=−200 NOK
            Stock stock = nokStock("TST", "200", "150");
            Share sh = share(stock, "4", "200");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal weekly = service.getWeeklyReturnInNok(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("-200"), weekly);
        }

        @Test
        @DisplayName("Should sum weekly returns across multiple positions in NOK")
        void sumsWeeklyReturnsAcrossMultiplePositions() {
            // Arrange
            // Stock A: priceChange=+20, qty=10 → +200 NOK
            // Stock B: priceChange=−10, qty=5  → −50 NOK
            // Total = +150 NOK
            Stock stockA = nokStock("AAA", "80", "100", "120");
            Stock stockB = nokStock("BBB", "200", "190");
            Share shA = share(stockA, "10", "80");
            Share shB = share(stockB, "5", "200");
            Player p = player("5000");
            p.getPortfolio().addShare(shA);
            p.getPortfolio().addShare(shB);

            // Act
            BigDecimal weekly = service.getWeeklyReturnInNok(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("150"), weekly);
        }
    }

    @Nested
    @DisplayName("getWeeklyReturnPercent()")
    class GetWeeklyReturnPercent {

        @Test
        @DisplayName("Should return weeklyReturnInNok over investedAmount times 100")
        void returnsWeeklyReturnOverInvestedTimesOneHundred() {
            // Arrange - prices=[80,100,120]: priceChange=20, weeklyReturn=200 NOK
            // investedAmount = marketValue(1200)−totalReturn(400) = 800 NOK
            // weeklyPercent = 200/800×100 = 25.0000
            Stock stock = nokStock("TST", "80", "100", "120");
            Share sh = share(stock, "10", "80");
            Player p = player("5000");
            p.getPortfolio().addShare(sh);

            // Act
            BigDecimal pct = service.getWeeklyReturnPercent(p, converter);

            // Assert
            assertBigDecimalEquals(new BigDecimal("25"), pct);
        }

        @Test
        @DisplayName("Should return zero when portfolio is empty")
        void returnsZeroForEmptyPortfolio() {
            // Arrange - investedAmount=0 → divide-by-zero guard triggers
            Player p = player("5000");

            // Act
            BigDecimal pct = service.getWeeklyReturnPercent(p, converter);

            // Assert
            assertBigDecimalEquals(BigDecimal.ZERO, pct);
        }
    }

    @Nested
    @DisplayName("getPositionCount()")
    class GetPositionCount {

        @Test
        @DisplayName("Should return zero for an empty portfolio")
        void returnsZeroForEmptyPortfolio() {
            // Arrange
            Player p = player("5000");

            // Act & Assert
            assertEquals(0, service.getPositionCount(p));
        }

        @Test
        @DisplayName("Should return one for a single stock position")
        void returnsOneForSinglePosition() {
            // Arrange
            Stock stock = nokStock("TST", "100");
            Player p = player("5000");
            p.getPortfolio().addShare(share(stock, "10", "100"));

            // Act & Assert
            assertEquals(1, service.getPositionCount(p));
        }

        @Test
        @DisplayName("Should count each distinct stock symbol as one position")
        void countsDistinctSymbolsAsPositions() {
            // Arrange - two different stocks → 2 positions
            Stock s1 = nokStock("AAA", "100");
            Stock s2 = nokStock("BBB", "200");
            Player p = player("5000");
            p.getPortfolio().addShare(share(s1, "5", "100"));
            p.getPortfolio().addShare(share(s2, "5", "200"));

            // Act & Assert
            assertEquals(2, service.getPositionCount(p));
        }
    }
}
