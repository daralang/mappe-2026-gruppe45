package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for reading and writing stock data to and from files.
 * Implementations of this interface handle a specific file format,
 * making it easy to add support for new formats in the future.
 */
public interface StockFileHandler {

    /**
     * Reads stock data from the file at the given path.
     *
     * @param path the path to the file to read from
     * @return a list of stocks parsed from the file
     */
    List<Stock> readStocks(Path path);

    /**
     * Writes stock data to the file at the given path.
     *
     * @param stocks the list of stocks to write
     * @param path   the path to the file to write to
     */
    void writeStocks(List<Stock> stocks, Path path);
}