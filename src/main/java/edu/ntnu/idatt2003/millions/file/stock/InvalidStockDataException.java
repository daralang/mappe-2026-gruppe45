package edu.ntnu.idatt2003.millions.file.stock;

/**
 * Thrown when a CSV stock file contains a line that cannot be parsed. The two
 * triggering conditions are:
 * <ul>
 *   <li>A data line does not have exactly the expected number of fields
 *       (wrong column count).</li>
 *   <li>A numeric field (e.g. the price column) contains a non-numeric value.</li>
 * </ul>
 *
 * <p>Blank lines and lines beginning with {@code #} are skipped silently and
 * never trigger this exception.
 *
 * <p>Callers should report the {@link #getLineNumber() line number} and
 * {@link #getLineContent() raw content} to the user so they can locate and
 * correct the malformed entry in the file.
 */
public class InvalidStockDataException extends Exception {

    private final int lineNumber;
    private final String lineContent;

    /**
     * Creates an exception identifying the malformed line by its position in
     * the file and its raw content.
     *
     * @param lineNumber  the 1-based line number of the malformed entry
     * @param lineContent the raw text of the malformed line
     */
    public InvalidStockDataException(int lineNumber, String lineContent) {
        super("Invalid stock data at line " + lineNumber + ": \"" + lineContent + "\"");
        this.lineNumber = lineNumber;
        this.lineContent = lineContent;
    }

    /**
     * Creates an exception with a descriptive message and a chained cause.
     * Use this constructor when wrapping a lower-level exception such as a
     * {@link java.io.IOException}.
     *
     * @param message a human-readable description of the parse failure
     * @param cause   the lower-level exception that triggered this one
     */
    public InvalidStockDataException(String message, Throwable cause) {
        super(message, cause);
        this.lineNumber = -1;
        this.lineContent = null;
    }

    /** @return the 1-based line number of the malformed entry, or -1 if not set */
    public int getLineNumber() {
        return lineNumber;
    }

    /** @return the raw text of the malformed line, or null if not set */
    public String getLineContent() {
        return lineContent;
    }
}
