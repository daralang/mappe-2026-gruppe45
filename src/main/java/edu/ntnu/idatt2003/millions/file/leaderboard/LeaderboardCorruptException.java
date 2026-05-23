package edu.ntnu.idatt2003.millions.file.leaderboard;

/**
 * Thrown when a leaderboard file cannot be deserialized because its content does
 * not conform to the expected JSON schema — for example, the file contains invalid
 * JSON or an unexpected structure.
 *
 * <p>This is a recoverable failure. Callers should catch it, treat the leaderboard
 * as empty or unavailable, and log the problem rather than crashing. The rest of
 * the game must continue normally even if the leaderboard is unreadable.
 *
 * <p>Note: this exception covers content-level corruption only. File-system failures
 * (unreadable file, missing permissions) are reported as {@link IllegalStateException}
 * by the reader. See {@link JsonLeaderboardFileHandler} for the exact split.
 */
public class LeaderboardCorruptException extends Exception {

    /**
     * Creates an exception with a descriptive message identifying what was corrupt.
     *
     * @param message a human-readable description of the parse failure
     */
    public LeaderboardCorruptException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a descriptive message and a chained cause.
     * Use this constructor when wrapping a lower-level exception such as a
     * {@link com.google.gson.JsonParseException}.
     *
     * @param message a human-readable description of the parse failure
     * @param cause   the lower-level exception that triggered this one
     */
    public LeaderboardCorruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
