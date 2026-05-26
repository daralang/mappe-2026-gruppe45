// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.leaderboard;

/**
 * Thrown when a leaderboard file cannot be deserialized because its content does
 * not conform to the expected JSON schema — for example, the file contains invalid
 * JSON or an unexpected structure.
 *
 * <p>This exception covers content-level corruption only. File-system failures
 * (unreadable file, missing permissions) are reported as {@link java.io.UncheckedIOException}.
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
     *
     * @param message a human-readable description of the parse failure
     * @param cause   the lower-level exception that triggered this one
     */
    public LeaderboardCorruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
