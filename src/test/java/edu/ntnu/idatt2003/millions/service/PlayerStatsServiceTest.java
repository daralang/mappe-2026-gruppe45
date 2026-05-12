package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PlayerStatsService#getProgressToNextStatus}.
 */
class PlayerStatsServiceTest {

    private PlayerStatsService service;
    private CurrencyConverter converter;

    @BeforeEach
    void setUp() {
        service = new PlayerStatsService();
        converter = new FixedRateCurrencyConverter();
    }

    private static Player playerWith(String startingMoney) {
        return new Player("Test", new BigDecimal(startingMoney));
    }

    /**
     * Adds {@code weeks} distinct-week transactions to the player's archive.
     * Transactions are not committed so the player's money is unaffected.
     */
    private static void addWeeks(Player player, int weeks) {
        Stock stock = new Stock("TST", "Test Co.", List.of(BigDecimal.ONE));
        for (int week = 1; week <= weeks; week++) {
            Share share = new Share(stock, BigDecimal.ONE, BigDecimal.ONE);
            player.getTransactionArchive().add(new Purchase(share, week));
        }
    }

    @Nested
    @DisplayName("getProgressToNextStatus()")
    class GetProgressToNextStatus {

        @Test
        @DisplayName("Novice with 0 weeks and 0% growth returns 0.0")
        void noviceZeroWeeksZeroGrowth() {
            // Arrange
            Player p = playerWith("100");
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert
            assertEquals(0.0, progress);
        }

        @Test
        @DisplayName("Novice with 10 weeks and 10% growth returns 0.75")
        void noviceTenWeeksTenPercentGrowth() {
            // Arrange — 10 weeks, +10 money: growth = 10%, net worth 110 < 120 threshold → NOVICE
            Player p = playerWith("100");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("10"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksScore=1.0, growthScore=0.5, avg=0.75
            assertEquals(0.75, progress);
        }

        @Test
        @DisplayName("Novice with 10 weeks and 20% growth (rounded) returns 1.0")
        void noviceTenWeeksTwentyPercentGrowth() {
            // Arrange — addMoney(19.95): actual growth 19.95% rounds to 20.0%, but net worth 119.95 < 120 → NOVICE
            Player p = playerWith("100");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("19.95"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksScore=1.0, growthScore=1.0, avg=1.0
            assertEquals(1.0, progress);
        }

        @Test
        @DisplayName("Novice with 5 weeks and 30% growth returns 0.75 (growth capped at 1.0)")
        void noviceFiveWeeksThirtyPercentGrowthCapped() {
            // Arrange — 5 weeks (<10) so still NOVICE despite 30% growth exceeding 20% threshold
            Player p = playerWith("100");
            addWeeks(p, 5);
            p.addMoney(new BigDecimal("30"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksScore=0.5, growthScore=min(1.0,1.5)=1.0, avg=0.75
            assertEquals(0.75, progress);
        }

        @Test
        @DisplayName("Investor with 15 weeks and 50% growth returns 0.625")
        void investorFifteenWeeksFiftyPercentGrowth() {
            // Arrange — 15 weeks, +50% growth: net worth 150 >= 120 and 15 >= 10 → INVESTOR
            Player p = playerWith("100");
            addWeeks(p, 15);
            p.addMoney(new BigDecimal("50"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — INVESTOR thresholds: weeksScore=15/20=0.75, growthScore=50/100=0.5, avg=0.625
            assertEquals(0.625, progress);
        }

        @Test
        @DisplayName("Speculator returns 1.0 regardless of values")
        void speculatorAlwaysReturnsOne() {
            // Arrange — 20 weeks, +100% growth: net worth 200 = 200 threshold and 20 weeks → SPECULATOR
            Player p = playerWith("100");
            addWeeks(p, 20);
            p.addMoney(new BigDecimal("100"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert
            assertEquals(1.0, progress);
        }

        @Test
        @DisplayName("Negative growth does not produce a negative result")
        void negativeGrowthClampedToZero() {
            // Arrange — withdraw half starting money so net worth drops below start → negative growth
            Player p = playerWith("1000");
            p.withdrawMoney(new BigDecimal("500"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — growth sub-score must be clamped to 0, not go negative
            assertEquals(0.0, progress);
        }
    }
}
