package edu.ntnu.idatt2003.millions.file.leaderboard;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonLeaderboardFileHandlerTest {

    private JsonLeaderboardFileHandler handler;
    private File leaderboardFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new JsonLeaderboardFileHandler();
        leaderboardFile = tempDir.resolve("leaderboard.json").toFile();
    }

    // ---- readAll ----

    @Test
    void readAllReturnsEmptyListWhenFileDoesNotExist() {
        assertFalse(leaderboardFile.exists());

        List<LeaderboardEntry> entries = handler.readAll(leaderboardFile);

        assertNotNull(entries);
        assertTrue(entries.isEmpty());
    }

    @Test
    void readAllReturnsEmptyListWhenFileIsEmptyJsonArray() {
        // Arrange
        // Act
        List<LeaderboardEntry> entries = handler.parse(new StringReader("[]"));
        // Assert
        assertTrue(entries.isEmpty());
    }

    @Test
    void readAllThrowsForCorruptFile() {
        // Arrange
        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> handler.parse(new StringReader("{not valid json")));
    }

    @Test
    void readAllRejectsNullFile() {
        assertThrows(NullPointerException.class, () -> handler.readAll(null));
    }

    // ---- writeAll ----

    @Test
    void writeAllRejectsNullEntries() {
        assertThrows(NullPointerException.class,
                () -> handler.writeAll(null, leaderboardFile));
    }

    @Test
    void writeAllRejectsNullFile() {
        assertThrows(NullPointerException.class,
                () -> handler.writeAll(List.of(), null));
    }

    @Test
    void writeAllCreatesFileIfMissing() {
        assertFalse(leaderboardFile.exists());

        handler.writeAll(List.of(sample("s-1", "Alice", "10.0")), leaderboardFile);

        assertTrue(leaderboardFile.exists());
    }

    @Test
    void writeAllCreatesParentDirectoriesIfMissing() {
        File nested = tempDir.resolve("subdir/leaderboard.json").toFile();
        assertFalse(nested.getParentFile().exists());

        handler.writeAll(List.of(sample("s-1", "Alice", "10.0")), nested);

        assertTrue(nested.exists());
        assertTrue(nested.getParentFile().isDirectory());
    }

    @Test
    void writeAllOverwritesExistingContent() {
        handler.writeAll(List.of(sample("s-1", "Alice", "10.0")), leaderboardFile);
        handler.writeAll(List.of(sample("s-2", "Bob", "20.0")), leaderboardFile);

        List<LeaderboardEntry> entries = handler.readAll(leaderboardFile);

        assertEquals(1, entries.size());
        assertEquals("Bob", entries.get(0).playerName());
    }

    @Test
    void writeAllDoesNotLeaveTempFileBehind() {
        handler.writeAll(List.of(sample("s-1", "Alice", "10.0")), leaderboardFile);

        File tmp = new File(leaderboardFile.getAbsolutePath() + ".tmp");
        assertFalse(tmp.exists(),
                "Temp file should be moved over the target, not left behind");
    }

    // ---- round-trip ----

    @Test
    void roundTripPreservesAllFields() {
        LeaderboardEntry original = sample("s-roundtrip", "Charlie", "42.5");
        handler.writeAll(List.of(original), leaderboardFile);

        List<LeaderboardEntry> read = handler.readAll(leaderboardFile);

        assertEquals(1, read.size());
        LeaderboardEntry restored = read.get(0);
        assertEquals(original.sessionId(), restored.sessionId());
        assertEquals(original.playerName(), restored.playerName());
        assertEquals(original.startingCapital(), restored.startingCapital());
        assertEquals(original.finalNetWorth(), restored.finalNetWorth());
        assertEquals(original.returnPercent(), restored.returnPercent());
        assertEquals(original.weeksPlayed(), restored.weeksPlayed());
        assertEquals(original.status(), restored.status());
        assertEquals(original.outcome(), restored.outcome());
        assertEquals(original.lastUpdated(), restored.lastUpdated());
    }

    @Test
    void roundTripPreservesMultipleEntries() {
        List<LeaderboardEntry> written = List.of(
                sample("s-1", "Alice", "10.0"),
                sample("s-2", "Bob", "20.0"),
                sample("s-3", "Charlie", "30.0")
        );
        handler.writeAll(written, leaderboardFile);

        List<LeaderboardEntry> read = handler.readAll(leaderboardFile);

        assertEquals(3, read.size());
        assertEquals("Alice", read.get(0).playerName());
        assertEquals("Bob", read.get(1).playerName());
        assertEquals("Charlie", read.get(2).playerName());
    }

    @Test
    void roundTripPreservesInstantPrecision() {
        Instant instant = Instant.parse("2026-05-14T10:30:45.123456789Z");
        LeaderboardEntry original = new LeaderboardEntry(
                "s-time", "Alice", BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO,
                1, PlayerStatusLevel.NOVICE, Outcome.ACTIVE, instant);
        handler.writeAll(List.of(original), leaderboardFile);

        LeaderboardEntry restored = handler.readAll(leaderboardFile).get(0);

        assertEquals(instant, restored.lastUpdated());
    }

    // ---- Helpers ----

    private LeaderboardEntry sample(String sessionId, String name, String returnPct) {
        return new LeaderboardEntry(
                sessionId, name,
                new BigDecimal("10000"),
                new BigDecimal("12000"),
                new BigDecimal(returnPct),
                5,
                PlayerStatusLevel.NOVICE,
                Outcome.ACTIVE,
                Instant.parse("2026-01-01T12:00:00Z")
        );
    }
}