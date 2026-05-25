package edu.ntnu.idatt2003.millions.service;

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
        @DisplayName("Novice with 10 weeks and 10% growth returns 0.5 (return is the bottleneck)")
        void noviceTenWeeksTenPercentGrowth() {
            // Arrange — 10 weeks, +10 money: growth = 10%, net worth 110 < 120 threshold → NOVICE
            Player p = playerWith("100");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("10"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weekProgress=(10-0)/10=1.0, returnProgress=(10-0)/20=0.5, min=0.5
            assertEquals(0.5, progress);
        }

        @Test
        @DisplayName("Novice with 10 weeks and 16% growth returns 0.8")
        void noviceTenWeeksSixteenPercentGrowth() {
            // Arrange — 10 weeks, +16 money: growth = 16%, net worth 116 < 120 → NOVICE
            Player p = playerWith("100");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("16"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weekProgress=1.0, returnProgress=(16-0)/20=0.8, min=0.8
            assertEquals(0.8, progress);
        }

        @Test
        @DisplayName("Novice with 5 weeks and 30% growth returns 0.5 (weeks are the bottleneck)")
        void noviceFiveWeeksThirtyPercentGrowthCapped() {
            // Arrange — 5 weeks (<10) so still NOVICE despite 30% growth exceeding 20% threshold
            Player p = playerWith("100");
            addWeeks(p, 5);
            p.addMoney(new BigDecimal("30"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weekProgress=(5-0)/10=0.5, returnProgress=min(1.0,30/20)=1.0, min=0.5
            assertEquals(0.5, progress);
        }

        @Test
        @DisplayName("Investor who just leveled up shows 0% progress toward Speculator")
        void investorJustLeveledUpShowsZeroProgress() {
            // Arrange — exactly 10 weeks and 20% growth: net worth 120 >= 120 and 10 >= 10 → INVESTOR
            Player p = playerWith("100");
            addWeeks(p, 10);
            p.addMoney(new BigDecimal("20"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weekProgress=(10-10)/10=0.0, returnProgress=(20-20)/80=0.0, min=0.0
            assertEquals(0.0, progress);
        }

        @Test
        @DisplayName("Investor with 15 weeks and 50% growth returns 0.375 (return is the bottleneck)")
        void investorFifteenWeeksFiftyPercentGrowth() {
            // Arrange — 15 weeks, +50% growth: net worth 150 >= 120 and 15 >= 10 → INVESTOR
            Player p = playerWith("100");
            addWeeks(p, 15);
            p.addMoney(new BigDecimal("50"));
            // Act
            double progress = service.getProgressToNextStatus(p, converter);
            // Assert — weekProgress=(15-10)/10=0.5, returnProgress=(50-20)/80=0.375, min=0.375
            assertEquals(0.375, progress);
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
