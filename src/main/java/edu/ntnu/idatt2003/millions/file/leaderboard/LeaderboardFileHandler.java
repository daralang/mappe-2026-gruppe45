package edu.ntnu.idatt2003.millions.file.leaderboard;

import edu.ntnu.idatt2003.millions.model.leaderboard.LeaderboardEntry;

import java.io.File;
import java.util.List;

/**
 * Reads and writes the application leaderboard to and from a file.
 * Implementations may support different formats, such as JSON.
 *
 * <p>Implementations must tolerate a missing file on read — that is the normal
 * state before the first entry is recorded — and return an empty list rather
 * than throwing. Malformed content, however, should surface as an
 * {@link IllegalStateException} so the caller can decide how to react.</p>
 */
public interface LeaderboardFileHandler {

    /**
     * Reads all entries from the leaderboard file.
     *
     * @param file the file to read from
     * @return a list of entries; empty if the file does not exist or contains no entries
     * @throws NullPointerException  if file is null
     * @throws IllegalStateException if the file exists but cannot be parsed
     */
    List<LeaderboardEntry> readAll(File file);

    /**
     * Writes all entries to the leaderboard file, replacing any existing content.
     * The file is created if it does not exist.
     *
     * @param entries the entries to persist
     * @param file    the file to write to
     * @throws NullPointerException if entries or file is null
     */
    void writeAll(List<LeaderboardEntry> entries, File file);
}