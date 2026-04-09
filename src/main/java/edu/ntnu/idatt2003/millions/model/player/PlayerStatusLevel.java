package edu.ntnu.idatt2003.millions.model.player;

/**
 * Represents the progression level of a player in the game.
 * The status is determined by the players net worth growth and trading activity.
 * They will be given these levels:
 * <ul>
 *     <li>Novice: basic start level</li>
 *     <li>Investor: players who have been trading for minimum 10 weeks and increased their
 *          net worth with at least 20%.</li>
 *     <li> Speculator: players who have been trading for minimum 20 weeks and have
 *          minimum doubled their net worth.</li>
 * </ul>
 */
public enum PlayerStatusLevel {
    NOVICE,
    INVESTOR,
    SPECULATOR;

    /**
     * Returns the i18n key for this status level.
     * Used to retrieve the localized display name from the resource bundle.
     *
     * @return the i18n key, e.g. "status.novice"
     */
    public String getI18nKey() {
        return "status." + name().toLowerCase();
    }
}
