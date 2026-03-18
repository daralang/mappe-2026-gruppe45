package edu.ntnu.idatt2003.millions.file;

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

public class CsvStockFileHandler implements StockFileHandler {

    private static final String DELIMITER = ",";
    private static final String COMMENT_PREFIX = "#";
    private static final int EXPECTED_FIELDS = 3;
    private static final int SYMBOL_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int PRICE_INDEX = 2;

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