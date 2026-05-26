package edu.ntnu.idatt2003.millions.service.leaderboard;

import edu.ntnu.idatt2003.millions.file.leaderboard.LeaderboardFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LeaderboardService}. Uses a fake {@link LeaderboardFileHandler}
 * so no real I/O happens. The Player and Exchange are constructed via their
 * real constructors and then mutated through reflection-free public APIs where
 * possible; sessionId is set via reflection because Player generates a fresh
 * UUID per construction and we need predictable IDs for these tests.
 */
class LeaderboardServiceTest {

    private FakeFileHandler fakeHandler;
    private LeaderboardService service;
    private CurrencyConverter identityConverter;

    @BeforeEach
    void setUp() {
        fakeHandler = new FakeFileHandler();
        service = new LeaderboardService(fakeHandler, new File("ignored"));
        identityConverter = (amount, from, to) -> amount;
    }

    // ---- Constructor validation ----

    @Test
    void constructorRejectsNullFileHandler() {
        assertThrows(NullPointerException.class,
                () -> new LeaderboardService(null, new File("ignored")));
    }

    @Test
    void constructorRejectsNullFile() {
        assertThrows(NullPointerException.class,
                () -> new LeaderboardService(fakeHandler, null));
    }

    // ---- recordOrUpdate: argument validation ----

    @Test
    void recordOrUpdateRejectsNullPlayer() {
        assertThrows(NullPointerException.class, () -> service.recordOrUpdate(
                null, exchange(1), identityConverter, Outcome.ACTIVE));
    }

    @Test
    void recordOrUpdateRejectsNullExchange() {
        assertThrows(NullPointerException.class, () -> service.recordOrUpdate(
                player("Alice", "s-1", "10000"), null, identityConverter, Outcome.ACTIVE));
    }

    @Test
    void recordOrUpdateRejectsNullConverter() {
        assertThrows(NullPointerException.class, () -> service.recordOrUpdate(
                player("Alice", "s-1", "10000"), exchange(1), null, Outcome.ACTIVE));
    }

    @Test
    void recordOrUpdateRejectsNullOutcome() {
        assertThrows(NullPointerException.class, () -> service.recordOrUpdate(
                player("Alice", "s-1", "10000"), exchange(1), identityConverter, null));
    }

    // ---- recordOrUpdate: upsert semantics ----

    @Test
    void recordCreatesNewEntryWhenSessionUnknown() {
        Player p = player("Alice", "s-1", "10000");

        service.recordOrUpdate(p, exchange(5), identityConverter, Outcome.ACTIVE);

        assertEquals(1, fakeHandler.entries.size());
        LeaderboardEntry stored = fakeHandler.entries.get(0);
        assertEquals("s-1", stored.sessionId());
        assertEquals("Alice", stored.playerName());
        assertEquals(5, stored.weeksPlayed());
        assertEquals(Outcome.ACTIVE, stored.outcome());
    }

    @Test
    void recordUpdatesExistingEntryWhenSessionMatches() {
        Player p = player("Alice", "s-1", "10000");
        service.recordOrUpdate(p, exchange(5), identityConverter, Outcome.ACTIVE);

        // Pretend the player's net worth grew — update should overwrite, not append.
        p.addMoney(new BigDecimal("5000"));
        service.recordOrUpdate(p, exchange(10), identityConverter, Outcome.ACTIVE);

        assertEquals(1, fakeHandler.entries.size(),
                "Same session must not produce two entries");
        assertEquals(10, fakeHandler.entries.get(0).weeksPlayed());
    }

