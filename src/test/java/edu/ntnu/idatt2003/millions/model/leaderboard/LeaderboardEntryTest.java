package edu.ntnu.idatt2003.millions.model.leaderboard;

import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeaderboardEntryTest {

    // ---- Positive tests ----

    @Test
    void constructsWithValidArguments() {
        LeaderboardEntry entry = validEntry();

        assertEquals("session-1", entry.sessionId());
        assertEquals("Alice", entry.playerName());
        assertEquals(new BigDecimal("10000"), entry.startingCapital());
        assertEquals(new BigDecimal("12000"), entry.finalNetWorth());
        assertEquals(new BigDecimal("20.0"), entry.returnPercent());
        assertEquals(5, entry.weeksPlayed());
        assertEquals(PlayerStatusLevel.NOVICE, entry.status());
        assertEquals(Outcome.ACTIVE, entry.outcome());
    }

    @Test
    void recordEqualsBasedOnAllFields() {
        Instant now = Instant.parse("2026-01-01T12:00:00Z");
        LeaderboardEntry a = new LeaderboardEntry("s", "n", BigDecimal.ONE, BigDecimal.TEN,
                BigDecimal.valueOf(10), 1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, now);
        LeaderboardEntry b = new LeaderboardEntry("s", "n", BigDecimal.ONE, BigDecimal.TEN,
                BigDecimal.valueOf(10), 1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    // ---- Negative tests: null arguments ----

    @Test
    void rejectsNullSessionId() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                null, "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullPlayerName() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullStartingCapital() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", null, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullFinalNetWorth() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, null, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullReturnPercent() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, null,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullStatus() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, null, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNullOutcome() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, null, Instant.now()));
    }

    @Test
    void rejectsNullLastUpdated() {
        assertThrows(NullPointerException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, null));
    }

    // ---- Negative tests: invalid values ----

    @Test
    void rejectsBlankSessionId() {
        assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry(
                "   ", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsBlankPlayerName() {
        assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry(
                "s", "   ", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsWeeksPlayedLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                0, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void rejectsNegativeWeeksPlayed() {
        assertThrows(IllegalArgumentException.class, () -> new LeaderboardEntry(
                "s", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                -1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, Instant.now()));
    }

    @Test
    void acceptsNegativeReturnPercent() {
        // Bankruptcies have negative returns — this should not throw.
        LeaderboardEntry entry = new LeaderboardEntry(
                "s", "Alice", new BigDecimal("10000"), new BigDecimal("4000"),
                new BigDecimal("-60.0"), 12, PlayerStatusLevel.NOVICE,
                Outcome.GAME_OVER, Instant.now());

        assertEquals(new BigDecimal("-60.0"), entry.returnPercent());
    }

    // ---- Helpers ----

    private LeaderboardEntry validEntry() {
        return new LeaderboardEntry(
                "session-1",
                "Alice",
                new BigDecimal("10000"),
                new BigDecimal("12000"),
                new BigDecimal("20.0"),
                5,
                PlayerStatusLevel.NOVICE,
                Outcome.ACTIVE,
                Instant.parse("2026-01-01T12:00:00Z")
        );
    }
}