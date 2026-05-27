package edu.ntnu.idatt2003.millions.service.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PlayerStatsService}.
 * Covers all seven public methods — net worth, change since start, percent change since start,
 * weekly change, weekly percent change, status, and progress to next status.
 * All tests follow the AAA pattern.
 */
class PlayerStatsServiceTest {

    private PlayerStatsService service;
    private CurrencyConverter converter;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        service = new PlayerStatsService();
        converter = new FixedRateCurrencyConverter();
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
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
        @DisplayName("Novice with 0 weeks and 0% growth → 0/20 = 0.0")
        void noviceZeroWeeksZeroGrowth() {
            // Arrange
            Player p = playerWith("1000");
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksPart=min(10,0)=0, growthPart=min(10,floor(0/2))=0; (0+0)/20=0.0
            assertEquals(0.0, progress);
        }

        @Test
        @DisplayName("Novice with 5 weeks and 0% growth → 5/20 = 0.25")
        void noviceFiveWeeksZeroGrowth() {
            // Arrange
            Player p = playerWith("1000");
            addWeeks(p, 5);
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksPart=min(10,5)=5, growthPart=0; (5+0)/20=0.25
            assertEquals(0.25, progress);
        }

        @Test
        @DisplayName("Novice with 0 weeks and 10% growth (5 full 2% steps) → 5/20 = 0.25")
        void noviceZeroWeeksTenPercentGrowth() {
            // Arrange — start=1000, add 100 → net worth 1100, growth 10%
            Player p = playerWith("1000");
            p.addMoney(new BigDecimal("100"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksPart=0, growthPart=min(10,floor(10/2))=min(10,5)=5; (0+5)/20=0.25
            assertEquals(0.25, progress);
        }

        @Test
        @DisplayName("Novice with 5 weeks and 11.9% growth → (5+5)/20 = 0.5 (discrete: 11.9% is 5 steps)")
        void noviceFiveWeeksElevenPointNinePercentGrowth() {
            // Arrange — start=1000, add 119 → net worth 1119, growth 11.9%
            Player p = playerWith("1000");
            addWeeks(p, 5);
            p.addMoney(new BigDecimal("119"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — growthPart=floor(11.9/2)=floor(5.95)=5; (5+5)/20=0.5
            assertEquals(0.5, progress);
        }

        @Test
        @DisplayName("Novice with 10 weeks and 19.9% growth → (10+9)/20 = 0.95 (not yet Investor)")
        void noviceTenWeeksNineteenPointNinePercentGrowth() {
            // Arrange — start=1000, add 199 → net worth 1199 < 1200 threshold → NOVICE
            Player p = playerWith("1000");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("199"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — growthPart=floor(19.9/2)=floor(9.95)=9; (10+9)/20=0.95
            assertEquals(0.95, progress);
        }

        @Test
        @DisplayName("Novice with 10 weeks and 20% growth → INVESTOR, new bar starts at 0.0")
        void investorJustReachedShowsZeroProgressOnNewBar() {
            // Arrange — start=1000, add 200 → net worth 1200 >= 1200 and 10 >= 10 → INVESTOR
            Player p = playerWith("1000");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("200"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — INVESTOR bar: weeksPart=min(10,max(0,10-10))=0, growthPart=min(40,max(0,floor((20-20)/2)))=0;
            //          (0+0)/50=0.0
            assertEquals(0.0, progress);
        }

        @Test
        @DisplayName("Investor with 15 weeks and 50% growth → 20/50 = 0.4")
        void investorFifteenWeeksFiftyPercentGrowth() {
            // Arrange — start=1000, add 500 → net worth 1500 >= 1200 and 15 >= 10 → INVESTOR
            Player p = playerWith("1000");
            addWeeks(p, 15);
            p.addMoney(new BigDecimal("500"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksPart=min(10,15-10)=5, growthPart=min(40,floor((50-20)/2))=min(40,15)=15;
            //          (5+15)/50=0.4
            assertEquals(0.4, progress);
        }

        @Test
        @DisplayName("Investor with 20 weeks and 100% growth → SPECULATOR, returns 1.0")
        void speculatorAlwaysReturnsOne() {
            // Arrange — start=1000, add 1000 → net worth 2000 >= 2000 and 20 >= 20 → SPECULATOR
            Player p = playerWith("1000");
            addWeeks(p, 20);
            p.addMoney(new BigDecimal("1000"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert
            assertEquals(1.0, progress);
        }

        @Test
        @DisplayName("Degradation: 15 trade-weeks but only 10% growth → NOVICE, (10+5)/20 = 0.75")
        void degradationInvestorWeeksButLowGrowthReverts() {
            // Arrange — start=1000, 15 weeks traded, only 10% growth: 1100 < 1200 threshold → NOVICE
            Player p = playerWith("1000");
            addWeeks(p, 15);
            p.addMoney(new BigDecimal("100"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — NOVICE bar: weeksPart=min(10,15)=10, growthPart=min(10,floor(10/2))=5;
            //          (10+5)/20=0.75
            assertEquals(0.75, progress);
        }

        @Test
        @DisplayName("Novice with 5 weeks and -10% growth → (5+0)/20 = 0.25 (negative growth doesn't reduce week-part)")
        void noviceFiveWeeksNegativeGrowthWeeksPartUnaffected() {
            // Arrange — start=1000, withdraw 100 → net worth 900, growth -10%
            Player p = playerWith("1000");
            addWeeks(p, 5);
            p.withdrawMoney(new BigDecimal("100"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weeksPart=5, growthPart=max(0,floor(-10/2))=max(0,-5)=0; (5+0)/20=0.25
            assertEquals(0.25, progress);
        }
    }

    @Nested
    @DisplayName("getNetWorth()")
    class GetNetWorth {

        @Test
        @DisplayName("Returns money balance when portfolio is empty")
        void returnsMoneyBalanceWithEmptyPortfolio() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("1000"), service.getNetWorth(p, converter));
        }
    }

    @Nested
    @DisplayName("getNetWorthChangeSinceStart()")
    class GetNetWorthChangeSinceStart {

        @Test
        @DisplayName("Returns zero for a player whose balance is unchanged")
        void returnsZeroForUnchangedBalance() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO, service.getNetWorthChangeSinceStart(p, converter));
        }

        @Test
        @DisplayName("Returns positive value when player gained money")
        void returnsPositiveValueWhenPlayerGainedMoney() {
            // Arrange — start=1000, add 200 → change=200
            Player p = playerWith("1000");
            p.addMoney(new BigDecimal("200"));
            assertBigDecimalEquals(new BigDecimal("200"), service.getNetWorthChangeSinceStart(p, converter));
        }

        @Test
        @DisplayName("Returns negative value when player lost money")
        void returnsNegativeValueWhenPlayerLostMoney() {
            // Arrange — start=1000, withdraw 300 → change=−300
            Player p = playerWith("1000");
            p.withdrawMoney(new BigDecimal("300"));
            assertBigDecimalEquals(new BigDecimal("-300"), service.getNetWorthChangeSinceStart(p, converter));
        }
    }

    @Nested
    @DisplayName("getNetWorthChangePercentSinceStart()")
    class GetNetWorthChangePercentSinceStart {

        @Test
        @DisplayName("Returns zero percent for a player whose balance is unchanged")
        void returnsZeroPercentForUnchangedBalance() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertBigDecimalEquals(BigDecimal.ZERO,
                    service.getNetWorthChangePercentSinceStart(p, converter));
        }

        @Test
        @DisplayName("Returns 10.0% when player gained 100 on a 1000 start")
        void returnsTenPercentOnTenPercentGain() {
            // Arrange — start=1000, add 100 → 100/1000×100 = 10.0%
            Player p = playerWith("1000");
            p.addMoney(new BigDecimal("100"));
            assertBigDecimalEquals(new BigDecimal("10.0"),
                    service.getNetWorthChangePercentSinceStart(p, converter));
        }
    }

    @Nested
    @DisplayName("getWeeklyNetWorthChange()")
    class GetWeeklyNetWorthChange {

        @Test
        @DisplayName("Returns null when no week has been advanced yet")
        void returnsNullBeforeAnyWeekAdvance() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertNull(service.getWeeklyNetWorthChange(p, converter));
        }

        @Test
        @DisplayName("Returns difference between current and previous net worth after week advance")
        void returnsDifferenceAfterWeekAdvance() {
            // Arrange — snapshot previous=1000, add 100 → change=100
            Player p = playerWith("1000");
            p.setPreviousNetWorth(p.getNetWorth(converter));
            p.addMoney(new BigDecimal("100"));
            assertBigDecimalEquals(new BigDecimal("100"),
                    service.getWeeklyNetWorthChange(p, converter));
        }
    }

    @Nested
    @DisplayName("getWeeklyNetWorthChangePercent()")
    class GetWeeklyNetWorthChangePercent {

        @Test
        @DisplayName("Returns null when no week has been advanced yet")
        void returnsNullBeforeAnyWeekAdvance() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertNull(service.getWeeklyNetWorthChangePercent(p, converter));
        }

        @Test
        @DisplayName("Returns 10.0% when player gained 100 on a 1000 previous net worth")
        void returnsPercentageChangeAfterWeekAdvance() {
            // Arrange — previous=1000, add 100 → 100/1000×100 = 10.0%
            Player p = playerWith("1000");
            p.setPreviousNetWorth(p.getNetWorth(converter));
            p.addMoney(new BigDecimal("100"));
            assertBigDecimalEquals(new BigDecimal("10.0"),
                    service.getWeeklyNetWorthChangePercent(p, converter));
        }
    }

    @Nested
    @DisplayName("getNetWorthHistory()")
    class GetNetWorthHistory {

        @Test
        @DisplayName("Fresh player history contains only the seed entry (starting money)")
        void freshPlayerHasSeedEntry() {
            // Arrange
            Player p = playerWith("1000");
            // Act
            List<BigDecimal> history = service.getNetWorthHistory(p);
            // Assert
            assertEquals(1, history.size());
            assertBigDecimalEquals(new BigDecimal("1000"), history.get(0));
        }

        @Test
        @DisplayName("History reflects recorded entries in insertion order")
        void historyReflectsRecordedEntriesInOrder() {
            // Arrange — seed=1000, record after gaining 200 then 100 more
            Player p = playerWith("1000");
            p.addMoney(new BigDecimal("200"));
            p.recordNetWorth(converter);
            p.addMoney(new BigDecimal("100"));
            p.recordNetWorth(converter);
            // Act
            List<BigDecimal> history = service.getNetWorthHistory(p);
            // Assert — [1000, 1200, 1300]
            assertEquals(3, history.size());
            assertBigDecimalEquals(new BigDecimal("1000"), history.get(0));
            assertBigDecimalEquals(new BigDecimal("1200"), history.get(1));
            assertBigDecimalEquals(new BigDecimal("1300"), history.get(2));
        }

        @Test
        @DisplayName("Returned list is a defensive copy — mutating it does not affect player history")
        void returnedListIsDefensiveCopy() {
            // Arrange
            Player p = playerWith("1000");
            List<BigDecimal> history = service.getNetWorthHistory(p);
            // Act & Assert — list is unmodifiable
            assertThrows(UnsupportedOperationException.class,
                    () -> history.add(new BigDecimal("9999")));
            assertEquals(1, service.getNetWorthHistory(p).size());
        }
    }

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("Returns NOVICE for a fresh player with no growth")
        void returnsNoviceForFreshPlayer() {
            // Arrange
            Player p = playerWith("1000");
            // Act & Assert
            assertEquals(PlayerStatusLevel.NOVICE, service.getStatus(p, converter));
        }

        @Test
        @DisplayName("Returns SPECULATOR for a player with 20 weeks and 100% growth")
        void returnsSpeculatorForTopTierPlayer() {
            // Arrange — 20 weeks + 100% growth (1000 on 1000 start) → SPECULATOR
            Player p = playerWith("1000");
            addWeeks(p, 20);
            p.addMoney(new BigDecimal("1000"));
            // Act & Assert
            assertEquals(PlayerStatusLevel.SPECULATOR, service.getStatus(p, converter));
        }
    }
}
