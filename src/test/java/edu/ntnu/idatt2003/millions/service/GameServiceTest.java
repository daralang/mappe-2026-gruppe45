package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.file.game.GameSaveCorruptException;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.file.leaderboard.JsonLeaderboardFileHandler;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.file.stock.EmptyStockFileException;
import edu.ntnu.idatt2003.millions.file.stock.InvalidStockDataException;
import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.InsufficientSaleProceedsException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel;
import edu.ntnu.idatt2003.millions.model.player.Player;
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

    private GameService gameService;
    private LeaderboardService lbService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws GameSaveCorruptException {
        Stock stock = new Stock("EQNR", "Equinor ASA",
                new ArrayList<>(List.of(STOCK_PRICE)),
                Currency.getInstance("NOK"));
        Exchange exchange = new Exchange("NYSE",
                new ArrayList<>(List.of(stock)),
                new FixedRateCurrencyConverter());
        Player player = new Player("Dara", STARTING_MONEY);

        Path file = tempDir.resolve("save.json");
        new JsonGameFileHandler().saveGame(player, exchange, false, file.toFile());

        lbService = new LeaderboardService(
                new JsonLeaderboardFileHandler(),
                tempDir.resolve("leaderboard.json").toFile());
        gameService = new GameService(lbService);
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
    class CreateNewGameTab {

        @Test
        @DisplayName("Should create player and exchange from stock file")
        void createsPlayerAndExchangeFromStockFile() throws IOException, InvalidStockDataException {
            // Arrange
            File stockFile = createStockFile("AAPL,Apple Inc.,276.43\nMSFT,Microsoft,404.68\n");
            CountingObserver observer = new CountingObserver();
            GameService newGameService = new GameService();
            newGameService.addObserver(observer);
            // Act
            newGameService.createNewGame("Dara", STARTING_MONEY, stockFile);
            // Assert
            assertEquals("Dara", newGameService.getPlayer().getName());
            assertEquals(0, STARTING_MONEY.compareTo(newGameService.getPlayer().getCash()));
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
            assertEquals(0, STARTING_MONEY.compareTo(newGameService.getPlayer().getCash()));
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
        @DisplayName("Should throw EmptyStockFileException when stock file is empty")
        void throwsExceptionWhenStockFileIsEmpty() throws IOException {
            // Arrange
            File stockFile = createStockFile("");
            GameService newGameService = new GameService();
            // Act & Assert
            assertThrows(EmptyStockFileException.class, () ->
                    newGameService.createNewGame("Dara", STARTING_MONEY, stockFile));
        }

        @Test
        @DisplayName("Should tag uploaded stocks with the selected currency")
        void tagsUploadedStocksWithSelectedCurrency() throws IOException, InvalidStockDataException {
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
        void convertsPurchaseCostFromSelectedCurrencyToNok() throws IOException, InvalidStockDataException {
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
                    expectedRemaining.compareTo(newGameService.getPlayer().getCash()));
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
        void defaultsStockCurrencyToUsdForCustomFile() throws IOException, InvalidStockDataException {
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
                    .compareTo(gameService.getPlayer().getCash()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when symbol is null")
        void throwsWhenSymbolIsNull() {
            assertThrows(NullPointerException.class, () ->
                    gameService.buy(null, BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw NullPointerException when quantity is null")
        void throwsWhenQuantityIsNull() {
            assertThrows(NullPointerException.class, () ->
                    gameService.buy("EQNR", null));
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
                    .compareTo(gameService.getPlayer().getCash()));
        }

        @Test
        @DisplayName("Should throw NullPointerException when share is null")
        void throwsWhenShareIsNull() {
            assertThrows(NullPointerException.class, () ->
                    gameService.sell((Share) null));
        }

        @Test
        @DisplayName("Should throw NullPointerException when share is null (two-arg overload)")
        void throwsWhenShareIsNullTwoArg() {
            assertThrows(NullPointerException.class, () ->
                    gameService.sell(null, BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should throw NullPointerException when quantity is null")
        void throwsWhenQuantityIsNull() {
            gameService.buy("EQNR", QUANTITY);
            Share share = gameService.getPlayer().getPortfolio().getShares("EQNR").getFirst();
            assertThrows(NullPointerException.class, () ->
                    gameService.sell(share, null));
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
    @DisplayName("executeForcedSale()")
    class ExecuteForcedSale {

        private LoanOffer offer;

        @BeforeEach
        void setUpLoan() {
            offer = new LoanOffer("standard", new BigDecimal("0.01"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
        }

        private Share buyAndGetShare() {
            gameService.buy("EQNR", QUANTITY);
            return gameService.getPlayer().getPortfolio().getShares("EQNR").getFirst();
        }

        private int nextWeek() {
            return gameService.getExchange().getWeek() + 1;
        }

        @Test
        @DisplayName("sells shares and deducts obligations from cash")
        void executeForcedSale_sellsSharesAndPaysObligations() throws InsufficientSaleProceedsException, ExcessiveDebtException {
            // Arrange — buy shares then take a loan so there is interest due
            gameService.takeLoan(offer, new BigDecimal("500.00"));
            Share share = buyAndGetShare();
            BigDecimal obligations = gameService.getPlayer().getTotalObligationsThisWeek(nextWeek());
            BigDecimal moneyBefore = gameService.getPlayer().getCash();
            BigDecimal netNok = SalesCalculator.calculateNetNok(share, gameService.getCurrencyConverter());
            // Act
            gameService.executeForcedSale(List.of(share), nextWeek());
            // Assert — portfolio empty, cash = previous + net sale - obligations
            assertTrue(gameService.getPlayer().getPortfolio().getShares("EQNR").isEmpty());
            BigDecimal expectedMoney = moneyBefore.add(netNok).subtract(obligations);
            assertEquals(0, expectedMoney.compareTo(gameService.getPlayer().getCash()));
        }

        @Test
        @DisplayName("writes INTEREST ledger entries for each active loan")
        void executeForcedSale_writesInterestLedgerEntries() throws InsufficientSaleProceedsException, ExcessiveDebtException {
            // Arrange
            gameService.takeLoan(offer, new BigDecimal("500.00"));
            Share share = buyAndGetShare();
            // Act
            gameService.executeForcedSale(List.of(share), nextWeek());
            // Assert — one INTEREST entry recorded for the loan
            long interestEntries = gameService.getPlayer().getLoanLedger().stream()
                    .filter(e -> e.type() == LoanLedgerEntryType.INTEREST)
                    .count();
            assertEquals(1, interestEntries);
        }

        @Test
        @DisplayName("throws InsufficientSaleProceedsException when proceeds < obligations")
        void executeForcedSale_throwsWhenSharesDontCover() throws ExcessiveDebtException {
            // Arrange — player: 10000 NOK, capacity = 5000.
            // Loan: 4000 NOK at 50%/week → interest = 2000 NOK.
            // Selling 5 shares at 100 NOK (NOK stock): net ≈ 495 NOK < 2000 → throws.
            LoanOffer bigRate = new LoanOffer("big", new BigDecimal("0.50"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.HIGH);
            gameService.takeLoan(bigRate, new BigDecimal("4000.00"));
            Share share = buyAndGetShare();
            int nw = nextWeek();
            assertThrows(InsufficientSaleProceedsException.class, () ->
                    gameService.executeForcedSale(List.of(share), nw));
        }

        @Test
        @DisplayName("is all-or-nothing: no mutations happen when proceeds are insufficient")
        void executeForcedSale_isAllOrNothingOnInsufficientSale() throws ExcessiveDebtException {
            // Arrange — same as above: obligations >> share net sale value
            LoanOffer bigRate = new LoanOffer("big", new BigDecimal("0.50"), 10,
                    new BigDecimal("50000.00"), LoanRiskLevel.HIGH);
            gameService.takeLoan(bigRate, new BigDecimal("4000.00"));
            Share share = buyAndGetShare();
            BigDecimal moneyBefore = gameService.getPlayer().getCash();
            int weekBefore = gameService.getExchange().getWeek();
            int nw = nextWeek();
            // Act
            try {
                gameService.executeForcedSale(List.of(share), nw);
            } catch (InsufficientSaleProceedsException ignored) {}
            // Assert — no state changed
            assertEquals(weekBefore, gameService.getExchange().getWeek());
            assertEquals(0, moneyBefore.compareTo(gameService.getPlayer().getCash()));
            assertFalse(gameService.getPlayer().getPortfolio().getShares("EQNR").isEmpty());
        }

        @Test
        @DisplayName("advances the week and records total debt after forced sale")
        void executeForcedSale_advancesWeekAndRecordsTotalDebt() throws InsufficientSaleProceedsException, ExcessiveDebtException {
            // Arrange
            gameService.takeLoan(offer, new BigDecimal("200.00"));
            Share share = buyAndGetShare();
            int weekBefore = gameService.getExchange().getWeek();
            // Act
            gameService.executeForcedSale(List.of(share), nextWeek());
            // Assert — week advanced, debt history has a new entry
            assertEquals(weekBefore + 1, gameService.getExchange().getWeek());
            assertFalse(gameService.getPlayer().getTotalDebtHistory().isEmpty());
        }

        @Test
        @DisplayName("repays maturing loan and removes it from active loans")
        void executeForcedSale_repaysMaturingLoanOnDueWeek() throws InsufficientSaleProceedsException, ExcessiveDebtException {
            // Arrange — offer has term 10; loan taken at week 1, due at week 11 (nextWeek = 2 here)
            // Use a short-term offer instead: term = 1 so due at week 2 (nextWeek after setUp)
            LoanOffer shortOffer = new LoanOffer("short", new BigDecimal("0.01"), 1,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            gameService.takeLoan(shortOffer, new BigDecimal("200.00"));
            Share share = buyAndGetShare();
            // Act — nextWeek = 2, loan due at week 2
            gameService.executeForcedSale(List.of(share), nextWeek());
            // Assert — loan removed from active list
            assertTrue(gameService.getPlayer().getActiveLoans().isEmpty());
        }

        @Test
        @DisplayName("writes REPAYMENT ledger entry for maturing loan")
        void executeForcedSale_writesRepaymentEntryForMaturingLoan() throws InsufficientSaleProceedsException, ExcessiveDebtException {
            LoanOffer shortOffer = new LoanOffer("short", new BigDecimal("0.01"), 1,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            gameService.takeLoan(shortOffer, new BigDecimal("200.00"));
            Share share = buyAndGetShare();
            gameService.executeForcedSale(List.of(share), nextWeek());
            long repaymentEntries = gameService.getPlayer().getLoanLedger().stream()
                    .filter(e -> e.type() == LoanLedgerEntryType.REPAYMENT)
                    .count();
            assertEquals(1, repaymentEntries);
        }
    }

    @Nested
    @DisplayName("advanceWeek() with maturing loans")
    class AdvanceWeekMaturity {

        @Test
        @DisplayName("repays maturing loan and removes it from active loans")
        void advanceWeek_repaysMaturingLoan() throws ExcessiveDebtException {
            // Arrange — term 1 loan taken at week 1, due at week 2
            LoanOffer shortOffer = new LoanOffer("short", new BigDecimal("0.01"), 1,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            gameService.takeLoan(shortOffer, new BigDecimal("200.00"));
            // Act
            gameService.advanceWeek();
            // Assert — loan gone, week advanced
            assertTrue(gameService.getPlayer().getActiveLoans().isEmpty());
            assertEquals(2, gameService.getExchange().getWeek());
        }

        @Test
        @DisplayName("writes INTEREST and REPAYMENT entries for maturing loan")
        void advanceWeek_writesInterestAndRepaymentForMaturingLoan() throws ExcessiveDebtException {
            LoanOffer shortOffer = new LoanOffer("short", new BigDecimal("0.01"), 1,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            gameService.takeLoan(shortOffer, new BigDecimal("200.00"));
            gameService.advanceWeek();
            long interestCount = gameService.getPlayer().getLoanLedger().stream()
                    .filter(e -> e.type() == LoanLedgerEntryType.INTEREST).count();
            long repaymentCount = gameService.getPlayer().getLoanLedger().stream()
                    .filter(e -> e.type() == LoanLedgerEntryType.REPAYMENT).count();
            assertEquals(1, interestCount);
            assertEquals(1, repaymentCount);
        }

        @Test
        @DisplayName("deducts interest plus principal from cash on maturity week")
        void advanceWeek_deductsInterestAndPrincipalOnMaturityWeek() throws ExcessiveDebtException {
            LoanOffer shortOffer = new LoanOffer("short", new BigDecimal("0.01"), 1,
                    new BigDecimal("50000.00"), LoanRiskLevel.LOW);
            BigDecimal principal = new BigDecimal("200.00");
            gameService.takeLoan(shortOffer, principal);
            BigDecimal moneyAfterLoan = gameService.getPlayer().getCash(); // starting + principal
            BigDecimal interest = principal.multiply(new BigDecimal("0.01")).setScale(2, java.math.RoundingMode.HALF_UP);
            gameService.advanceWeek();
            BigDecimal expectedMoney = moneyAfterLoan.subtract(interest).subtract(principal);
            assertEquals(0, expectedMoney.compareTo(gameService.getPlayer().getCash()));
        }
    }

    @Nested
    @DisplayName("isGameOver() and declareGameOver()")
    class GameOverState {

        @Test
        @DisplayName("isGameOver() returns false when game is freshly loaded")
        void isGameOver_falseInitially() {
            assertFalse(gameService.isGameOver());
        }

        @Test
        @DisplayName("isGameOver() returns true after declareGameOver()")
        void isGameOver_trueAfterDeclare() {
            gameService.declareGameOver();
            assertTrue(gameService.isGameOver());
        }

        @Test
        @DisplayName("declareGameOver() notifies observers")
        void declareGameOver_notifiesObservers() {
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            int before = observer.updateCount;
            gameService.declareGameOver();
            assertEquals(before + 1, observer.updateCount);
        }

        @Test
        @DisplayName("buy() throws IllegalStateException when game is over")
        void buy_throwsWhenGameOver() {
            gameService.declareGameOver();
            assertThrows(IllegalStateException.class, () ->
                    gameService.buy("EQNR", BigDecimal.ONE));
        }

        @Test
        @DisplayName("sell() throws IllegalStateException when game is over")
        void sell_throwsWhenGameOver() {
            gameService.declareGameOver();
            Share share = new Share(gameService.getExchange().getStock("EQNR"),
                    BigDecimal.ONE, STOCK_PRICE);
            assertThrows(IllegalStateException.class, () ->
                    gameService.sell(share, BigDecimal.ONE));
        }

        @Test
        @DisplayName("advanceWeek() throws IllegalStateException when game is over")
        void advanceWeek_throwsWhenGameOver() {
            gameService.declareGameOver();
            assertThrows(IllegalStateException.class, () ->
                    gameService.advanceWeek());
        }

        @Test
        @DisplayName("takeLoan() throws IllegalStateException when game is over")
        void takeLoan_throwsWhenGameOver() {
            gameService.declareGameOver();
            LoanOffer offer = new LoanOffer("t", new BigDecimal("0.01"), 10,
                    new BigDecimal("9000.00"), LoanRiskLevel.LOW);
            assertThrows(IllegalStateException.class, () ->
                    gameService.takeLoan(offer, new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("repayLoan() throws IllegalStateException when game is over")
        void repayLoan_throwsWhenGameOver() throws ExcessiveDebtException {
            LoanOffer offer = new LoanOffer("t", new BigDecimal("0.01"), 10,
                    new BigDecimal("9000.00"), LoanRiskLevel.LOW);
            Loan loan = gameService.takeLoan(offer, new BigDecimal("100.00"));
            gameService.declareGameOver();
            assertThrows(IllegalStateException.class, () ->
                    gameService.repayLoan(loan));
        }

        @Test
        @DisplayName("isGameOver() resets to false when createNewGame() is called")
        void createNewGame_resetsGameOver() {
            gameService.declareGameOver();
            gameService.createNewGame("Fresh", new BigDecimal("5000.00"));
            assertFalse(gameService.isGameOver());
        }

        @Test
        @DisplayName("isGameOver() resets to false when loadGame() is called")
        void loadGame_resetsGameOver() throws GameSaveCorruptException {
            gameService.declareGameOver();
            Path file = tempDir.resolve("reset-save.json");
            Stock stock = new Stock("EQNR", "Equinor ASA",
                    new ArrayList<>(List.of(STOCK_PRICE)),
                    Currency.getInstance("NOK"));
            edu.ntnu.idatt2003.millions.model.exchange.Exchange exchange =
                    new edu.ntnu.idatt2003.millions.model.exchange.Exchange(
                            "NYSE", new ArrayList<>(List.of(stock)),
                            new FixedRateCurrencyConverter());
            Player player = new Player("Dara", STARTING_MONEY);
            new JsonGameFileHandler().saveGame(player, exchange, false, file.toFile());
            gameService.loadGame(file.toFile());
            assertFalse(gameService.isGameOver());
        }

        @Test
        @DisplayName("executeForcedSale() throws IllegalStateException when game is over")
        void executeForcedSale_throwsWhenGameOver() {
            // Arrange
            gameService.declareGameOver();
            // Act & Assert
            assertThrows(IllegalStateException.class, () ->
                    gameService.executeForcedSale(List.of(), 1));
        }
    }

    @Nested
    @DisplayName("sellAllAndExit()")
    class SellAllAndExit {

        @Test
        @DisplayName("liquidates portfolio, increases money, records RETIRED on leaderboard")
        void sellAllAndExitLiquidatesPortfolioAndRecordsRetired() {
            gameService.buy("EQNR", new BigDecimal("3"));
            assertFalse(gameService.getPlayer().getPortfolio().getShares().isEmpty(),
                    "player must own shares before sell-all");

            BigDecimal moneyBeforeSell = gameService.getPlayer().getCash();

            gameService.sellAllAndExit();

            assertTrue(gameService.getPlayer().getPortfolio().getShares().isEmpty(),
                    "portfolio must be empty after sell-all");
            assertTrue(gameService.getPlayer().getCash().compareTo(moneyBeforeSell) > 0,
                    "money must increase after liquidation");
            assertEquals(1, lbService.getAllEntries().size());
            assertEquals(Outcome.RETIRED, lbService.getAllEntries().get(0).outcome());
        }

        @Test
        @DisplayName("works with an empty portfolio — no shares to sell")
        void sellAllAndExitWithEmptyPortfolio() {
            assertTrue(gameService.getPlayer().getPortfolio().getShares().isEmpty());

            assertDoesNotThrow(() -> gameService.sellAllAndExit());

            assertEquals(Outcome.RETIRED, lbService.getAllEntries().get(0).outcome());
        }

        @Test
        @DisplayName("notifies observers")
        void sellAllAndExitNotifiesObservers() {
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            int before = observer.updateCount;
            gameService.sellAllAndExit();
            assertTrue(observer.updateCount > before);
        }

        @Test
        @DisplayName("works after declareGameOver — records BANKRUPTCY, not RETIRED")
        void sellAllAndExitAfterGameOver_recordsBankruptcy() {
            gameService.buy("EQNR", new BigDecimal("3"));
            gameService.declareGameOver();
            assertTrue(gameService.isGameOver());

            assertDoesNotThrow(() -> gameService.sellAllAndExit());

            assertTrue(gameService.getPlayer().getPortfolio().getShares().isEmpty());
            assertEquals(1, lbService.getAllEntries().size());
            assertEquals(Outcome.BANKRUPTCY, lbService.getAllEntries().get(0).outcome());
        }
    }

    @Nested
    @DisplayName("getCurrentSaveFile()")
    class GetCurrentSaveFile {

        @Test
        @DisplayName("Is empty for a brand-new game")
        void getCurrentSavePath_isEmptyForNewGame() {
            GameService fresh = new GameService(lbService);
            assertTrue(fresh.getCurrentSaveFile().isEmpty());
        }

        @Test
        @DisplayName("Is set after loadGame()")
        void getCurrentSavePath_isSetAfterLoadGame() throws GameSaveCorruptException {
            Path file = tempDir.resolve("save.json");
            gameService.loadGame(file.toFile());
            assertEquals(file.toFile(), gameService.getCurrentSaveFile().orElseThrow());
        }

        @Test
        @DisplayName("Is set after saveGame()")
        void getCurrentSavePath_isSetAfterFirstSave() {
            File file = tempDir.resolve("new-save.json").toFile();
            gameService.saveGame(file);
            assertEquals(file, gameService.getCurrentSaveFile().orElseThrow());
        }

        @Test
        @DisplayName("Is reset when a new game is created")
        void getCurrentSavePath_isResetOnNewGameCreated() {
            gameService.createNewGame("Ola", new BigDecimal("5000"));
            assertTrue(gameService.getCurrentSaveFile().isEmpty());
        }
    }

    @Nested
    @DisplayName("takeLoan()")
    class TakeLoan {

        @Test
        @DisplayName("Should throw NullPointerException when offer is null")
        void throwsWhenOfferIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.takeLoan(null, new BigDecimal("100")));
        }

        @Test
        @DisplayName("Should throw NullPointerException when amount is null")
        void throwsWhenAmountIsNull() {
            // Arrange
            LoanOffer offer = new LoanOffer("t", new BigDecimal("0.01"), 10,
                    new BigDecimal("9000.00"), LoanRiskLevel.LOW);
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.takeLoan(offer, null));
        }
    }

    @Nested
    @DisplayName("repayLoan()")
    class RepayLoan {

        @Test
        @DisplayName("Should throw NullPointerException when loan is null")
        void throwsWhenLoanIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.repayLoan(null));
        }
    }

    @Nested
    @DisplayName("addToWatchlist()")
    class AddToWatchlist {

        @Test
        @DisplayName("Should add a known stock to the player's watchlist")
        void addsKnownStockToWatchlist() {
            // Act & Assert
            gameService.addToWatchlist("EQNR");
            assertTrue(gameService.getPlayer().isOnWatchlist("EQNR"));
        }

        @Test
        @DisplayName("Should be a no-op when the symbol does not exist on the exchange")
        void isNoOpForUnknownSymbol() {
            // Act & Assert
            gameService.addToWatchlist("UNKNOWN");
            assertFalse(gameService.getPlayer().isOnWatchlist("UNKNOWN"));
        }

        @Test
        @DisplayName("Should throw NullPointerException when symbol is null")
        void throwsWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.addToWatchlist(null));
        }
    }

    @Nested
    @DisplayName("removeFromWatchlist()")
    class RemoveFromWatchlist {

        @Test
        @DisplayName("Should remove a watchlisted stock from the player's watchlist")
        void removesWatchlistedStock() {
            // Arrange
            gameService.addToWatchlist("EQNR");
            assertTrue(gameService.getPlayer().isOnWatchlist("EQNR"));
            // Act
            gameService.removeFromWatchlist("EQNR");
            // Assert
            assertFalse(gameService.getPlayer().isOnWatchlist("EQNR"));
        }

        @Test
        @DisplayName("Should throw NullPointerException when symbol is null")
        void throwsWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.removeFromWatchlist(null));
        }
    }

    @Nested
    @DisplayName("updateWatchlistNote()")
    class UpdateWatchlistNote {

        @Test
        @DisplayName("Should update the note for a watchlisted stock")
        void updatesNoteForWatchlistedStock() {
            // Arrange
            gameService.addToWatchlist("EQNR");
            // Act
            gameService.updateWatchlistNote("EQNR", "my note");
            // Assert
            String note = gameService.getPlayer().getWatchlist().stream()
                    .filter(e -> e.symbol().equals("EQNR"))
                    .findFirst()
                    .orElseThrow()
                    .note();
            assertEquals("my note", note);
        }

        @Test
        @DisplayName("Should throw NullPointerException when symbol is null")
        void throwsWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    gameService.updateWatchlistNote(null, "note"));
        }
    }

    @Nested
    @DisplayName("clearAllNotifications()")
    class ClearAllNotifications {

        @Test
        @DisplayName("Should clear all notifications and notify observers")
        void clearsNotificationsAndNotifiesObservers() throws ExcessiveDebtException {
            // Arrange — take a loan expiring in 3 weeks, advance week to push a notification
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 3,
                    new BigDecimal("50000"), LoanRiskLevel.LOW);
            gameService.takeLoan(offer, new BigDecimal("1000"));
            gameService.advanceWeek();
            assertFalse(gameService.getPlayer().getNotifications().isEmpty(),
                    "precondition: notifications must be non-empty after week advance with maturing loan");
            CountingObserver observer = new CountingObserver();
            gameService.addObserver(observer);
            // Act
            gameService.clearAllNotifications();
            // Assert
            assertTrue(gameService.getPlayer().getNotifications().isEmpty());
            assertEquals(1, observer.updateCount);
        }
    }

    @Nested
    @DisplayName("recordLeaderboardEntry()")
    class RecordLeaderboardEntry {

        @Test
        @DisplayName("Should record an ACTIVE entry for the current player")
        void recordsActiveEntryForCurrentPlayer() {
            // Act & Assert
            gameService.recordLeaderboardEntry();
            assertEquals(1, lbService.getAllEntries().size());
            assertEquals(Outcome.ACTIVE, lbService.getAllEntries().get(0).outcome());
        }

        @Test
        @DisplayName("Should be a no-op when no game is active")
        void isNoOpWhenNoGameIsActive() {
            // Arrange
            GameService fresh = new GameService(lbService);
            // Act
            fresh.recordLeaderboardEntry();
            // Assert
            assertTrue(lbService.getAllEntries().isEmpty());
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

    @Nested
    @DisplayName("Notification triggers")
    class NotificationTriggers {

        @Test
        @DisplayName("advanceWeek pushes notifications via service")
        void advanceWeek_pushesNotificationsViaService() {
            // Give the player a loan expiring in 3 weeks
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 3,
                    new BigDecimal("50000"), LoanRiskLevel.LOW);
            assertDoesNotThrow(() -> gameService.takeLoan(offer, new BigDecimal("1000")));
            int sizeBefore = gameService.getPlayer().getNotifications().size();

            gameService.advanceWeek();

            // At least loan-maturity notification should be pushed
            assertTrue(gameService.getPlayer().getNotifications().size() > sizeBefore);
        }

        @Test
        @DisplayName("advanceWeek does not push when game is over")
        void advanceWeek_doesNotPushWhenGameOver() {
            gameService.declareGameOver();

            assertThrows(IllegalStateException.class, () -> gameService.advanceWeek());
            assertEquals(0, gameService.getPlayer().getNotifications().size());
        }

        @Test
        @DisplayName("takeLoan pushes debt ratio notification when appropriate")
        void takeLoan_pushesDebtRatioNotificationWhenAppropriate() {
            // player netWorth ≈ 10000, capacity = 5000, 85% = 4250
            LoanOffer offer = new LoanOffer("std", new BigDecimal("0.001"), 20,
                    new BigDecimal("50000"), LoanRiskLevel.LOW);
            assertDoesNotThrow(() -> gameService.takeLoan(offer, new BigDecimal("4300")));

            assertTrue(gameService.getPlayer().getNotifications().stream()
                    .anyMatch(n -> n.titleKey().equals("notification.debtRatio.high.title")));
        }
    }
}
