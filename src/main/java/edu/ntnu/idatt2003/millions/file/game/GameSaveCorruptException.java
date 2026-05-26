// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.file.LocalizedException;
import java.util.Arrays;

/**
 * Thrown when a game save file cannot be deserialized because its content does not
 * conform to the expected JSON schema — for example, a required field is missing,
 * has the wrong type, or the file is truncated.
 *
 * <p>This is a recoverable failure.
 *
 * <p>The exception carries an opaque {@link #i18nKey} and structured {@link #args}
 * (typically the filename) rather than a pre-resolved string, so the message can
 * be rendered in the user's current language at display time.
 *
 * <p>{@link #getMessage()} returns a fallback string intended for logging and
 * debugging only — it is not the user-facing message.
 *
 * <p>Implements {@link LocalizedException} so callers can catch it alongside other
 * i18n-aware exceptions in a single multi-catch block.
 */
public class GameSaveCorruptException extends Exception implements LocalizedException {

    private final String i18nKey;
    private final transient Object[] args;

    /**
     * Creates an exception with an i18n key and structured arguments but no chained cause.
     *
     * @param i18nKey the resource-bundle key identifying the error message template
     * @param args    the format arguments (e.g. the filename); may be null or empty
     */
    public GameSaveCorruptException(String i18nKey, Object[] args) {
        super(formatFallback(i18nKey, args));
        this.i18nKey = i18nKey;
        this.args = args != null ? args.clone() : new Object[0];
    }

    /**
     * Creates an exception with an i18n key, structured arguments, and a chained cause.
     *
     * @param i18nKey the resource-bundle key identifying the error message template
     * @param args    the format arguments (e.g. the filename); may be null or empty
     * @param cause   the lower-level exception that triggered this one
     */
    public GameSaveCorruptException(String i18nKey, Object[] args, Throwable cause) {
        super(formatFallback(i18nKey, args), cause);
        this.i18nKey = i18nKey;
        this.args = args != null ? args.clone() : new Object[0];
    }

    /**
     * Returns the resource-bundle key for the error message template.
     *
     * @return the i18n key
     */
    public String getI18nKey() {
        return i18nKey;
    }

    /**
     * Returns a defensive copy of the format arguments for the message template.
     *
     * @return the format arguments; never null, may be empty
     */
    public Object[] getArgs() {
        return args.clone();
    }

    private static String formatFallback(String key, Object[] args) {
        return key + (args != null && args.length > 0 ? " " + Arrays.toString(args) : "");
    }
}
