package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.StartScreenInputs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StartController}.
 *
 * <p>Uses the test seam constructor that takes a {@link StartScreenInputs}
 * and a string {@link java.util.function.Consumer} so the start flow can be
 * exercised without initialising the JavaFX toolkit. All tests follow the
 * AAA pattern.</p>
 */
class StartControllerTest {

    private StubInputs inputs;
    private RecordingGameManager gameManager;
    private List<String> errors;
    private boolean showMainCalled;
    private StartController controller;

    @BeforeEach
    void setUp() {
        inputs = new StubInputs();
        gameManager = new RecordingGameManager();
        errors = new ArrayList<>();
        showMainCalled = false;
        controller = new StartController(
                gameManager,
                inputs,
                () -> showMainCalled = true,
                errors::add);
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
            assertEquals("Dara", gameManager.lastName);
            assertEquals(0, new BigDecimal("10000.00").compareTo(gameManager.lastCapital));
            assertNull(gameManager.lastStockFile);
            assertNull(gameManager.lastCurrency);
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
            assertEquals("Dara", gameManager.lastName);
            assertEquals(0, new BigDecimal("10000.00").compareTo(gameManager.lastCapital));
            assertEquals(Path.of("/tmp/stocks.csv"), gameManager.lastStockFile.toPath());
            assertEquals(Currency.getInstance("EUR"), gameManager.lastCurrency);
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
            assertEquals(0, gameManager.createNewGameCalls);
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
            assertEquals(0, gameManager.createNewGameCalls);
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
            assertEquals(0, gameManager.createNewGameCalls);
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
            assertEquals(0, gameManager.createNewGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager fails")
        void reportsErrorWhenGameManagerFails() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "";
            gameManager.failNextCreate = new IllegalStateException("default stock data missing");
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals("default stock data missing", errors.getFirst());
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager throws UncheckedIOException")
        void reportsErrorWhenGameManagerThrowsIoException() {
            // Arrange
            inputs.name = "Dara";
            inputs.capital = "10000.00";
            inputs.stockFilePath = "";
            gameManager.failNextCreate =
                    new UncheckedIOException("read failed", new java.io.IOException("disk error"));
            // Act
            controller.handleStartGame();
            // Assert
            assertEquals(1, errors.size());
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
            assertEquals(Path.of("/tmp/save.json"), gameManager.lastLoadedFile.toPath());
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
            assertEquals(0, gameManager.loadGameCalls);
            assertFalse(showMainCalled);
        }

        @Test
        @DisplayName("Should report error and not navigate when game manager fails to load")
        void reportsErrorWhenGameManagerFailsToLoad() {
            // Arrange
            inputs.saveFilePath = "/tmp/save.json";
            gameManager.failNextLoad = new IllegalArgumentException("corrupt save file");
            // Act
            controller.handleLoadGame();
            // Assert
            assertEquals(1, errors.size());
            assertEquals("corrupt save file", errors.getFirst());
            assertFalse(showMainCalled);
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
     * Test double for {@link GameManager} that records the arguments passed
     * to its mutating methods and lets tests trigger controlled failures.
     */
    private static class RecordingGameManager extends GameManager {
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
