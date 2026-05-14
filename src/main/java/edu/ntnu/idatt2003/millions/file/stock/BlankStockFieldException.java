package edu.ntnu.idatt2003.millions.file.stock;

/**
 * Thrown when a CSV stock entry contains a required field that is present but blank.
 *
 * <p>This is a specialisation of {@link InvalidStockDataException} that carries the
 * name of the offending field in addition to the line number and raw line content.
 * Callers that only need to know that a line is malformed can catch the parent class;
 * callers that want to display which field was blank can catch this subclass and call
 * {@link #getFieldName()}.
 *
 * <p>Example triggering conditions:
 * <ul>
 *   <li>The ticker/symbol column is empty or whitespace-only.</li>
 *   <li>The company name column is empty or whitespace-only.</li>
 * </ul>
 */
public class BlankStockFieldException extends InvalidStockDataException {

  private final String fieldName;

  /**
   * Creates an exception identifying the blank field by name, the line it occurred
   * on, and the raw content of that line.
   *
   * @param lineNumber  the 1-based line number of the malformed entry
   * @param lineContent the raw text of the malformed line
   * @param fieldName   the name of the field that was blank (e.g. {@code "symbol"} or
   *                    {@code "name"})
   */
  public BlankStockFieldException(int lineNumber, String lineContent, String fieldName) {
    super(lineNumber, lineContent);
    this.fieldName = fieldName;
  }

  /**
   * Returns a message that includes the field name, line number, and raw line content.
   *
   * @return a human-readable description of which field was blank and where
   */
  @Override
  public String getMessage() {
    return "Blank \"" + fieldName + "\" field at line " + getLineNumber()
        + ": \"" + getLineContent() + "\"";
  }

  /**
   * Returns the name of the field that was blank.
   *
   * @return the field name, e.g. {@code "symbol"} or {@code "name"}
   */
  public String getFieldName() {
    return fieldName;
  }
}
