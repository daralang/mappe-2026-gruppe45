package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the facade methods on {@link GameService}.
 * <p>
 * This test class verifies that the facade methods on GameService correctly
 * delegate to the underlying domain objects ({@link Player}, Portfolio,
 * {@link Exchange}) and return the expected derived values.
 * </p>
 * <p>
 * Game state is set up by saving a known {@link Player} and {@link Exchange}
 * to a temporary file, then loading it via {@link GameService#loadGame}.
 * </p>
 */
class GameServiceTest {

    private static final BigDecimal STARTING_MONEY = new BigDecimal("10000.00");
    private static final BigDecimal STOCK_PRICE = new BigDecimal("100.00");
    private static final BigDecimal QUANTITY = new BigDecimal("5");
    private static final BigDecimal PURCHASE_COMMISSION = new BigDecimal("2.50000");
    private static final BigDecimal SALE_COMMISSION = new BigDecimal("5.0000");
    private static final BigDecimal EXPECTED_PORTFOLIO_VALUE = new BigDecimal("500.0000");
    private static final BigDecimal EXPECTED_NET_WORTH_AFTER_PURCHASE = new BigDecimal("9997.50000");

    private GameService gameService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Stock stock = new Stock("EQNR", "Equinor ASA",
                new ArrayList<>(List.of(STOCK_PRICE)),
                Currency.getInstance("NOK"));
        Exchange exchange = new Exchange("NYSE",
                new ArrayList<>(List.of(stock)),
                new FixedRateCurrencyConverter());
        Player player = new Player("Dara", STARTING_MONEY);

        Path file = tempDir.resolve("save.json");
        new JsonGameFileHandler().saveGame(player, exchange, file.toFile());

        gameService = new GameService();
        gameService.loadGame(file.toFile());
    }

    @Nested
    @DisplayName("addObserver()")
    class AddObserver {

        @Test
        @DisplayName("Should throw exception when observer is null")
        void throwsExceptionWhenObserverIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.addObserver(null));
        }
    }

    @Nested
    @DisplayName("saveGame()")
    class SaveGame {

        @Test
        @DisplayName("Should throw exception when file is null")
        void throwsExceptionWhenFileIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.saveGame(null));
        }

        @Test
        @DisplayName("Should throw exception when no active game exists")
        void throwsExceptionWhenNoActiveGameExists() {
            // Arrange
            GameService emptyGameService = new GameService();
            File file = tempDir.resolve("empty-save.json").toFile();
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    emptyGameService.saveGame(file));
        }

        @Test
        @DisplayName("Should save an active game to file")
        void savesActiveGameToFile() {
            // Arrange
            File file = tempDir.resolve("saved-game.json").toFile();
            // Act
            gameService.saveGame(file);
            // Assert
            assertTrue(file.isFile());
            assertTrue(file.length() > 0);
        }
    }

    @Nested
    @DisplayName("loadGame()")
    class LoadGame {

        @Test
        @DisplayName("Should throw exception when file is null")
        void throwsExceptionWhenFileIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.loadGame(null));
        }

        @Test
        @DisplayName("Should load player and exchange from file")
        void loadsPlayerAndExchangeFromFile() {
            // Act & Assert
            assertEquals("Dara", gameService.getPlayer().getName());
            assertEquals("NYSE", gameService.getExchange().getName());
            assertTrue(gameService.getExchange().hasStock("EQNR"));
            assertNotNull(gameService.getExchange().getCurrencyConverter());
        }
    }

    @Nested
    @DisplayName("createNewGame()")
    class CreateNewGame {

        @Test
        @DisplayName("Should create player and exchange from stock file")
        void createsPlayerAndExchangeFromStockFile() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n");
            CountingObserver observer = new CountingObserver();
            GameService newGameService = new GameService();
            newGameService.addObserver(observer);
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY, stockFile);
            // Assert
            assertEquals("Dara", newGameService.getPlayer().getName());
            assertEquals(0, STARTING_MONEY.compareTo(newGameService.getPlayer().getMoney()));
            assertEquals("MainExchange", newGameService.getExchange().getName());
            assertTrue(newGameService.getExchange().hasStock("AAPL"));
            assertTrue(newGameService.getExchange().hasStock("MSFT"));
            assertNotNull(newGameService.getExchange().getCurrencyConverter());
            assertEquals(1, observer.updateCount);
        }

        @Test
        @DisplayName("Should create player and exchange from default stock data")
        void createsPlayerAndExchangeFromDefaultStockData() {
            // Arrange
            CountingObserver observer = new CountingObserver();
            GameService newGameService = new GameService();
            newGameService.addObserver(observer);
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY);
            // Assert
            assertEquals("Dara", newGameService.getPlayer().getName());
            assertEquals(0, STARTING_MONEY.compareTo(newGameService.getPlayer().getMoney()));
            assertEquals("MainExchange", newGameService.getExchange().getName());
            assertTrue(newGameService.getExchange().hasStock("NVDA"));
            assertTrue(newGameService.getExchange().hasStock("AAPL"));
            assertNotNull(newGameService.getExchange().getCurrencyConverter());
            assertEquals(1, observer.updateCount);
        }

        @Test
        @DisplayName("Should throw exception when stock file is null")
        void throwsExceptionWhenStockFileIsNull() {
            // Arrange
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    newGameService.createNewGame("Dara", STARTING_MONEY, null));
        }

        @Test
        @DisplayName("Should throw exception when player name is blank")
        void throwsExceptionWhenPlayerNameIsBlank() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\n");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameService.createNewGame("", STARTING_MONEY, stockFile));
        }

        @Test
        @DisplayName("Should throw exception when capital is not greater than zero")
        void throwsExceptionWhenCapitalIsNotPositive() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\n");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameService.createNewGame("Dara", new BigDecimal("-1.00"), stockFile));
        }

        @Test
        @DisplayName("Should throw exception when capital is zero")
        void throwsExceptionWhenCapitalIsZero() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\n");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameService.createNewGame("Dara", BigDecimal.ZERO, stockFile));
        }

        @Test
        @DisplayName("Should throw exception when stock file is empty")
        void throwsExceptionWhenStockFileIsEmpty() throws IOException {
            // Arrange
            File stockFile = createStockFile("");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameService.createNewGame("Dara", STARTING_MONEY, stockFile));
        }

        @Test
        @DisplayName("Should tag uploaded stocks with the selected currency")
        void tagsUploadedStocksWithSelectedCurrency() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,100.00\n");
            Currency selectedCurrency = Currency.getInstance("EUR");
            GameService newGameService = new GameService();
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY, stockFile, selectedCurrency);
            // Assert
            assertEquals(selectedCurrency,
                    newGameService.getExchange().getStock("AAPL").getCurrency());
        }

        @Test
        @DisplayName("Should convert purchase cost from selected currency to NOK")
        void convertsPurchaseCostFromSelectedCurrencyToNok() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,100.00\n");
            Currency usd = Currency.getInstance("USD");
            GameService newGameService = new GameService();
            newGameService.createNewGame("Dara", STARTING_MONEY, stockFile, usd);
            // Act
            newGameService.buy("AAPL", QUANTITY);
            // Assert: 5 * 100 USD * 1.005 commission = 502.50 USD; * 9.21 NOK/USD = 4628.0250 NOK
            BigDecimal expectedRemaining = STARTING_MONEY.subtract(new BigDecimal("4628.0250"));
            assertEquals(0,
                    expectedRemaining.compareTo(newGameService.getPlayer().getMoney()));
            assertEquals(usd,
                    newGameService.getExchange().getStock("AAPL").getCurrency());
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void throwsExceptionWhenCurrencyIsNull() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,100.00\n");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    newGameService.createNewGame("Dara", STARTING_MONEY, stockFile, null));
        }

        @Test
        @DisplayName("Should default stock currency to USD when no currency is supplied")
        void defaultsStockCurrencyToUsdForCustomFile() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,100.00\n");
            GameService newGameService = new GameService();
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY, stockFile);
            // Assert
            assertEquals(Currency.getInstance("USD"),
                    newGameService.getExchange().getStock("AAPL").getCurrency());
        }

        @Test
        @DisplayName("Should tag default stock data with USD")
        void tagsDefaultStockDataWithUsd() {
            // Arrange
            GameService newGameService = new GameService();
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY);
            // Assert
            assertEquals(Currency.getInstance("USD"),
                    newGameService.getExchange().getStock("AAPL").getCurrency());
        }

        @Test
        @DisplayName("Should leave previous game state intact when createNewGame fails")
        void leavesPreviousStateIntactOnFailure() throws IOException {
            // Arrange: an already-active game from setUp() loaded "NYSE" with EQNR
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            File stockFile = createStockFile("AAPL,Apple Inc.,100.00\n");
            // Act: a failed createNewGame must not mutate the active state
            assertThrows(IllegalArgumentException.class, () ->
                    gameService.createNewGame("", STARTING_MONEY, stockFile));
            // Assert: previous player and exchange remain
            assertEquals("Dara", gameService.getPlayer().getName());
            assertEquals("NYSE", gameService.getExchange().getName());
            assertTrue(gameService.getExchange().hasStock("EQNR"));
            assertFalse(gameService.getExchange().hasStock("AAPL"));
            assertEquals(0, observer.updateCount);
        }

        @Test
        @DisplayName("Should not notify observers when stock file is null")
        void doesNotNotifyObserversWhenStockFileIsNull() {
            // Arrange
            CountingObserver observer = new CountingObserver();
            GameService newGameService = new GameService();
            newGameService.addObserver(observer);
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    newGameService.createNewGame("Dara", STARTING_MONEY, null));
            assertEquals(0, observer.updateCount);
        }
    }

    @Nested
    @DisplayName("buy()")
    class Buy {

        @Test
        @DisplayName("Should buy shares and notify observers")
        void buysSharesAndNotifiesObservers() {
            // Arrange
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            // Act
            Transaction transaction = gameService.buy("EQNR", QUANTITY);
            // Assert
            assertNotNull(transaction);
            assertEquals(1, gameService.getPlayer().getPortfolio().getShares("EQNR").size());
            assertEquals(1, observer.updateCount);
            assertEquals(0, STARTING_MONEY.subtract(STOCK_PRICE.multiply(QUANTITY))
                    .subtract(PURCHASE_COMMISSION)
                    .compareTo(gameService.getPlayer().getMoney()));
        }
    }

    @Nested
    @DisplayName("sell()")
    class Sell {

        @Test
        @DisplayName("Should sell share and notify observers")
        void sellsShareAndNotifiesObservers() {
            // Arrange
            gameService.buy("EQNR", QUANTITY);
            Share share = gameService.getPlayer().getPortfolio().getShares("EQNR").getFirst();
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            // Act
            Transaction transaction = gameService.sell(share);
            // Assert
            assertNotNull(transaction);
            assertTrue(gameService.getPlayer().getPortfolio().getShares("EQNR").isEmpty());
            assertEquals(1, observer.updateCount);
            assertEquals(0, STARTING_MONEY.subtract(PURCHASE_COMMISSION)
                    .subtract(SALE_COMMISSION)
                    .compareTo(gameService.getPlayer().getMoney()));
        }
    }

    @Nested
    @DisplayName("advanceWeek()")
    class AdvanceWeek {

        @Test
        @DisplayName("Should advance exchange week, record previous net worth and notify observers")
        void advancesWeekRecordsPreviousNetWorthAndNotifiesObservers() {
            // Arrange
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            // Act
            gameService.advanceWeek();
            // Assert
            assertEquals(2, gameService.getExchange().getWeek());
            assertEquals(0, STARTING_MONEY.compareTo(gameService.getPreviousNetWorth()));
            assertEquals(2, gameService.getPlayer().getNetWorthHistory().size());
            assertEquals(1, observer.updateCount);
        }
    }

    @Nested
    @DisplayName("getPlayerNetWorth()")
    class GetPlayerNetWorth {

        @Test
        @DisplayName("Should return starting money for fresh player with empty portfolio")
        void returnsStartingMoneyForFreshPlayer() {
            // Act
            BigDecimal netWorth = gameService.getPlayerNetWorth();
            // Assert
            assertEquals(0, STARTING_MONEY.compareTo(netWorth));
        }

        @Test
        @DisplayName("Should return same total when shares are bought at current price")
        void returnsSameTotalAfterPurchaseAtCurrentPrice() {
            // Arrange
            gameService.buy("EQNR", QUANTITY);
            // Act
            BigDecimal netWorth = gameService.getPlayerNetWorth();
            // Assert: cash after purchase plus portfolio market value (5 × 100 NOK)
            assertEquals(0, EXPECTED_NET_WORTH_AFTER_PURCHASE.compareTo(netWorth));
        }
    }

    @Nested
    @DisplayName("getPlayerNetWorthChangeSinceStart()")
    class GetPlayerNetWorthChangeSinceStart {

        @Test
        @DisplayName("Should return zero for fresh player")
        void returnsZeroForFreshPlayer() {
            // Act
            BigDecimal change = gameService.getPlayerNetWorthChangeSinceStart();
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(change));
        }
    }

    @Nested
    @DisplayName("getPlayerNetWorthChangePercentSinceStart()")
    class GetPlayerNetWorthChangePercentSinceStart {

        @Test
        @DisplayName("Should return zero for fresh player")
        void returnsZeroForFreshPlayer() {
            // Act
            BigDecimal percent = gameService.getPlayerNetWorthChangePercentSinceStart();
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(percent));
        }
    }

    @Nested
    @DisplayName("getPlayerWeeklyNetWorthChange()")
    class GetPlayerWeeklyNetWorthChange {

        @Test
        @DisplayName("Should return null before the first week advance")
        void returnsNullBeforeFirstAdvance() {
            // Act
            BigDecimal change = gameService.getPlayerWeeklyNetWorthChange();
            // Assert
            assertNull(change);
        }

        @Test
        @DisplayName("Should return non-null value after a week is advanced")
        void returnsNonNullAfterAdvance() {
            // Arrange
            gameService.advanceWeek();
            // Act
            BigDecimal change = gameService.getPlayerWeeklyNetWorthChange();
            // Assert
            assertNotNull(change);
        }
    }

    @Nested
    @DisplayName("getPlayerWeeklyNetWorthChangePercent()")
    class GetPlayerWeeklyNetWorthChangePercent {

        @Test
        @DisplayName("Should return null before the first week advance")
        void returnsNullBeforeFirstAdvance() {
            // Act
            BigDecimal percent = gameService.getPlayerWeeklyNetWorthChangePercent();
            // Assert
            assertNull(percent);
        }

        @Test
        @DisplayName("Should return non-null value after a week is advanced")
        void returnsNonNullAfterAdvance() {
            // Arrange
            gameService.advanceWeek();
            // Act
            BigDecimal percent = gameService.getPlayerWeeklyNetWorthChangePercent();
            // Assert
            assertNotNull(percent);
        }
    }

    @Nested
    @DisplayName("getPlayerStatus()")
    class GetPlayerStatus {

        @Test
        @DisplayName("Should return NOVICE for fresh player")
        void returnsNoviceForFreshPlayer() {
            // Act
            PlayerStatusLevel status = gameService.getPlayerStatus();
            // Assert
            assertEquals(PlayerStatusLevel.NOVICE, status);
        }
    }

    @Nested
    @DisplayName("getPortfolioValue()")
    class GetPortfolioValue {

        @Test
        @DisplayName("Should return zero for empty portfolio")
        void returnsZeroForEmptyPortfolio() {
            // Act
            BigDecimal value = gameService.getPortfolioValue();
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(value));
        }

        @Test
        @DisplayName("Should return shares times price after purchase")
        void returnsSharesTimesPriceAfterPurchase() {
            // Arrange
            gameService.buy("EQNR", QUANTITY);
            // Act
            BigDecimal value = gameService.getPortfolioValue();
            // Assert: 5 shares × 100 NOK = 500 NOK market value (no fees deducted)
            assertEquals(0, EXPECTED_PORTFOLIO_VALUE.compareTo(value));
        }
    }

    private static class CountingObserver implements GameObserver {
        private int updateCount;

        @Override
        public void onGameUpdated() {
            updateCount++;
        }
    }

    private File createStockFile(String content) throws IOException {
        Path file = tempDir.resolve("stocks-" + System.nanoTime() + ".csv");
        Files.writeString(file, content);
        return file.toFile();
    }
}
