// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.stock;

/**
 * Thrown when a CSV stock file contains no valid stock entries after parsing.
 *
 * <p>This is a specialisation of {@link InvalidStockDataException} for file-level
 * failures, as opposed to line-level failures. It is thrown when the file is either
 * completely empty or contains only blank lines and comment lines (starting with
 * {@code #}), leaving no stock data to load into the exchange.
 *
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