    @Test
    void bankruptcyEntryCanBeOverwrittenByLaterSave() {
        Player p = player("Bob", "s-2", "10000");
        service.recordOrUpdate(p, exchange(20), identityConverter, Outcome.BANKRUPTCY);

        assertEquals(Outcome.BANKRUPTCY, fakeHandler.entries.get(0).outcome());

        // Player reloads the save and makes progress — the leaderboard must update.
        p.addMoney(new BigDecimal("5000"));
        service.recordOrUpdate(p, exchange(25), identityConverter, Outcome.ACTIVE);

        assertEquals(1, fakeHandler.entries.size(), "same session must not produce two entries");
        assertEquals(Outcome.ACTIVE, fakeHandler.entries.get(0).outcome());
        assertEquals(25, fakeHandler.entries.get(0).weeksPlayed());
    }

    @Test
    void sameNameDifferentSessionGivesTwoEntries() {
        Player game1 = player("Alice", "s-game1", "10000");
        Player game2 = player("Alice", "s-game2", "10000");

        service.recordOrUpdate(game1, exchange(5), identityConverter, Outcome.ACTIVE);
        service.recordOrUpdate(game2, exchange(5), identityConverter, Outcome.ACTIVE);

        assertEquals(2, fakeHandler.entries.size(),
                "Different sessionIds with same name are independent entries");
    }

    // ---- recordOrUpdate: robustness ----

    @Test
    void writeFailureIsSwallowedRatherThanPropagated() {
        fakeHandler.failOnWrite = true;
        Player p = player("Alice", "s-1", "10000");

        // Must not throw — leaderboard failures are non-fatal.
        service.recordOrUpdate(p, exchange(5), identityConverter, Outcome.ACTIVE);
    }

    @Test
    void readFailureDuringRecordIsSwallowed() {
        fakeHandler.failOnRead = true;
        Player p = player("Alice", "s-1", "10000");

        // Must not throw.
        service.recordOrUpdate(p, exchange(5), identityConverter, Outcome.ACTIVE);
    }

    // ---- getAllEntries & getTopEntries: sorting ----

    @Test
    void getAllEntriesSortsByReturnPercentDescending() {
        addEntry(service, "Alice",   "s-A", "11000");   // +10%
        addEntry(service, "Bob",     "s-B", "15000");   // +50%
        addEntry(service, "Charlie", "s-C", "8000");    // -20%

        List<LeaderboardEntry> sorted = service.getAllEntries();

        assertEquals("Bob",     sorted.get(0).playerName());
        assertEquals("Alice",   sorted.get(1).playerName());
        assertEquals("Charlie", sorted.get(2).playerName());
    }

    @Test
    void getAllEntriesUsesNetWorthAsTiebreaker() {
        // Both at +10% return, but different starting capital:
        // Alice: 10000 → 11000 (+10%)
        // Bob:   20000 → 22000 (+10%)
        // Same percent, Bob has higher absolute net worth → Bob first.
        addEntry(service, "Alice", "s-A", "10000", "11000");
        addEntry(service, "Bob",   "s-B", "20000", "22000");

        List<LeaderboardEntry> sorted = service.getAllEntries();

        assertEquals("Bob",   sorted.get(0).playerName());
        assertEquals("Alice", sorted.get(1).playerName());
    }

    @Test
    void getAllEntriesReturnsEmptyForEmptyLeaderboard() {
        assertTrue(service.getAllEntries().isEmpty());
    }

    @Test
    void getAllEntriesReturnsEmptyWhenReadFails() {
        fakeHandler.failOnRead = true;

        // Must not throw — read errors are swallowed.
        List<LeaderboardEntry> entries = service.getAllEntries();

        assertTrue(entries.isEmpty());
    }

    @Test
    void getTopEntriesLimitsResults() {
        // Each player gets a distinct net worth: Player0 = 10000 (0% return),
        // Player1 = 11000 (10%), ..., Player9 = 19000 (90%). Sorting descending
        // by return percent should put Player9 first.
        for (int i = 0; i < 10; i++) {
            String finalNetWorth = String.valueOf(10000 + i * 1000);
            addEntry(service, "Player" + i, "s-" + i, finalNetWorth);
        }

        List<LeaderboardEntry> top3 = service.getTopEntries(3);

        assertEquals(3, top3.size());
        assertEquals("Player9", top3.get(0).playerName());
        assertEquals("Player8", top3.get(1).playerName());
        assertEquals("Player7", top3.get(2).playerName());
    }

