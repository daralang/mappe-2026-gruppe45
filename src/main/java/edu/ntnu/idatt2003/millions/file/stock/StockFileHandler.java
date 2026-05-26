// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Currency;
import java.util.List;

/**
 * Interface for reading and writing stock data to and from files.
 *
 * <p>Reading is exposed for both file paths and arbitrary input streams,
 * which lets callers parse classpath resources or in-memory data without
 * routing them through the filesystem.</p>
 *
 * <p>All read methods throw {@link InvalidStockDataException} when the file
 * contains a line that cannot be parsed. The entire parse is aborted on the
 * first bad line; the exception carries the line number and raw content so
 * the caller can report a precise error to the user.
 */
public interface StockFileHandler {

    /**
     * Reads stock data from the file at the given path. Stocks are tagged
     * with the implementation's default currency.
     *
     * @param path the path to the file to read from
     * @return a list of stocks parsed from the file
     * @throws InvalidStockDataException if any data line cannot be parsed
     */
    List<Stock> readStocks(Path path) throws InvalidStockDataException;

    /**
     * Reads stock data from the file at the given path and tags each stock
     * with the supplied currency.
     *
     * @param path     the path to the file to read from
     * @param currency the currency to assign to every parsed stock
     * @return a list of stocks parsed from the file
     * @throws InvalidStockDataException if any data line cannot be parsed
     */
    List<Stock> readStocks(Path path, Currency currency) throws InvalidStockDataException;

    /**
     * Reads stock data from the given input stream. The caller retains
     * ownership of the stream and is responsible for closing it. Stocks
     * are tagged with the implementation's default currency.
     *
     * @param inputStream the input stream to read from
     * @return a list of stocks parsed from the stream
     * @throws InvalidStockDataException if any data line cannot be parsed
     */
    List<Stock> readStocks(InputStream inputStream) throws InvalidStockDataException;

    /**
     * Reads stock data from the given input stream and tags each stock
     * with the supplied currency. The caller retains ownership of the
     * stream and is responsible for closing it.
     *
     * @param inputStream the input stream to read from
     * @param currency    the currency to assign to every parsed stock
     * @return a list of stocks parsed from the stream
     * @throws InvalidStockDataException if any data line cannot be parsed
     */
    List<Stock> readStocks(InputStream inputStream, Currency currency) throws InvalidStockDataException;

    /**
     * Writes stock data to the file at the given path.
     *
     * @param stocks the list of stocks to write
     * @param path   the path to the file to write to
     */
    void writeStocks(List<Stock> stocks, Path path);
}
