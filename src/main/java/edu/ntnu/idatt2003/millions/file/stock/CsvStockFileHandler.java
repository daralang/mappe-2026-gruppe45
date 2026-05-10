package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link StockFileHandler} for reading and writing
 * stock data to and from CSV files.
 *
 * <p>Each line in the file represents a stock on the form:
 * {@code symbol,name,price}. Lines starting with {@code #} and
 * blank lines are ignored.</p>
 *
 * <p>Both file paths and arbitrary input streams are supported as input,
 * which lets callers parse classpath resources without writing them to disk.</p>
 */
public class CsvStockFileHandler implements StockFileHandler {

    /**
     * Default currency assigned to parsed stocks when no currency is supplied
     * by the caller. Used by the no-currency overloads to preserve previous
     * behaviour.
     */
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("USD");

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
        return readStocks(path, DEFAULT_CURRENCY);
    }

    /**
     * Reads stock data from a CSV file at the given path and tags every
     * parsed stock with the supplied currency. Lines starting with {@code #}
     * and blank lines are skipped. Lines with invalid format are also skipped.
     *
     * @param path     the path to the CSV file to read from
     * @param currency the currency to assign to every parsed stock
     * @return a list of stocks parsed from the file
     * @throws NullPointerException if path or currency is null
     * @throws UncheckedIOException if the file cannot be read
     */
    @Override
    public List<Stock> readStocks(Path path, Currency currency) {
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return parse(reader, currency);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file: " + path, e);
        }
    }

    /**
     * Reads stock data from a CSV input stream. Lines starting with {@code #}
     * and blank lines are skipped. Lines with invalid format are also skipped.
     * The caller retains ownership of the stream and is responsible for
     * closing it.
     *
     * @param inputStream the input stream to read from
     * @return a list of stocks parsed from the stream
     * @throws NullPointerException if inputStream is null
     * @throws UncheckedIOException if the stream cannot be read
     */
    @Override
    public List<Stock> readStocks(InputStream inputStream) {
        return readStocks(inputStream, DEFAULT_CURRENCY);
    }

    /**
     * Reads stock data from a CSV input stream and tags every parsed stock
     * with the supplied currency. Lines starting with {@code #} and blank
     * lines are skipped. Lines with invalid format are also skipped. The
     * caller retains ownership of the stream and is responsible for closing it.
     *
     * @param inputStream the input stream to read from
     * @param currency    the currency to assign to every parsed stock
     * @return a list of stocks parsed from the stream
     * @throws NullPointerException if inputStream or currency is null
     */
    @Override
    public List<Stock> readStocks(InputStream inputStream, Currency currency) {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return parse(reader, currency);
    }

    /**
     * Parses CSV stock data from the given reader and tags every produced
     * stock with the supplied currency. Lines starting with {@code #} and
     * blank lines are skipped, as are lines that do not have the expected
     * number of fields.
     *
     * @param reader   the buffered reader to parse from
     * @param currency the currency to assign to every parsed stock
     * @return a list of stocks parsed from the reader
     */
    private List<Stock> parse(BufferedReader reader, Currency currency) {
        return reader.lines()
                .filter(line -> !line.isBlank() && !line.startsWith(COMMENT_PREFIX))
                .map(line -> line.split(DELIMITER))
                .filter(fields -> fields.length == EXPECTED_FIELDS)
                .map(fields -> new Stock(
                        fields[SYMBOL_INDEX].trim(),
                        fields[NAME_INDEX].trim(),
                        new ArrayList<>(List.of(new BigDecimal(fields[PRICE_INDEX].trim()))),
                        currency))
                .toList();
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