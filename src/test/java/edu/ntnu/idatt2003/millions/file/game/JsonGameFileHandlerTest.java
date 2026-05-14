package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanCatalog;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;

import java.io.File;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
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
        stock = new Stock("EQNR", "Equinor ASA",
                new ArrayList<>(List.of(new BigDecimal("276.43"))),
                Currency.getInstance("NOK"));
        exchange = new Exchange("Oslo Børs",
                new ArrayList<>(List.of(stock)),
                new FixedRateCurrencyConverter());
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
            exchange.buy("EQNR", new BigDecimal("5"), player);
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
        void returnsCorrectPlayerNameAfterLoad() throws GameSaveCorruptException {
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
        void returnsCorrectPlayerMoneyAfterLoad() throws GameSaveCorruptException {
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
        void returnsCorrectExchangeNameAfterLoad() throws GameSaveCorruptException {
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
        void returnsCorrectWeekAfterLoad() throws GameSaveCorruptException {
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
        void returnsCorrectNumberOfSharesAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("EQNR", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getPortfolio().getShares().size());
        }

        @Test
        @DisplayName("Should relink share stock reference to exchange stock after load")
        void relinksShareStockReferenceAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("EQNR", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            Share loadedShare = state.player().getPortfolio().getShares().getFirst();
            Stock exchangeStock = state.exchange().getStock("EQNR");
            // Assert
            assertSame(exchangeStock, loadedShare.getStock());
        }

        @Test
        @DisplayName("Should return correct number of transactions in archive after load")
        void returnsCorrectNumberOfTransactionsAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("EQNR", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertFalse(state.player().getTransactionArchive().isEmpty());
        }

        @Test
        @DisplayName("Should return empty portfolio when no shares were owned")
        void returnsEmptyPortfolioWhenNoSharesOwned() throws GameSaveCorruptException {
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
        void returnsEmptyTransactionArchiveWhenNoTransactionsMade() throws GameSaveCorruptException {
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
        void preservesPriceHistoryAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.advance();
            exchange.advance();
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(3, state.exchange().getStock("EQNR")
                    .getHistoricalPrices().size());
        }

        @Test
        @DisplayName("Should merge shares with the same stock symbol from a legacy save on load")
        void mergesSharesWithSameSymbolFromLegacySaveOnLoad() throws GameSaveCorruptException {
            // Arrange: bypass addShare to simulate a legacy file with two entries for same stock
            Path file = tempDir.resolve("legacy_save.json");
            Share share1 = new Share(stock, new BigDecimal("5"), new BigDecimal("276.43"));
            Share share2 = new Share(stock, new BigDecimal("3"), new BigDecimal("300.00"));
            player.getPortfolio().setShares(List.of(share1, share2));
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getPortfolio().getShares().size());
            Share merged = state.player().getPortfolio().getShares().getFirst();
            assertEquals(0, new BigDecimal("8").compareTo(merged.getQuantity()));
        }

        @Test
        @DisplayName("Should throw GameSaveCorruptException when file contains invalid JSON")
        void throwsGameSaveCorruptExceptionForInvalidJson() throws Exception {
            // Arrange
            Path file = tempDir.resolve("corrupt.json");
            Files.writeString(file, "{ this is not valid json }");
            // Act & Assert
            assertThrows(GameSaveCorruptException.class, () ->
                    handler.loadGame(file.toFile()));
        }

        @Test
        @DisplayName("Should throw GameSaveCorruptException when file is missing required fields")
        void throwsGameSaveCorruptExceptionForMissingFields() throws Exception {
            // Arrange
            Path file = tempDir.resolve("incomplete.json");
            Files.writeString(file, "{ \"player\": {} }");
            // Act & Assert
            assertThrows(GameSaveCorruptException.class, () ->
                    handler.loadGame(file.toFile()));
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

        @Test
        @DisplayName("Should preserve active loans after round-trip save/load")
        void preservesActiveLoansAfterRoundTrip() throws ExcessiveDebtException, GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            Loan loan = new Loan(LoanCatalog.getOffers().get(0), new BigDecimal("4000.00"), 1);
            player.takeLoan(loan, new FixedRateCurrencyConverter());
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getActiveLoans().size());
            Loan loadedLoan = state.player().getActiveLoans().get(0);
            assertEquals(0, new BigDecimal("4000.00").compareTo(loadedLoan.principal()));
            assertEquals(1, loadedLoan.takenAtWeek());
        }

        @Test
        @DisplayName("Should preserve loan ledger entries after round-trip save/load")
        void preservesLoanLedgerAfterRoundTrip() throws ExcessiveDebtException, GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            Loan loan = new Loan(LoanCatalog.getOffers().get(0), new BigDecimal("4000.00"), 1);
            player.takeLoan(loan, new FixedRateCurrencyConverter());
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getLoanLedger().size());
            assertEquals(0, new BigDecimal("4000.00")
                    .compareTo(state.player().getLoanLedger().get(0).amount()));
        }

        @Test
        @DisplayName("Should preserve total debt history after round-trip save/load")
        void preservesTotalDebtHistoryAfterRoundTrip() throws ExcessiveDebtException, GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            Loan loan = new Loan(LoanCatalog.getOffers().get(0), new BigDecimal("4000.00"), 1);
            player.takeLoan(loan, new FixedRateCurrencyConverter());
            player.recordTotalDebt();
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            assertEquals(1, state.player().getTotalDebtHistory().size());
            assertEquals(0, new BigDecimal("4000.00")
                    .compareTo(state.player().getTotalDebtHistory().get(0)));
        }

        @Test
        @DisplayName("Should relink archived transaction shares to canonical exchange stocks after load")
        void relinksArchivedTransactionShareAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("EQNR", new BigDecimal("5"), player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            Stock canonicalStock = state.exchange().getStock("EQNR");
            for (Transaction transaction : state.player().getTransactionArchive().getAll()) {
                assertSame(canonicalStock, transaction.getShare().getStock());
            }
        }

        @Test
        @DisplayName("Should relink archived sale transaction share after load")
        void relinksArchivedSaleShareAfterLoad() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            exchange.buy("EQNR", new BigDecimal("5"), player);
            Share portfolioShare = player.getPortfolio().getShares().getFirst();
            new Sale(portfolioShare, 1).commit(player);
            handler.saveGame(player, exchange, file.toFile());
            // Act
            GameState state = handler.loadGame(file.toFile());
            // Assert
            Stock canonicalStock = state.exchange().getStock("EQNR");
            for (Transaction transaction : state.player().getTransactionArchive().getAll()) {
                assertSame(canonicalStock, transaction.getShare().getStock());
            }
        }

        @Test
        @DisplayName("Should be able to advance week after loading game and reinitializing exchange")
        void canAdvanceWeekAfterLoadingGameAndReinitialize() throws GameSaveCorruptException {
            // Arrange
            Path file = tempDir.resolve("save.json");
            handler.saveGame(player, exchange, file.toFile());
            GameState state = handler.loadGame(file.toFile());
            state.exchange().reinitialize(new FixedRateCurrencyConverter());

            // Act & Assert
            assertDoesNotThrow(() -> state.exchange().advance());
        }
    }

    @Nested
    @DisplayName("reinitialize()")
    class Reinitialize {

        @Test
        @DisplayName("Should allow advance() to be called after reinitialize")
        void allowsAdvanceAfterReinitialize() {
            // Arrange
            exchange.reinitialize(new FixedRateCurrencyConverter());

            // Act & Assert
            assertDoesNotThrow(() -> exchange.advance());
        }
    }

    @Nested
    @DisplayName("Notification round-trip")
    class NotificationRoundTrip {

        @Test
        @DisplayName("Saves and loads notifications")
        void savesAndLoadsNotifications() throws GameSaveCorruptException {
            player.addNotification(new Notification(
                    1, Notification.Severity.WARNING,
                    "notification.loanMaturity.soon.title",
                    "notification.loanMaturity.soon.body",
                    List.of("@loans.offer.fast.name", "2", "5000.00"),
                    3, false
            ));
            File file = tempDir.resolve("notif-save.json").toFile();

            handler.saveGame(player, exchange, file);
            GameState loaded = handler.loadGame(file);

            assertEquals(1, loaded.player().getNotifications().size());
            Notification n = loaded.player().getNotifications().get(0);
            assertEquals(Notification.Severity.WARNING, n.severity());
            assertEquals("notification.loanMaturity.soon.title", n.titleKey());
            assertEquals(3, n.week());
            assertFalse(n.read());
        }

        @Test
        @DisplayName("Saves and loads threshold flags")
        void savesAndLoadsThresholdFlags() throws GameSaveCorruptException {
            player.setWasAboveDebtThreshold(true);
            player.setWasLowOnCash(true);
            File file = tempDir.resolve("flags-save.json").toFile();

            handler.saveGame(player, exchange, file);
            GameState loaded = handler.loadGame(file);

            assertTrue(loaded.player().wasAboveDebtThreshold());
            assertTrue(loaded.player().wasLowOnCash());
        }

        @Test
        @DisplayName("Saves and loads previousStatus")
        void savesAndLoadsPreviousStatus() throws GameSaveCorruptException {
            player.setPreviousStatus(PlayerStatusLevel.INVESTOR);
            File file = tempDir.resolve("status-save.json").toFile();

            handler.saveGame(player, exchange, file);
            GameState loaded = handler.loadGame(file);

            assertEquals(PlayerStatusLevel.INVESTOR, loaded.player().getPreviousStatus());
        }
    }
}