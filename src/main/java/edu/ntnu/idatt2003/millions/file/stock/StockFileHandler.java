package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Currency;
import java.util.List;

/**
 * Interface for reading and writing stock data to and from files.
 * Implementations of this interface handle a specific file format,
 * making it easy to add support for new formats in the future.
 *
 * <p>Reading is exposed for both file paths and arbitrary input streams,
 * which lets callers parse classpath resources or in-memory data without
 * routing them through the filesystem.</p>
 */
public interface StockFileHandler {

    /**
     * Reads stock data from the file at the given path. Stocks are tagged
     * with the implementation's default currency.
     *
     * @param path the path to the file to read from
     * @return a list of stocks parsed from the file
     */
    List<Stock> readStocks(Path path);

    /**
     * Reads stock data from the file at the given path and tags each stock
     * with the supplied currency.
     *
     * @param path     the path to the file to read from
     * @param currency the currency to assign to every parsed stock
     * @return a list of stocks parsed from the file
     */
    List<Stock> readStocks(Path path, Currency currency);

    /**
     * Reads stock data from the given input stream. Useful for parsing
     * classpath resources or in-memory data without writing to disk.
     * The caller retains ownership of the stream and is responsible for
     * closing it. Stocks are tagged with the implementation's default
     * currency.
     *
     * @param inputStream the input stream to read from
     * @return a list of stocks parsed from the stream
     */
    List<Stock> readStocks(InputStream inputStream);

    /**
     * Reads stock data from the given input stream and tags each stock
     * with the supplied currency. The caller retains ownership of the
     * stream and is responsible for closing it.
     *
     * @param inputStream the input stream to read from
     * @param currency    the currency to assign to every parsed stock
     * @return a list of stocks parsed from the stream
     */
    List<Stock> readStocks(InputStream inputStream, Currency currency);

    /**
     * Writes stock data to the file at the given path.
     *
     * @param stocks the list of stocks to write
     * @param path   the path to the file to write to
     */
    void writeStocks(List<Stock> stocks, Path path);
}