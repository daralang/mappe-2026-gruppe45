package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.*;
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
 * Unit tests for the {@link JsonGameFileHandler} class.
 * <p>
 * This test class verifies the behaviour of JsonGameFileHandler,
 * including saving and loading game state to and from JSON files.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class JsonGameFileHandlerTest {

    private JsonGameFileHandler handler;
    private Player player;
    private Exchange exchange;
    private Stock stock;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        handler = new JsonGameFileHandler();
        stock = new Stock("AAPL", "Apple Inc.",
                new ArrayList<>(List.of(new BigDecimal("276.43"))));
        exchange = new Exchange("Oslo Børs",
                new ArrayList<>(List.of(stock)));
        player = new Player("Alva", new BigDecimal("10000.00"));
    }

    @Nested
    @DisplayName("saveGame()")
    class SaveGame {

        @Test
        @DisplayName("Should create file when game is saved")
        void createsFileWhenGameIsSaved() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            // Act
            handler.saveGame(player, exchange, file.toFile());
            // Assert
            assertTrue(file.toFile().exists());
        }

        @Test
        @DisplayName("Should write valid JSON with player and exchange keys")
        void writesValidJsonWithExpectedKeys() throws Exception {
            // Arrange
            Path file = tempDir.resolve("save.json");
            // Act
            handler.saveGame(player, exchange, file.toFile());
            String content = Files.readString(file);
            // Assert
            assertTrue(content.contains("\"player\""));
            assertTrue(content.contains("\"exchange\""));
        }

        @Test
        @DisplayName("Should save game with shares in portfolio")
        void savesGameWithSharesInPortfolio() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            Share share = new Share(stock, new BigDecimal("5"),
                    new BigDecimal("276.43"));
            player.getPortfolio().addShare(share);
            // Act & Assert
            assertDoesNotThrow(() ->
                    handler.saveGame(player, exchange, file.toFile()));
        }

        @Test
        @DisplayName("Should save game with transactions in archive")
        void savesGameWithTransactionsInArchive() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("AAPL", new BigDecimal("5"), player);
            // Act & Assert
            assertDoesNotThrow(() ->
                    handler.saveGame(player, exchange, file.toFile()));
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void throwsExceptionWhenFileIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.saveGame(player, exchange, null));
        }

        @Test
        @DisplayName("Should throw exception when player is null")
        void throwsExceptionWhenPlayerIsNull() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.saveGame(null, exchange, file.toFile()));
        }

        @Test
        @DisplayName("Should throw exception when exchange is null")
        void throwsExceptionWhenExchangeIsNull() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.saveGame(player, null, file.toFile()));
        }

        @Test
        @DisplayName("Should throw exception when directory does not exist")
        void throwsExceptionWhenDirectoryDoesNotExist() {
            // Arrange
            Path file = tempDir.resolve("nonexistent/save.json");
            // Act & Assert
            assertThrows(UncheckedIOException.class, () ->
                    handler.saveGame(player, exchange, file.toFile()));
        }
    }

    @Nested
    @DisplayName("loadGame()")
    class LoadGame {

        @Test
        @DisplayName("Should return correct player name after load")
        void returnsCorrectPlayerNameAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals("Alva", state.player().getName());
        }

        @Test
        @DisplayName("Should return correct player money after load")
        void returnsCorrectPlayerMoneyAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(0, new BigDecimal("10000.00")
                    .compareTo(state.player().getMoney()));
        }

        @Test
        @DisplayName("Should return correct exchange name after load")
        void returnsCorrectExchangeNameAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals("Oslo Børs", state.exchange().getName());
        }

        @Test
        @DisplayName("Should return correct week after load")
        void returnsCorrectWeekAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.advance();
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(2, state.exchange().getWeek());
        }

        @Test
        @DisplayName("Should return correct number of shares in portfolio after load")
        void returnsCorrectNumberOfSharesAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("AAPL", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getPortfolio().getShares().size());
        }

        @Test
        @DisplayName("Should relink share stock reference to exchange stock after load")
        void relinksShareStockReferenceAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("AAPL", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            Share loadedShare = state.player().getPortfolio().getShares().getFirst();
            Stock exchangeStock = state.exchange().getStock("AAPL");
            // Assert
            assertSame(exchangeStock, loadedShare.getStock());
        }

        @Test
        @DisplayName("Should return correct number of transactions in archive after load")
        void returnsCorrectNumberOfTransactionsAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("AAPL", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertFalse(state.player().getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should return empty portfolio when no shares were owned")
        void returnsEmptyPortfolioWhenNoSharesOwned() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertTrue(state.player().getPortfolio().getShares().isEmpty());
        }

        @Test
        @DisplayName("Should return empty transaction archive when no transactions were made")
        void returnsEmptyTransactionArchiveWhenNoTransactionsMade() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertTrue(state.player().getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should preserve price history after load")
        void preservesPriceHistoryAfterLoad() {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.advance();
            exchange.advance();
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(3, state.exchange().getStock("AAPL")
                    .getHistoricalPrices().size());
        }

        @Test
        @DisplayName("Should throw exception when file does not exist")
        void throwsExceptionWhenFileDoesNotExist() {
            // Arrange
            Path file = tempDir.resolve("nonexistent.json");
            // Act & Assert
            assertThrows(UncheckedIOException.class, () ->
                    handler.loadGame(file.toFile()));
        }

        @Test
        @DisplayName("Should throw exception when file is null")
        void throwsExceptionWhenFileIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    handler.loadGame(null));
        }
    }
}