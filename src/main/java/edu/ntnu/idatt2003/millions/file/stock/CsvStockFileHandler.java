package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.Stock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link StockFileHandler} for reading and writing
 * stock data to and from CSV files.
 *
 * <p>Each line in the file represents a stock on the form:
 * {@code symbol,name,price}. Lines starting with {@code #} and
 * blank lines are ignored.</p>
 */
public class CsvStockFileHandler implements StockFileHandler {

    /**
     * Delimiter used to separate fields in the CSV file.
     */
    private static final String DELIMITER = ",";

    /**
     * Prefix used to identify comment lines in the CSV file.
     */
    private static final String COMMENT_PREFIX = "#";

    /**
     * Expected number of fields per stock entry in the CSV file.
     */
    private static final int EXPECTED_FIELDS = 3;

    /**
     * Index of the stock symbol field in a CSV line.
     */
    private static final int SYMBOL_INDEX = 0;

    /**
     * Index of the stock name field in a CSV line.
     */
    private static final int NAME_INDEX = 1;

    /**
     * Index of the stock price field in a CSV line.
     */
    private static final int PRICE_INDEX = 2;

    /**
     * Reads stock data from a CSV file at the given path.
     * Lines starting with {@code #} and blank lines are skipped.
     * Lines with invalid format are also skipped.
     *
     * @param path the path to the CSV file to read from
     * @return a list of stocks parsed from the file
     * @throws NullPointerException if path is null
     * @throws UncheckedIOException if the file cannot be read
     */
    @Override
    public List<Stock> readStocks(Path path) {
        Objects.requireNonNull(path, "Path cannot be null");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return reader.lines()
                    .filter(line -> !line.isBlank() && !line.startsWith(COMMENT_PREFIX))
                    .map(line -> line.split(DELIMITER))
                    .filter(fields -> fields.length == EXPECTED_FIELDS)
                    .map(fields -> new Stock(
                            fields[SYMBOL_INDEX].trim(),
                            fields[NAME_INDEX].trim(),
                            new ArrayList<>(List.of(new BigDecimal(fields[PRICE_INDEX].trim())))))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file: " + path, e);
        }
    }

    /**
     * Writes stock data to a CSV file at the given path.
     * Each stock is written on the form {@code symbol,name,price},
     * where price is the current sales price.
     *
     * @param stocks the list of stocks to write
     * @param path   the path to the CSV file to write to
     * @throws NullPointerException if path or stocks is null
     * @throws UncheckedIOException if the file cannot be written to
     */
    @Override
    public void writeStocks(List<Stock> stocks, Path path) {
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(stocks, "Stocks cannot be null");

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (Stock stock : stocks) {
                writer.write(String.join(DELIMITER,
                        stock.getSymbol(),
                        stock.getCompany(),
                        stock.getSalesPrice().toString()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not write to file: " + path, e);
        }
    }
}