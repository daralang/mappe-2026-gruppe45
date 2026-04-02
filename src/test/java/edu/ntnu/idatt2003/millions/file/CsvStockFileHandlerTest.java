package edu.ntnu.idatt2003.millions.file;

import edu.ntnu.idatt2003.millions.file.stock.CsvStockFileHandler;
import edu.ntnu.idatt2003.millions.model.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        @DisplayName("Should skip lines with invalid format when file contains invalid lines")
        void skipsInvalidLines() throws Exception {
            // Arrange
            Path file = tempDir.resolve("stocks.csv");
            Files.writeString(file, "AAPL,Apple Inc.,276.43\nINVALID_LINE\n");
            // Act
            List<Stock> stocks = handler.readStocks(file);
            // Assert
            assertEquals(1, stocks.size());
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
        void throwsExceptionWhenPathIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.readStocks(null));
        }

        @Test
        @DisplayName("Should throw exception when file does not exist")
        void throwsExceptionWhenFileDoesNotExist() {
            // Arrange
            Path file = tempDir.resolve("nonexistent.csv");
            // Act & Assert
            assertThrows(UncheckedIOException.class, () ->
                    handler.readStocks(file));
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
        void returnsSameNumberOfStocks() {
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
        void returnsSameSymbol() {
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
        void returnsSamePrice() {
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