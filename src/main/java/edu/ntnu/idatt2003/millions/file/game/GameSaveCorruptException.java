package edu.ntnu.idatt2003.millions.file.game;

/**
 * Thrown when a game save file cannot be deserialized because its content does not
 * conform to the expected JSON schema — for example, a required field is missing,
 * has the wrong type, or the file is truncated.
 *
 * <p>This is a recoverable failure. Callers should catch it, inform the player that
 * the save file is corrupt, and offer to start a new game instead.
 *
 * <p>Note: this exception class is introduced here to give future save-file loading
 * logic a specific type to throw. The wiring into {@link JsonGameFileHandler} is
 * tracked separately.
 */
public class GameSaveCorruptException extends Exception {

    /**
     * Creates an exception with a descriptive message identifying what was corrupt.
     *
     * @param message a human-readable description of the schema violation
     */
    public GameSaveCorruptException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a descriptive message and a chained cause.
     * Use this constructor when wrapping a lower-level exception such as a
     * {@link com.google.gson.JsonSyntaxException}.
     *
     * @param message a human-readable description of the schema violation
     * @param cause   the lower-level exception that triggered this one
     */
    public GameSaveCorruptException(String message, Throwable cause) {
        super(message, cause);
    }
}
