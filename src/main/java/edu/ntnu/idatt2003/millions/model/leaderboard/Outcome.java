package edu.ntnu.idatt2003.millions.model.leaderboard;

/**
 * Final state of a leaderboard entry's underlying game session.
 *
 * <ul>
 *   <li>{@link #ACTIVE} — the game is still in progress. Updated on every save.</li>
 *   <li>{@link #RETIRED} — the player chose "Sell all and exit". Portfolio was
 *       liquidated voluntarily and the final result was recorded.</li>
 *   <li>{@link #BANKRUPTCY} — the player was declared bankrupt after failing a
 *       forced sale. The entry reflects the player's state at that point.</li>
 * </ul>
 *
 * <p>The leaderboard never locks — any outcome can be overwritten by a later
 * save or end-game event. The most recent write wins.</p>
 */
public enum Outcome {
    ACTIVE,
    RETIRED,
    BANKRUPTCY
}
