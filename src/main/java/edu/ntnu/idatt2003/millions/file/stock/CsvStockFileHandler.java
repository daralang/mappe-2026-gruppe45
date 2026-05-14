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
 *
 * <p>Any data line that fails validation causes the entire parse to abort with an
 * {@link InvalidStockDataException} identifying the offending line number and content.
 * Validation covers wrong field count, blank symbol or name, non-numeric price, and
 * non-positive price. If the file contains no valid stock entries at all, an
 * {@link EmptyStockFileException} is thrown instead.
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
     *
     * @param path the path to the CSV file to read from
     * @return a list of stocks parsed from the file
     * @throws NullPointerException      if path is null
     * @throws UncheckedIOException      if the file cannot be read
     * @throws InvalidStockDataException if any data line has the wrong column count
     *                                   or a non-numeric price field
     */
    @Override
    public List<Stock> readStocks(Path path) throws InvalidStockDataException {
        return readStocks(path, DEFAULT_CURRENCY);
    }

    /**
     * Reads stock data from a CSV file at the given path and tags every
     * parsed stock with the supplied currency. Lines starting with {@code #}
     * and blank lines are skipped.
     *
     * @param path     the path to the CSV file to read from
     * @param currency the currency to assign to every parsed stock
     * @return a non-empty list of stocks parsed from the file
     * @throws NullPointerException      if path or currency is null
     * @throws UncheckedIOException      if the file cannot be read
     * @throws EmptyStockFileException   if the file contains no valid stock entries
     * @throws InvalidStockDataException if any data line has the wrong column count,
     *                                   a blank symbol or name, or an invalid price
     */
    @Override
    public List<Stock> readStocks(Path path, Currency currency) throws InvalidStockDataException {
        Objects.requireNonNull(path, "Path cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            return parse(reader, currency, path.toString());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file: " + path, e);
        }
    }

    /**
     * Reads stock data from a CSV input stream. Lines starting with {@code #}
     * and blank lines are skipped. The caller retains ownership of the stream
     * and is responsible for closing it.
     *
     * @param inputStream the input stream to read from
     * @return a list of stocks parsed from the stream
     * @throws NullPointerException      if inputStream is null
     * @throws UncheckedIOException      if the stream cannot be read
     * @throws InvalidStockDataException if any data line has the wrong column count
     *                                   or a non-numeric price field
     */
    @Override
    public List<Stock> readStocks(InputStream inputStream) throws InvalidStockDataException {
        return readStocks(inputStream, DEFAULT_CURRENCY);
    }

    /**
     * Reads stock data from a CSV input stream and tags every parsed stock
     * with the supplied currency. Lines starting with {@code #} and blank
     * lines are skipped. The caller retains ownership of the stream and is
     * responsible for closing it.
     *
     * @param inputStream the input stream to read from
     * @param currency    the currency to assign to every parsed stock
     * @return a non-empty list of stocks parsed from the stream
     * @throws NullPointerException      if inputStream or currency is null
     * @throws UncheckedIOException      if the stream cannot be read
     * @throws EmptyStockFileException   if the stream contains no valid stock entries
     * @throws InvalidStockDataException if any data line has the wrong column count,
     *                                   a blank symbol or name, or an invalid price
     */
    @Override
    public List<Stock> readStocks(InputStream inputStream, Currency currency) throws InvalidStockDataException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return parse(reader, currency, "<stream>");
    }

    /**
     * Parses CSV stock data from the given reader and tags every produced stock with
     * the supplied currency. Lines starting with {@code #} and blank lines are skipped.
     *
     * <p>Each data line is validated in order:
     * <ol>
     *   <li>Exactly {@value #EXPECTED_FIELDS} comma-separated fields must be present.</li>
     *   <li>The symbol field must not be blank.</li>
     *   <li>The name field must not be blank.</li>
     *   <li>The price field must be a valid number.</li>
     *   <li>The price must be greater than zero.</li>
     * </ol>
     * Any failing line aborts the parse immediately. If no valid entries are found
     * after reading the entire input, an {@link EmptyStockFileException} is thrown.
     *
     * @param reader   the buffered reader to parse from
     * @param currency the currency to assign to every parsed stock
     * @param source   a human-readable identifier for the input (file path or
     *                 {@code "<stream>"}), used in the {@link EmptyStockFileException}
     *                 message when no entries are found
     * @return a non-empty list of stocks parsed from the reader
     * @throws EmptyStockFileException   if the input contains no valid stock entries
     * @throws InvalidStockDataException if any data line fails validation
     */
    private List<Stock> parse(BufferedReader reader, Currency currency, String source)
            throws InvalidStockDataException {
        List<Stock> result = new ArrayList<>();
        int lineNumber = 0;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith(COMMENT_PREFIX)) {
                    continue;
                }
                String[] fields = line.split(DELIMITER);
                if (fields.length != EXPECTED_FIELDS) {
                    throw new InvalidStockDataException(lineNumber, line.trim());
                }
                String symbol = fields[SYMBOL_INDEX].trim();
                if (symbol.isBlank()) {
                    throw new InvalidStockDataException(lineNumber, line.trim(), "blank symbol");
                }
                String name = fields[NAME_INDEX].trim();
                if (name.isBlank()) {
                    throw new InvalidStockDataException(lineNumber, line.trim(), "blank name");
                }
                String rawPrice = fields[PRICE_INDEX].trim();
                BigDecimal price;
                try {
                    price = new BigDecimal(rawPrice);
                } catch (NumberFormatException e) {
                    throw new InvalidStockDataException(
                            lineNumber, line.trim(), "non-numeric price \"" + rawPrice + "\"");
                }
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InvalidStockDataException(
                            lineNumber, line.trim(), "non-positive price \"" + rawPrice + "\"");
                }
                result.add(new Stock(symbol, name, new ArrayList<>(List.of(price)), currency));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read CSV data", e);
        }
        if (result.isEmpty()) {
            throw new EmptyStockFileException(source);
        }
        return result;
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
