package edu.ntnu.idatt2003.millions.manager;

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
 * Unit tests for the facade methods on {@link GameManager}.
 * <p>
 * This test class verifies that the facade methods on GameManager correctly
 * delegate to the underlying domain objects ({@link Player}, Portfolio,
 * {@link Exchange}) and return the expected derived values.
 * </p>
 * <p>
 * Game state is set up by saving a known {@link Player} and {@link Exchange}
 * to a temporary file, then loading it via {@link GameManager#loadGame}.
 * </p>
 */
class GameManagerTest {

    private static final BigDecimal STARTING_MONEY = new BigDecimal("10000.00");
    private static final BigDecimal STOCK_PRICE = new BigDecimal("100.00");
    private static final BigDecimal QUANTITY = new BigDecimal("5");
    private static final BigDecimal PURCHASE_COMMISSION = new BigDecimal("2.50000");
    private static final BigDecimal SALE_COMMISSION = new BigDecimal("5.0000");
    private static final BigDecimal EXPECTED_PORTFOLIO_VALUE = new BigDecimal("495.0000");
    private static final BigDecimal EXPECTED_NET_WORTH_AFTER_PURCHASE = new BigDecimal("9992.50000");

    private GameManager gameManager;

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

        gameManager = new GameManager();
        gameManager.loadGame(file.toFile());
    }

    @Nested
    @DisplayName("addObserver()")
    class AddObserver {

        @Test
        @DisplayName("Should throw exception when observer is null")
        void throwsExceptionWhenObserverIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameManager.addObserver(null));
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
                    gameManager.saveGame(null));
        }

        @Test
        @DisplayName("Should throw exception when no active game exists")
        void throwsExceptionWhenNoActiveGameExists() {
            // Arrange
            GameManager emptyGameManager = new GameManager();
            File file = tempDir.resolve("empty-save.json").toFile();
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    emptyGameManager.saveGame(file));
        }

        @Test
        @DisplayName("Should save an active game to file")
        void savesActiveGameToFile() {
            // Arrange
            File file = tempDir.resolve("saved-game.json").toFile();
            // Act
            gameManager.saveGame(file);
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
                    gameManager.loadGame(null));
        }

        @Test
        @DisplayName("Should load player and exchange from file")
        void loadsPlayerAndExchangeFromFile() {
            // Act & Assert
            assertEquals("Dara", gameManager.getPlayer().getName());
            assertEquals("NYSE", gameManager.getExchange().getName());
            assertTrue(gameManager.getExchange().hasStock("EQNR"));
            assertNotNull(gameManager.getExchange().getCurrencyConverter());
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
            GameManager newGameManager = new GameManager();
            newGameManager.addObserver(observer);
            // Act
            newGameManager.createNewGame("Dara", STARTING_MONEY, stockFile);
            // Assert
            assertEquals("Dara", newGameManager.getPlayer().getName());
            assertEquals(0, STARTING_MONEY.compareTo(newGameManager.getPlayer().getMoney()));
            assertEquals("MainExchange", newGameManager.getExchange().getName());
            assertTrue(newGameManager.getExchange().hasStock("AAPL"));
            assertTrue(newGameManager.getExchange().hasStock("MSFT"));
            assertNotNull(newGameManager.getExchange().getCurrencyConverter());
            assertEquals(1, observer.updateCount);
        }

        @Test
        @DisplayName("Should create player and exchange from default stock data")
        void createsPlayerAndExchangeFromDefaultStockData() {
            // Arrange
            CountingObserver observer = new CountingObserver();
            GameManager newGameManager = new GameManager();
            newGameManager.addObserver(observer);
            // Act
            newGameManager.createNewGame("Dara", STARTING_MONEY);
            // Assert
            assertEquals("Dara", newGameManager.getPlayer().getName());
            assertEquals(0, STARTING_MONEY.compareTo(newGameManager.getPlayer().getMoney()));
            assertEquals("MainExchange", newGameManager.getExchange().getName());
            assertTrue(newGameManager.getExchange().hasStock("NVDA"));
            assertTrue(newGameManager.getExchange().hasStock("AAPL"));
            assertNotNull(newGameManager.getExchange().getCurrencyConverter());
            assertEquals(1, observer.updateCount);
        }

        @Test
        @DisplayName("Should throw exception when stock file is null")
        void throwsExceptionWhenStockFileIsNull() {
            // Arrange
            GameManager newGameManager = new GameManager();
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    newGameManager.createNewGame("Dara", STARTING_MONEY, null));
        }

        @Test
        @DisplayName("Should throw exception when player name is blank")
        void throwsExceptionWhenPlayerNameIsBlank() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\n");
            GameManager newGameManager = new GameManager();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameManager.createNewGame("", STARTING_MONEY, stockFile));
        }

        @Test
        @DisplayName("Should throw exception when capital is negative")
        void throwsExceptionWhenCapitalIsNegative() throws IOException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\n");
            GameManager newGameManager = new GameManager();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameManager.createNewGame("Dara", new BigDecimal("-1.00"), stockFile));
        }

        @Test
        @DisplayName("Should throw exception when stock file is empty")
        void throwsExceptionWhenStockFileIsEmpty() throws IOException {
            // Arrange
            File stockFile = createStockFile("");
            GameManager newGameManager = new GameManager();
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    newGameManager.createNewGame("Dara", STARTING_MONEY, stockFile));
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
            gameManager.addObserver(observer);
            // Act
            Transaction transaction = gameManager.buy("EQNR", QUANTITY);
            // Assert
            assertNotNull(transaction);
            assertEquals(1, gameManager.getPlayer().getPortfolio().getShares("EQNR").size());
            assertEquals(1, observer.updateCount);
            assertEquals(0, STARTING_MONEY.subtract(STOCK_PRICE.multiply(QUANTITY))
                    .subtract(PURCHASE_COMMISSION)
                    .compareTo(gameManager.getPlayer().getMoney()));
        }
    }

    @Nested
    @DisplayName("sell()")
    class Sell {

        @Test
        @DisplayName("Should sell share and notify observers")
        void sellsShareAndNotifiesObservers() {
            // Arrange
            gameManager.buy("EQNR", QUANTITY);
            Share share = gameManager.getPlayer().getPortfolio().getShares("EQNR").getFirst();
            CountingObserver observer = new CountingObserver();
            gameManager.addObserver(observer);
            // Act
            Transaction transaction = gameManager.sell(share);
            // Assert
            assertNotNull(transaction);
            assertTrue(gameManager.getPlayer().getPortfolio().getShares("EQNR").isEmpty());
            assertEquals(1, observer.updateCount);
            assertEquals(0, STARTING_MONEY.subtract(PURCHASE_COMMISSION)
                    .subtract(SALE_COMMISSION)
                    .compareTo(gameManager.getPlayer().getMoney()));
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
            gameManager.addObserver(observer);
            // Act
            gameManager.advanceWeek();
            // Assert
            assertEquals(2, gameManager.getExchange().getWeek());
            assertEquals(0, STARTING_MONEY.compareTo(gameManager.getPreviousNetWorth()));
            assertEquals(2, gameManager.getPlayer().getNetWorthHistory().size());
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
            BigDecimal netWorth = gameManager.getPlayerNetWorth();
            // Assert
            assertEquals(0, STARTING_MONEY.compareTo(netWorth));
        }

        @Test
        @DisplayName("Should return same total when shares are bought at current price")
        void returnsSameTotalAfterPurchaseAtCurrentPrice() {
            // Arrange
            gameManager.buy("EQNR", QUANTITY);
            // Act
            BigDecimal netWorth = gameManager.getPlayerNetWorth();
            // Assert: cash after purchase plus portfolio liquidation value
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
            BigDecimal change = gameManager.getPlayerNetWorthChangeSinceStart();
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
            BigDecimal percent = gameManager.getPlayerNetWorthChangePercentSinceStart();
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
            BigDecimal change = gameManager.getPlayerWeeklyNetWorthChange();
            // Assert
            assertNull(change);
        }

        @Test
        @DisplayName("Should return non-null value after a week is advanced")
        void returnsNonNullAfterAdvance() {
            // Arrange
            gameManager.advanceWeek();
            // Act
            BigDecimal change = gameManager.getPlayerWeeklyNetWorthChange();
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
            BigDecimal percent = gameManager.getPlayerWeeklyNetWorthChangePercent();
            // Assert
            assertNull(percent);
        }

        @Test
        @DisplayName("Should return non-null value after a week is advanced")
        void returnsNonNullAfterAdvance() {
            // Arrange
            gameManager.advanceWeek();
            // Act
            BigDecimal percent = gameManager.getPlayerWeeklyNetWorthChangePercent();
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
            PlayerStatusLevel status = gameManager.getPlayerStatus();
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
            BigDecimal value = gameManager.getPortfolioValue();
            // Assert
            assertEquals(0, BigDecimal.ZERO.compareTo(value));
        }

        @Test
        @DisplayName("Should return shares times price after purchase")
        void returnsSharesTimesPriceAfterPurchase() {
            // Arrange
            gameManager.buy("EQNR", QUANTITY);
            // Act
            BigDecimal value = gameManager.getPortfolioValue();
            // Assert: 5 shares * 100 NOK minus 1% sale commission
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
