// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.notification;

import java.util.List;

/**
 * An immutable notification displayed to the player during a game session.
 *
 * <p>Title and body are stored as i18n keys with optional interpolation arguments,
 * so the record carries no locale-specific text itself.</p>
 *
 * @param id       unique identifier assigned at creation
 * @param severity importance level of the notification
 * @param titleKey i18n key for the notification title
 * @param bodyKey  i18n key for the notification body template
 * @param bodyArgs arguments interpolated into the body template; empty if the template has none
 * @param week     the game week in which the notification was created
 * @param read     true if the player has acknowledged this notification
 */
public record Notification(
        int id,
        Severity severity,
        String titleKey,
        String bodyKey,
        List<String> bodyArgs,
        int week,
        boolean read
) {
    /**
     * Importance level of a {@link Notification}.
     *
     * <ul>
     *   <li>{@link #SEVERE}    — critical event requiring immediate attention (e.g. bankruptcy risk)</li>
     *   <li>{@link #WARNING}   — non-critical problem the player should be aware of</li>
     *   <li>{@link #MILESTONE} — positive achievement or progress update</li>
     *   <li>{@link #INFO}      — routine information with no action required</li>
     * </ul>
     */
    public enum Severity {
        SEVERE,
        WARNING,
        MILESTONE,
        INFO
    }

    /**
     * Returns a copy of this notification with {@code read} set to true.
     *
     * @return a new Notification identical to this one except marked as read
     */
    public Notification markAsRead() {
        return new Notification(id, severity, titleKey, bodyKey, bodyArgs, week, true);
    }
}
