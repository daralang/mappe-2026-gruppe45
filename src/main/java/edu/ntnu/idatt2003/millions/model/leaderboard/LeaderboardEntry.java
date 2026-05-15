package edu.ntnu.idatt2003.millions.model.leaderboard;

import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of one game session's standing on the leaderboard.
 *
 * <p>Each entry is tied to a single game session via {@code sessionId}, which is
 * generated when a {@code Player} is constructed and preserved across save and
 * load. The same session therefore updates the same entry, regardless of how
 * many times the player saves.</p>
 *
 * <p>{@code returnPercent} is frozen at construction so sorting is consistent
 * even if exchange rates or domain logic change later. This mirrors how
 * {@link edu.ntnu.idatt2003.millions.model.transaction.Sale} freezes its
 * financial values.</p>
 *
 * @param sessionId       UUID tying the entry to one game session; never reused
 * @param playerName      the player's display name (need not be unique)
 * @param startingCapital the capital the player started with, in NOK
 * @param finalNetWorth   the player's net worth at the time of the snapshot, in NOK
 * @param returnPercent   {@code (finalNetWorth - startingCapital) / startingCapital × 100},
 *                        rounded to one decimal
 * @param weeksPlayed     the exchange week number when the snapshot was taken
 * @param status          the player's status level at snapshot time
 * @param outcome         the current state: {@link Outcome#ACTIVE}, {@link Outcome#RETIRED},
 *                        or {@link Outcome#BANKRUPTCY}
 * @param lastUpdated     instant the entry was last written
 */
public record LeaderboardEntry(
        String sessionId,
        String playerName,
        BigDecimal startingCapital,
        BigDecimal finalNetWorth,
        BigDecimal returnPercent,
        int weeksPlayed,
        PlayerStatusLevel status,
        Outcome outcome,
        Instant lastUpdated
) {

    public LeaderboardEntry {
        Objects.requireNonNull(sessionId, "sessionId cannot be null");
        Objects.requireNonNull(playerName, "playerName cannot be null");
        Objects.requireNonNull(startingCapital, "startingCapital cannot be null");
        Objects.requireNonNull(finalNetWorth, "finalNetWorth cannot be null");
        Objects.requireNonNull(returnPercent, "returnPercent cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(outcome, "outcome cannot be null");
        Objects.requireNonNull(lastUpdated, "lastUpdated cannot be null");

        if (sessionId.isBlank()) throw new IllegalArgumentException("sessionId cannot be blank");
        if (playerName.isBlank()) throw new IllegalArgumentException("playerName cannot be blank");
        if (weeksPlayed < 1) throw new IllegalArgumentException("weeksPlayed must be at least 1");
    }
}