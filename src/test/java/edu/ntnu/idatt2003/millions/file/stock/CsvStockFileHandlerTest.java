package edu.ntnu.idatt2003.millions.file.stock;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UncheckedIOException;
import edu.ntnu.idatt2003.millions.file.stock.InvalidStockDataException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CsvStockFileHandler} class.
 * <p>
 * This test class verifies the behaviour of the CsvStockFileHandler,
 * including reading and writing stock data to and from CSV files.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class CsvStockFileHandlerTest {

    private CsvStockFileHandler handler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new CsvStockFileHandler();
    }

    @Nested
    @DisplayName("readStocks()")
    class ReadStocks {

        @SuppressWarnings("java:S5976")
        @Test
        @DisplayName("Should return correct number of stocks when file is valid")
        void returnsCorrectNumberOfStocks() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals(2, stocks.size());
        }

        @Test
        @DisplayName("Should parse stock fields correctly when file is valid")
        void parsesStockFieldsCorrectly() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals("AAPL", stocks.getFirst().getSymbol());
            assertEquals("Apple Inc.", stocks.getFirst().getCompany());
            assertEquals(new BigDecimal("276.43"), stocks.getFirst().getSalesPrice());
        }

        @Test
        @DisplayName("Should skip comment lines when file contains comments")
        void skipsCommentLines() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "# This is a comment\nAAPL,Apple Inc.,276.43\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals(1, stocks.size());
        }

        @Test
        @DisplayName("Should skip blank lines when file contains blank lines")
        void skipsBlankLines() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "\nAAPL,Apple Inc.,276.43\n\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals(1, stocks.size());
        }

        @Test
        @DisplayName("Should throw InvalidStockDataException for wrong column count")
        void throwsForWrongColumnCount() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\nINVALID_LINE\n");
            // Act & Assert
            assertThrows(InvalidStockDataException.class, () -> handler.readStocks(file));
        }

        @Test
        @DisplayName("Should throw InvalidStockDataException for non-numeric price")
        void throwsForNonNumericPrice() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,abc\n");
            // Act & Assert
            assertThrows(InvalidStockDataException.class, () -> handler.readStocks(file));
        }

        @Test
        @DisplayName("Should report the correct line number when a data line is malformed")
        void reportsCorrectLineNumberOnMalformedLine() throws Exception {
            // Arrange — line 1 is a comment, line 2 is valid, line 3 is malformed
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "# comment\nAAPL,Apple Inc.,276.43\nBAD\n");
            // Act
            InvalidStockDataException ex = assertThrows(InvalidStockDataException.class,
                    () -> handler.readStocks(file));
            // Assert — line 3 in the file is the bad one
            assertEquals(3, ex.getLineNumber());
        }

        @Test
        @DisplayName("Should return empty list when file is empty")
        void returnsEmptyListWhenFileIsEmpty() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertTrue(stocks.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() throws InvalidStockDataException {
            // Arrange
            Path nullPath = null;
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks(nullPath));
        }

        @Test
        @DisplayName("Should throw exception when file does not exist")
        void throwsExceptionWhenFileDoesNotExist() throws InvalidStockDataException {
            // Arrange
            Path file = tempDir.resolve("nonexistent.csv");
            // Act & Assert
            assertThrows(UncheckedIOException.class, () ->
                    handler.readStocks(file));
        }

        @Test
        @DisplayName("Should default stock currency to USD when no currency is supplied")
        void defaultsCurrencyToUsd() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals(Currency.getInstance("USD"), stocks.getFirst().getCurrency());
        }
    }

    @Nested
    @DisplayName("readStocks(Path, Currency)")
    class ReadStocksWithCurrency {

        @Test
        @DisplayName("Should tag every parsed stock with the supplied currency")
        void tagsStocksWithSuppliedCurrency() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n");
            Currency eur = Currency.getInstance("EUR");
            // Act
            List<Stock> stocks = handler.readStocks(file, eur);
            // Assert
            assertEquals(2, stocks.size());
            assertTrue(stocks.stream().allMatch(stock -> eur.equals(stock.getCurrency())));
        }

        @Test
        @DisplayName("Should return empty list when file is empty")
        void returnsEmptyListWhenFileIsEmpty() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "");
            // Act
            List<Stock> stocks = handler.readStocks(file, Currency.getInstance("NOK"));
            // Assert
            assertTrue(stocks.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() throws InvalidStockDataException {
            // Arrange
            Currency usd = Currency.getInstance("USD");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks((Path) null, usd));
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void throwsExceptionWhenCurrencyIsNull() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\n");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks(file, null));
        }
    }

    @Nested
    @DisplayName("readStocks(InputStream)")
    class ReadStocksFromStream {

        @Test
        @DisplayName("Should parse stocks from input stream")
        void parsesStocksFromStream() throws InvalidStockDataException {
            // Arrange
            InputStream stream = new ByteArrayInputStream(
                    "AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n".getBytes(StandardCharsets.UTF_8));
            // Act
            List<Stock> stocks = handler.readStocks(stream);
            // Assert
            assertEquals(2, stocks.size());
            assertEquals("AAPL", stocks.getFirst().getSymbol());
        }

        @Test
        @DisplayName("Should default stock currency to USD when no currency is supplied")
        void defaultsCurrencyToUsd() throws InvalidStockDataException {
            // Arrange
            InputStream stream = new ByteArrayInputStream(
                    "AAPL,Apple Inc.,276.43\n".getBytes(StandardCharsets.UTF_8));
            // Act
            List<Stock> stocks = handler.readStocks(stream);
            // Assert
            assertEquals(Currency.getInstance("USD"), stocks.getFirst().getCurrency());
        }

        @Test
        @DisplayName("Should return empty list when stream is empty")
        void returnsEmptyListWhenStreamIsEmpty() throws InvalidStockDataException {
            // Arrange
            InputStream stream = new ByteArrayInputStream(new byte[0]);
            // Act
            List<Stock> stocks = handler.readStocks(stream);
            // Assert
            assertTrue(stocks.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when stream is null")
        void throwsExceptionWhenStreamIsNull() throws InvalidStockDataException {
            // Arrange
            InputStream nullStream = null;
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks(nullStream));
        }
    }

    @Nested
    @DisplayName("readStocks(InputStream, Currency)")
    class ReadStocksFromStreamWithCurrency {

        @Test
        @DisplayName("Should tag every parsed stock with the supplied currency")
        void tagsStocksWithSuppliedCurrency() throws InvalidStockDataException {
            // Arrange
            InputStream stream = new ByteArrayInputStream(
                    "AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n".getBytes(StandardCharsets.UTF_8));
            Currency gbp = Currency.getInstance("GBP");
            // Act
            List<Stock> stocks = handler.readStocks(stream, gbp);
            // Assert
            assertEquals(2, stocks.size());
            assertTrue(stocks.stream().allMatch(stock -> gbp.equals(stock.getCurrency())));
        }

        @Test
        @DisplayName("Should throw exception when stream is null")
        void throwsExceptionWhenStreamIsNull() throws InvalidStockDataException {
            // Arrange
            Currency usd = Currency.getInstance("USD");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks((InputStream) null, usd));
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void throwsExceptionWhenCurrencyIsNull() throws InvalidStockDataException {
            // Arrange
            InputStream stream = new ByteArrayInputStream(
                    "AAPL,Apple Inc.,276.43\n".getBytes(StandardCharsets.UTF_8));
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks(stream, null));
        }
    }

    @Nested
    @DisplayName("writeStocks()")
    class WriteStocks {

        @Test
        @DisplayName("Should write correct number of lines when stocks are valid")
        void writesCorrectNumberOfLines() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            List<Stock> stocks = List.of(
                    new Stock("AAPL", "Apple Inc.", new ArrayList<>(List.of(new BigDecimal("276.43")))),
                    new Stock("MSFT", "Microsoft", new ArrayList<>(List.of(new BigDecimal("404.68"))))
            );
            // Act
            handler.writeStocks(stocks, file);
            // Assert
            assertEquals(2, Files.readAllLines(file).size());
        }

        @Test
        @DisplayName("Should write correct format when stocks are valid")
        void writesCorrectFormat() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            List<Stock> stocks = List.of(
                    new Stock("AAPL", "Apple Inc.", new ArrayList<>(List.of(new BigDecimal("276.43"))))
            );
            // Act
            handler.writeStocks(stocks, file);
            // Assert
            assertEquals("AAPL,Apple Inc.,276.43", Files.readAllLines(file).getFirst());
        }

        @Test
        @DisplayName("Should write empty file when stock list is empty")
        void writesEmptyFileWhenStockListIsEmpty() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            // Act
            handler.writeStocks(List.of(), file);
            // Assert
            assertTrue(Files.readAllLines(file).isEmpty());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() {
            // Arrange
            List<Stock> stocks = List.of();
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.writeStocks(stocks, null));
        }

        @Test
        @DisplayName("Should throw exception when stocks is null")
        void throwsExceptionWhenStocksIsNull() {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.writeStocks(null, file));
        }
    }

    @Nested
    @DisplayName("writeStocks() and readStocks()")
    class RoundTrip {

        @Test
        @DisplayName("Should return same number of stocks after write and read")
        void returnsSameNumberOfStocks() throws InvalidStockDataException {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            List<Stock> original = List.of(
                    new Stock("AAPL", "Apple Inc.", new ArrayList<>(List.of(new BigDecimal("276.43")))),
                    new Stock("MSFT", "Microsoft", new ArrayList<>(List.of(new BigDecimal("404.68"))))
            );
            // Act
            handler.writeStocks(original, file);
            List<Stock> result = handler.readStocks(file);
            // Assert
            assertEquals(original.size(), result.size());
        }

        @Test
        @DisplayName("Should return same symbol after write and read")
        void returnsSameSymbol() throws InvalidStockDataException {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            List<Stock> original = List.of(
                    new Stock("AAPL", "Apple Inc.", new ArrayList<>(List.of(new BigDecimal("276.43"))))
            );
            // Act
            handler.writeStocks(original, file);
            List<Stock> result = handler.readStocks(file);
            // Assert
            assertEquals(original.getFirst().getSymbol(), result.getFirst().getSymbol());
        }

        @Test
        @DisplayName("Should return same price after write and read")
        void returnsSamePrice() throws InvalidStockDataException {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            List<Stock> original = List.of(
                    new Stock("AAPL", "Apple Inc.", new ArrayList<>(List.of(new BigDecimal("276.43"))))
            );
            // Act
            handler.writeStocks(original, file);
            List<Stock> result = handler.readStocks(file);
            // Assert
            assertEquals(0, original.getFirst().getSalesPrice()
                    .compareTo(result.getFirst().getSalesPrice()));
        }
    }
}