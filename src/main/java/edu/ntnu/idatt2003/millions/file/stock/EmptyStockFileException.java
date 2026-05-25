package edu.ntnu.idatt2003.millions.file.stock;

/**
 * Thrown when a CSV stock file contains no valid stock entries after parsing.
 *
 * <p>This is a specialisation of {@link InvalidStockDataException} for file-level
 * failures, as opposed to line-level failures. It is thrown when the file is either
 * completely empty or contains only blank lines and comment lines (starting with
 * {@code #}), leaving no stock data to load into the exchange.
 *
 * <p>Callers that only need to know that the stock file is unusable can catch the
 * parent class {@link InvalidStockDataException}. Callers that want to distinguish
 * between a malformed line and a structurally empty file can catch this subclass
 * directly.
 */
public class EmptyStockFileException extends InvalidStockDataException {

  /**
   * Creates an exception reporting that the given file contained no valid stock entries.
   *
   * @param filePath the path or name of the stock file that was empty
   */
  public EmptyStockFileException(String filePath) {
    super("error.stock.csv.emptyFile", new Object[]{filePath}, null);
  }
}
