package edu.ntnu.idatt2003.millions.model.leaderboard;

/**
 * Final state of a leaderboard entry's underlying game session.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — the game is still in progress. The entry is updated on every
 *       subsequent save so the leaderboard reflects the player's current standing.</li>
 *   <li>{@link #GAME_OVER} — the game has ended (player declared bankrupt). The entry
 *       is locked: no further updates are written for this session.</li>
 * </ul>
 */
public enum Outcome {
    ACTIVE,
    GAME_OVER
}