    @Test
    void getTopEntriesRejectsNonPositiveLimit() {
        assertThrows(IllegalArgumentException.class, () -> service.getTopEntries(0));
        assertThrows(IllegalArgumentException.class, () -> service.getTopEntries(-1));
    }

    @Test
    void getTopEntriesReturnsAllWhenLimitExceedsSize() {
        addEntry(service, "Alice", "s-1", "11000");
        addEntry(service, "Bob",   "s-2", "12000");

        List<LeaderboardEntry> top = service.getTopEntries(100);

        assertEquals(2, top.size());
    }

    // ---- Helpers ----

    /**
     * Records an entry with default starting capital 10000 and the given
     * final net worth. The service computes returnPercent from
     * (netWorth - startingCapital) / startingCapital.
     */
    private void addEntry(LeaderboardService svc, String name, String sessionId,
                          String finalNetWorth) {
        addEntry(svc, name, sessionId, "10000", finalNetWorth);
    }

    /**
     * Records an entry with explicit starting capital and final net worth.
     * Lets tests construct scenarios where two players have the same return
     * percent but different absolute net worth — needed for tiebreaker tests.
     */
    private void addEntry(LeaderboardService svc, String name, String sessionId,
                          String startingCapital, String finalNetWorth) {
        BigDecimal start = new BigDecimal(startingCapital);
        Player p = player(name, sessionId, start.toPlainString());

        BigDecimal target = new BigDecimal(finalNetWorth);
        if (target.compareTo(start) > 0) {
            p.addMoney(target.subtract(start));
        } else if (target.compareTo(start) < 0) {
            p.withdrawMoney(start.subtract(target));
        }

        svc.recordOrUpdate(p, exchange(5), identityConverter, Outcome.ACTIVE);
    }

    private Player player(String name, String sessionId, String startingMoney) {
        Player p = new Player(name, new BigDecimal(startingMoney));
        setSessionId(p, sessionId);
        return p;
    }

    /**
     * Forces a predictable sessionId on a freshly constructed Player.
     * Player generates a random UUID, which is undesirable in tests where we
     * need to assert upsert behaviour by session.
     */
    private void setSessionId(Player p, String sessionId) {
        try {
            Field f = Player.class.getDeclaredField("sessionId");
            f.setAccessible(true);
            f.set(p, sessionId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Could not set sessionId — has the field been renamed?", e);
        }
    }

    /**
     * Builds a real Exchange with a single stock so its constructor invariants
     * are satisfied, then advances it to the desired week.
     */
    private Exchange exchange(int week) {
        Stock dummy = new Stock("X", "X Corp",
                new ArrayList<>(List.of(new BigDecimal("100"))),
                Currency.getInstance("NOK"));
        Exchange ex = new Exchange("Test", List.of(dummy), identityConverter);
        for (int i = 1; i < week; i++) ex.advance();
        return ex;
    }

    // ---- Fake file handler ----

    /**
     * In-memory fake of {@link LeaderboardFileHandler} with configurable
     * failure modes for robustness tests.
     */
    static class FakeFileHandler implements LeaderboardFileHandler {
        List<LeaderboardEntry> entries = new ArrayList<>();
        boolean failOnRead = false;
        boolean failOnWrite = false;

        @Override
        public List<LeaderboardEntry> readAll(File file) {
            if (failOnRead) throw new IllegalStateException("simulated read failure");
            return new ArrayList<>(entries);
        }

        @Override
        public void writeAll(List<LeaderboardEntry> entries, File file) {
            if (failOnWrite) throw new UncheckedIOException(new IOException("simulated write failure"));
            this.entries = new ArrayList<>(entries);
        }
    }
}