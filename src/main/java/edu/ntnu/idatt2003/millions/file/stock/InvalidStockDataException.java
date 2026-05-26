// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.util.language.LocalizedException;
import java.util.Arrays;

/**
 * Thrown when a CSV stock file contains a line that cannot be parsed. Triggering
 * conditions include:
 * <ul>
 *   <li>A data line does not have exactly the expected number of fields
 *       (wrong column count).</li>
 *   <li>A required field (symbol or name) is blank.</li>
 *   <li>The price field is non-numeric or non-positive (zero or negative).</li>
 * </ul>
 *
 * <p>Blank lines and lines beginning with {@code #} are skipped silently and
 * never trigger this exception.
 *
 * <p>The exception carries an opaque {@link #getI18nKey() i18nKey} and structured
 * {@link #getArgs() args} (typically line number and raw line content) rather than
 * a pre-resolved string.
 *
 * <p>{@link #getMessage()} returns a fallback string intended for logging and
 * debugging only — it is not the user-facing message.
 *
 * <p>{@link EmptyStockFileException} is a subclass for the case where the file
 * contains no valid stock entries at all.
 *
 * <p>Implements {@link LocalizedException} so callers can catch it alongside other
 * i18n-aware exceptions in a single multi-catch block.
 */
public class InvalidStockDataException extends Exception implements LocalizedException {

    private final String i18nKey;
    private final transient Object[] args;

    /**
     * Creates an exception with an i18n key and structured arguments but no chained cause.
     *
     * @param i18nKey the resource-bundle key identifying the error message template
     * @param args    the format arguments (e.g. line number and raw content); may be null or empty
     */
    public InvalidStockDataException(String i18nKey, Object[] args) {
        super(formatFallback(i18nKey, args));
        this.i18nKey = i18nKey;
        this.args = args != null ? args.clone() : new Object[0];
    }

    /**
     * Creates an exception with an i18n key, structured arguments, and a chained cause.
     *
     * @param i18nKey the resource-bundle key identifying the error message template
     * @param args    the format arguments (e.g. line number and raw content); may be null or empty
     * @param cause   the lower-level exception that triggered this one; may be null
     */
    public InvalidStockDataException(String i18nKey, Object[] args, Throwable cause) {
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
