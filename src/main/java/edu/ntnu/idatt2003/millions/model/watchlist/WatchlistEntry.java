package edu.ntnu.idatt2003.millions.model.watchlist;

import java.util.Objects;

/**
 * Represents a single entry in the player's watchlist.
 *
 * <p>Each entry is identified by a stock {@link #symbol}, records which
 * game week it was added and carries an optional
 * free-text. The record is immutable; use to produce an updated copy when the player edits
 * the note.</p>
 *
 * <p>Instances are serialised transparently by Gson as part of the player
 * state in the game save file.</p>
 */
public record WatchlistEntry(String symbol, int addedAtWeek, String note) {

    /**
     * Compact constructor that validates all fields.
     *
     * @param symbol      the stock ticker symbol; must not be null or blank
     * @param addedAtWeek the game week in which this entry was added; must be &gt;= 1
     * @param note        an optional free-text note; {@code null} is normalised to {@code ""}
     * @throws NullPointerException     if {@code symbol} is null
     * @throws IllegalArgumentException if {@code symbol} is blank or {@code addedAtWeek} is less than 1
     */
    public WatchlistEntry {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }
        if (addedAtWeek < 1) {
            throw new IllegalArgumentException("addedAtWeek must be >= 1");
        }
        note = (note == null) ? "" : note;
    }

    /**
     * Returns a new {@link WatchlistEntry} identical to this one except for the note.
     *
     * @param newNote the updated note text; {@code null} is normalised to {@code ""}
     * @return a new entry with the updated note
     */
    public WatchlistEntry withNote(String newNote) {
        return new WatchlistEntry(symbol, addedAtWeek, newNote == null ? "" : newNote);
    }
}
