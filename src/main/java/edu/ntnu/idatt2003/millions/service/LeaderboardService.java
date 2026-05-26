// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.file.leaderboard.JsonLeaderboardFileHandler;
import edu.ntnu.idatt2003.millions.file.leaderboard.LeaderboardCorruptException;
import edu.ntnu.idatt2003.millions.file.leaderboard.LeaderboardFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.model.player.Player;

import java.io.File;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service that owns the leaderboard's persistence and ranking rules.
 *
 * <p>Every game session is identified by {@link Player#getSessionId()}, which
 * stays stable across save and load. {@link #recordOrUpdate} therefore upserts:
 * if an entry exists for the same session it is updated in place, otherwise a
 * new entry is appended. The leaderboard never locks — the most recent write
 * always wins, regardless of the previous outcome.</p>
 *
 * <p>Primary ranking is by {@code returnPercent} descending, with
 * {@code finalNetWorth} as a tiebreaker. The {@link Outcome} flag lets the
 * view distinguish active, retired, and bankrupt sessions without affecting
 * ranking position.</p>
 *
 * <h2>File location</h2>
 * The default file is {@code leaderboard.json} in the working directory, which
 * is the project root when run from Maven or an IDE. The file is meant to be
 * committed to git so group members can compare scores asynchronously: pull
 * to see others' results, play, commit and push to share yours. The path is
 * configurable via the DI constructor so tests can use a temp file.
 */
@SuppressWarnings("ClassCanBeRecord")
public class LeaderboardService {

    private static final Logger LOGGER = Logger.getLogger(LeaderboardService.class.getName());

    /**
     * Default leaderboard file name. Relative to the working directory, which
     * is the project root when run from Maven or an IDE. If the file does not
     * exist, it is created on first write.
     */
    public static final String DEFAULT_FILE = "leaderboard.json";

    private final LeaderboardFileHandler fileHandler;
    private final File file;

    /**
     * Constructs a service using {@link JsonLeaderboardFileHandler} and the
     * default file path.
     */
    public LeaderboardService() {
        this(new JsonLeaderboardFileHandler(), new File(DEFAULT_FILE));
    }

    /**
     * Full dependency-injection constructor used by tests.
     *
     * @param fileHandler the persistence handler
     * @param file        the leaderboard file
     * @throws NullPointerException if either argument is null
     */
    public LeaderboardService(LeaderboardFileHandler fileHandler, File file) {
        this.fileHandler = Objects.requireNonNull(fileHandler, "fileHandler cannot be null");
        this.file = Objects.requireNonNull(file, "file cannot be null");
    }

    /**
     * Inserts or updates the leaderboard entry for the player's current session.
     *
     * <p>If an entry already exists for the same {@code sessionId} it is replaced
     * with a fresh snapshot; otherwise a new entry is appended. The leaderboard
     * never locks — a later save can overwrite a {@link Outcome#BANKRUPTCY} entry
     * with {@link Outcome#ACTIVE} if the player reloads and continues playing.</p>
     *
     * <p>Failures to read or write the leaderboard file are logged at WARNING
     * and swallowed rather than propagated. The leaderboard is a non-essential
     * auxiliary feature; a corrupt or unwriteable file should not crash an
     * in-progress save or game-over flow.</p>
     *
     * @param player    the active player
     * @param exchange  the active exchange (used for week number)
     * @param converter the converter used to compute net worth in NOK
     * @param outcome   the current outcome to record
     * @throws NullPointerException if any argument is null
     */
    public void recordOrUpdate(Player player, Exchange exchange,
                               CurrencyConverter converter, Outcome outcome) {
        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(exchange, "exchange cannot be null");
        Objects.requireNonNull(converter, "converter cannot be null");
        Objects.requireNonNull(outcome, "outcome cannot be null");

        try {
            List<LeaderboardEntry> entries = readMutable();
            String sessionId = player.getSessionId();

            int existingIndex = indexOfSession(entries, sessionId);
            LeaderboardEntry snapshot = snapshot(player, exchange, converter, outcome);
            if (existingIndex >= 0) {
                entries.set(existingIndex, snapshot);
            } else {
                entries.add(snapshot);
            }
            fileHandler.writeAll(entries, file);
        } catch (LeaderboardCorruptException | IllegalStateException | UncheckedIOException e) {
            LOGGER.log(Level.WARNING, "Could not update leaderboard", e);
        }
    }

    /**
     * Returns the top {@code limit} entries ranked by return percent descending.
     * Net worth in NOK is used as a secondary key so two equally well-performing
     * players are ordered by absolute outcome.
     *
     * @param limit maximum number of entries to return; must be positive
     * @return ranked entries, never null; empty if no entries exist yet
     * @throws IllegalArgumentException if limit is not positive
     */
    public List<LeaderboardEntry> getTopEntries(int limit) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be positive");
        return getAllEntries().stream()
                .limit(limit)
                .toList();
    }

    /**
     * Returns all leaderboard entries ranked by return percent descending, then
     * net worth descending. Returns an empty list if the leaderboard is empty
     * or its file does not exist yet. Read failures are logged and treated as
     * empty so a corrupt file does not break the view.
     *
     * @return ranked entries; never null
     */
    public List<LeaderboardEntry> getAllEntries() {
        List<LeaderboardEntry> entries;
        try {
            entries = readMutable();
        } catch (LeaderboardCorruptException | IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Could not read leaderboard", e);
            return List.of();
        }
        entries.sort(Comparator
                .comparing(LeaderboardEntry::returnPercent).reversed()
                .thenComparing(Comparator.comparing(LeaderboardEntry::finalNetWorth).reversed()));
        return entries;
    }

    private List<LeaderboardEntry> readMutable() throws LeaderboardCorruptException {
        return new ArrayList<>(fileHandler.readAll(file));
    }

    private int indexOfSession(List<LeaderboardEntry> entries, String sessionId) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).sessionId().equals(sessionId)) {
                return i;
            }
        }
        return -1;
    }

    private LeaderboardEntry snapshot(Player player, Exchange exchange,
                                      CurrencyConverter converter, Outcome outcome) {
        return new LeaderboardEntry(
                player.getSessionId(),
                player.getName(),
                player.getStartingMoney(),
                player.getNetWorth(converter),
                player.getNetWorthChangePercentSinceStart(converter),
                exchange.getWeek(),
                player.getStatus(converter),
                outcome,
                Instant.now()
        );
    }
}