package edu.ntnu.idatt2003.millions.controller.game;

import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.start.StartScreenInputs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StartController}.
 */
class StartControllerTest {

    @TempDir
    Path tempDir;

    private StubInputs inputs;
    private RecordingGameService gameService;
    private List<String> errors;
    private List<String> successes;
    private boolean showMainCalled;
    private StartController controller;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        inputs = new StubInputs();
        gameService = new RecordingGameService();
        errors = new ArrayList<>();
        successes = new ArrayList<>();
        showMainCalled = false;
        controller = new StartController(
                gameService,
                inputs,
                () -> showMainCalled = true,
                supplier -> errors.add(supplier.get()),
                supplier -> successes.add(supplier.get()));
    }

    @Nested
    @DisplayName("handleStartGame()")
    class HandleStartGame {

        @Test
        @DisplayName("Should create new game with default stocks when no stock file is selected")
        void createsNewGameWithDefaultStocksWhenNoFileSelected() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals("Dara", gameService.lastName);
            assertEquals(0, new BigDecimal("10000.00").compareTo(gameService.lastCapital));
            assertNull(gameService.lastStockFile);
            assertNull(gameService.lastCurrency);
            assertTrue(showMainCalled);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should create new game with custom stock file and selected currency")
        void createsNewGameWithCustomFileAndSelectedCurrency() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "/tmp/stocks.csv";
            inputs.selectedCurrency = Currency.getInstance("EUR");
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals("Dara", gameService.lastName);
            assertEquals(0, new BigDecimal("10000.00").compareTo(gameService.lastCapital));
            assertEquals(Path.of("/tmp/stocks.csv"), gameService.lastStockFile.toPath());
            assertEquals(Currency.getInstance("EUR"), gameService.lastCurrency);
            assertTrue(showMainCalled);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should report error and not navigate when player name is blank")
        void reportsErrorWhenPlayerNameIsBlank() {
            // Arrange
            inputs.name = "";
            inputs.capital = "10000.00";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when capital is invalid")
        void reportsErrorWhenCapitalIsInvalid() {
            // Arrange: NumberFormatException extends IllegalArgumentException
            // and is therefore caught and routed through the error sink.
            inputs.name = "Dara";
            inputs.capital = "not-a-number";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when stock file is not csv")
        void reportsErrorWhenStockFileIsNotCsv() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "/tmp/stocks.json";
            inputs.selectedCurrency = Currency.getInstance("USD");
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when currency is not selected")
        void reportsErrorWhenCurrencyIsNotSelected() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "/tmp/stocks.csv";
            inputs.selectedCurrency = null;
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager fails")
        void reportsErrorWhenGameServiceFails() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "";
            gameService.failNextCreate = new IllegalStateException("default stock data missing");
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals("default stock data missing", errors.getFirst());
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager throws UncheckedIOException")
        void reportsErrorWhenGameServiceThrowsIoException() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "";
            gameService.failNextCreate =
                    new UncheckedIOException("read failed", new java.io.IOException("disk error"));
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when capital is zero")
        void reportsErrorWhenCapitalIsZero() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "0";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when capital is negative")
        void reportsErrorWhenCapitalIsNegative() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "-500";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when player name is only whitespace")
        void reportsErrorWhenPlayerNameIsOnlyWhitespace() {
            // Arrange
            inputs.name = "   ";
            inputs.capital = "10000.00";
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should combine name and capital validation errors into a single message")
        void combinesNameAndCapitalErrorsIntoSingleMessage() {
            // Arrange: name too short AND capital negative
            inputs.name = "ab";
            inputs.capital = "-500";
            // Act
            controller.handleStartGame();
            // Assert: one combined message containing both error strings
            assertEquals(1, errors.size());
            String combined = errors.getFirst();
            assertTrue(combined.contains(LanguageManager.get("error.name.too.short")),
                    "Combined message should contain the name-too-short error");
            assertTrue(combined.contains(LanguageManager.get("error.capital.zero")),
                    "Combined message should contain the capital-zero error");
            assertEquals(0, gameService.createNewGameCalls);
            assertFalse(showMainCalled);
        }
    }

    @Nested
    @DisplayName("handleLoadGame()")
    class HandleLoadGame {

        @Test
        @DisplayName("Should load saved game and navigate")
        void loadsSavedGameAndNavigates() {
            // Arrange
            inputs.saveFilePath = "/tmp/save.json";
            // Act
            controller.handleLoadGame();
            // Assert
            assertEquals(Path.of("/tmp/save.json"), gameService.lastLoadedFile.toPath());
            assertTrue(showMainCalled);
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("Should report error and not navigate when save file is blank")
        void reportsErrorWhenSaveFileIsBlank() {
            // Arrange
            inputs.saveFilePath = "";
            // Act
            controller.handleLoadGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals(0, gameService.loadGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager fails to load")
        void reportsErrorWhenGameServiceFailsToLoad() {
            // Arrange
            inputs.saveFilePath = "/tmp/save.json";
            gameService.failNextLoad = new IllegalArgumentException("corrupt save file");
            // Act
            controller.handleLoadGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals("corrupt save file", errors.getFirst());
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager throws UncheckedIOException on load")
        void reportsErrorWhenGameServiceThrowsIoExceptionOnLoad() {
            // Arrange
            inputs.saveFilePath = "/tmp/save.json";
            gameService.failNextLoad =
                    new UncheckedIOException("read failed", new java.io.IOException("disk error"));
            // Act
            controller.handleLoadGame();
            // Assert
            assertEquals(1, errors.size());
            assertFalse(showMainCalled);
        }
    }

    @Nested
    @DisplayName("validateAndSetSaveFile()")
    class ValidateAndSetSaveFile {

        @Test
        @DisplayName("Should store file path and emit success when save file is valid")
        void storesFilePathWhenSaveFileIsValid() throws IOException {
            // Arrange
            Path saveFile = tempDir.resolve("save.json");
            Stock stock = new Stock("EQNR", "Equinor ASA",
                    new ArrayList<>(List.of(new BigDecimal("276.43"))),
                    Currency.getInstance("NOK"));
            Exchange exchange = new Exchange("Oslo Børs",
                    new ArrayList<>(List.of(stock)),
                    new FixedRateCurrencyConverter());
            Player player = new Player("Dara", new BigDecimal("10000.00"));
            new JsonGameFileHandler().saveGame(player, exchange, false, saveFile.toFile());
            // Act
            controller.validateAndSetSaveFile(saveFile.toFile());
            // Assert
            assertEquals(saveFile.toAbsolutePath().toString(), inputs.saveFilePath);
            assertTrue(errors.isEmpty());
            assertEquals(1, successes.size());
        }

        @Test
        @DisplayName("Should clear file path and report error when save file contains invalid JSON")
        void clearsFilePathAndReportsErrorWhenSaveFileIsCorrupt() throws IOException {
            // Arrange
            Path saveFile = tempDir.resolve("corrupt.json");
            Files.writeString(saveFile, "{ this is not valid json }");
            // Act
            controller.validateAndSetSaveFile(saveFile.toFile());
            // Assert
            assertTrue(inputs.saveFilePath.isBlank());
            assertEquals(1, errors.size());
            assertTrue(successes.isEmpty());
        }

        @Test
        @DisplayName("Should clear file path and report error when save file is missing required fields")
        void clearsFilePathAndReportsErrorWhenSaveFileMissingRequiredFields() throws IOException {
            // Arrange
            Path saveFile = tempDir.resolve("incomplete.json");
            Files.writeString(saveFile, "{ \"player\": {} }");
            // Act
            controller.validateAndSetSaveFile(saveFile.toFile());
            // Assert
            assertTrue(inputs.saveFilePath.isBlank());
            assertEquals(1, errors.size());
        }

        @Test
        @DisplayName("Should clear file path and report error when save file does not exist")
        void clearsFilePathAndReportsErrorWhenSaveFileDoesNotExist() {
            // Arrange
            File nonExistent = tempDir.resolve("ghost.json").toFile();
            // Act
            controller.validateAndSetSaveFile(nonExistent);
            // Assert
            assertTrue(inputs.saveFilePath.isBlank());
            assertEquals(1, errors.size());
        }
    }

    @Nested
    @DisplayName("validateAndSetStockFile()")
    class ValidateAndSetStockFile {

        @Test
        @DisplayName("Should store file path and emit success when stock file is valid")
        void storesFilePathWhenStockFileIsValid() throws IOException {
            // Arrange
            Path stockFile = tempDir.resolve("stocks.csv");
            Files.writeString(stockFile, "AAPL,Apple Inc.,276.43\n");
            // Act
            controller.validateAndSetStockFile(stockFile.toFile());
            // Assert
            assertEquals(stockFile.toAbsolutePath().toString(), inputs.stockFilePath);
            assertTrue(errors.isEmpty());
            assertEquals(1, successes.size());
        }

        @Test
        @DisplayName("Should clear file path and report error when stock file has invalid format")
        void clearsFilePathAndReportsErrorWhenStockFileHasInvalidFormat() throws IOException {
            // Arrange
            Path stockFile = tempDir.resolve("stocks.csv");
            Files.writeString(stockFile, "INVALID_LINE\n");
            // Act
            controller.validateAndSetStockFile(stockFile.toFile());
            // Assert
            assertTrue(inputs.stockFilePath.isBlank());
            assertEquals(1, errors.size());
            assertTrue(successes.isEmpty());
        }

        @Test
        @DisplayName("Should clear file path and report error when stock file is empty")
        void clearsFilePathAndReportsErrorWhenStockFileIsEmpty() throws IOException {
            // Arrange
            Path stockFile = tempDir.resolve("stocks.csv");
            Files.writeString(stockFile, "");
            // Act
            controller.validateAndSetStockFile(stockFile.toFile());
            // Assert
            assertTrue(inputs.stockFilePath.isBlank());
            assertEquals(1, errors.size());
        }

        @Test
        @DisplayName("Should clear file path and report error when stock file does not exist")
        void clearsFilePathAndReportsErrorWhenStockFileDoesNotExist() {
            // Arrange
            File nonExistent = tempDir.resolve("ghost.csv").toFile();
            // Act
            controller.validateAndSetStockFile(nonExistent);
            // Assert
            assertTrue(inputs.stockFilePath.isBlank());
            assertEquals(1, errors.size());
        }
    }

    /**
     * In-memory implementation of {@link StartScreenInputs} for tests.
     * Each field is set directly by the test before invoking the controller.
     */
    private static class StubInputs implements StartScreenInputs {
        String name = "";
        String capital = "";
        String stockFilePath = "";
        String saveFilePath = "";
        Currency selectedCurrency = Currency.getInstance("USD");

        @Override public String getName() { return name; }
        @Override public String getCapital() { return capital; }
        @Override public String getStockFilePath() { return stockFilePath; }
        @Override public String getSaveFilePath() { return saveFilePath; }
        @Override public Currency getSelectedCurrency() { return selectedCurrency; }
        @Override public void setStockFilePath(String path) { this.stockFilePath = path == null ? "" : path; }
        @Override public void setSaveFilePath(String path) { this.saveFilePath = path == null ? "" : path; }
    }

    /**
     * Test double for {@link GameService} that records the arguments passed
     * to its mutating methods and lets tests trigger controlled failures.
     */
    private static class RecordingGameService extends GameService {
        String lastName;
        BigDecimal lastCapital;
        File lastStockFile;
        Currency lastCurrency;
        File lastLoadedFile;
        int createNewGameCalls;
        int loadGameCalls;
        RuntimeException failNextCreate;
        RuntimeException failNextLoad;

        @Override
        public void createNewGame(String name, BigDecimal capital) {
            createNewGameCalls++;
            this.lastName = name;
            this.lastCapital = capital;
            this.lastStockFile = null;
            this.lastCurrency = null;
            throwPendingFailure();
        }

        @Override
        public void createNewGame(String name, BigDecimal capital, File stockFile, Currency currency) {
            createNewGameCalls++;
            this.lastName = name;
            this.lastCapital = capital;
            this.lastStockFile = stockFile;
            this.lastCurrency = currency;
            throwPendingFailure();
        }

        @Override
        public void loadGame(File file) {
            loadGameCalls++;
            this.lastLoadedFile = file;
            if (failNextLoad != null) {
                RuntimeException toThrow = failNextLoad;
                failNextLoad = null;
                throw toThrow;
            }
        }

        private void throwPendingFailure() {
            if (failNextCreate != null) {
                RuntimeException toThrow = failNextCreate;
                failNextCreate = null;
                throw toThrow;
            }
        }
    }
}
