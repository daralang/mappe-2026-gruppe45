// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service;

import edu.ntnu.idatt2003.millions.file.game.GameFileHandler;
import edu.ntnu.idatt2003.millions.file.game.GameSaveCorruptException;
import edu.ntnu.idatt2003.millions.file.game.GameState;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.CsvStockFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.InvalidStockDataException;
import edu.ntnu.idatt2003.millions.file.stock.StockFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.watchlist.WatchlistEntry;
import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.InsufficientSaleProceedsException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.model.leaderboard.Outcome;
import edu.ntnu.idatt2003.millions.service.LeaderboardService;
import edu.ntnu.idatt2003.millions.service.notification.NotificationService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service-layer manager for the game lifecycle: creates new games, loads and
 * saves game state, and advances the game week. Owns the active {@link Player}
 * and {@link Exchange}, and notifies registered {@link GameObserver}s after
 * every state-changing operation. Delegates persistence to {@link GameFileHandler}.
 *
 * <p>{@code createNewGame} validates player input before performing any file I/O
 * and only swaps in the new state once the player and exchange are fully
 * constructed, so failures leave the previous game intact.</p>
 */
public class GameService {

    private static final Logger LOGGER = Logger.getLogger(GameService.class.getName());

    private static final String DEFAULT_EXCHANGE_NAME = "MainExchange";
    private static final String DEFAULT_STOCK_RESOURCE = "/data/sp500.csv";
    private static final Currency DEFAULT_STOCK_CURRENCY = Currency.getInstance("USD");

    private Player player;
    private Exchange exchange;
    private boolean gameOver = false;
    private File currentSaveFile = null;
    private final GameFileHandler gameFileHandler;
    private final List<GameObserver> observers = new ArrayList<>();
    private final NotificationService notificationService = new NotificationService();
    private final LeaderboardService leaderboardService;

    /**
     * Default constructor — uses production {@link LeaderboardService}
     * that reads and writes {@code leaderboard.json} in the project root.
     */
    public GameService() {
        this(new LeaderboardService());
    }

    /**
     * Test-friendly constructor — accepts an injected
     * {@link LeaderboardService} so tests can redirect leaderboard
     * persistence to a temporary file.
     *
     * @param leaderboardService the leaderboard service to use
     * @throws NullPointerException if {@code leaderboardService} is null
     */
    public GameService(LeaderboardService leaderboardService) {
        this.leaderboardService = Objects.requireNonNull(
                leaderboardService, "leaderboardService cannot be null");
        this.gameFileHandler = new JsonGameFileHandler();
    }

    /**
     * Registers an observer to be notified when the game state changes.
     *
     * @param observer the observer to register
     * @throws NullPointerException if observer is null
     */
    public void addObserver(GameObserver observer) {
        Objects.requireNonNull(observer, "Observer cannot be null");
        observers.add(observer);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Marks the game as over and notifies observers. After this call, all trading
     * and week-advance operations throw {@link IllegalStateException}.
     */
    public void declareGameOver() {
        this.gameOver = true;
        if (player != null && exchange != null) {
            leaderboardService.recordOrUpdate(
                    player, exchange, exchange.getCurrencyConverter(), Outcome.BANKRUPTCY);
            if (currentSaveFile != null) {
                try {
                    gameFileHandler.saveGame(player, exchange, gameOver, currentSaveFile);
                } catch (UncheckedIOException e) {
                    LOGGER.log(Level.WARNING, "Could not write save file on bankruptcy", e);
                } catch (RuntimeException e) {
                    LOGGER.log(Level.SEVERE, "Unexpected error writing save file on bankruptcy", e);
                }
            }
        }
        notifyObservers();
    }

    /**
     * Saves the current game state to a JSON file.
     *
     * @param file the file to save the game state to
     * @throws NullPointerException  if the file is null
     * @throws IllegalStateException if no active game exists
     */
    public void saveGame(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        if (player == null || exchange == null) {
            throw new IllegalStateException("No active game to save");
        }
        gameFileHandler.saveGame(player, exchange, gameOver, file);
        this.currentSaveFile = file;
        try {
            leaderboardService.recordOrUpdate(player, exchange, exchange.getCurrencyConverter(),
                    gameOver ? Outcome.BANKRUPTCY : Outcome.ACTIVE);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Could not update leaderboard after save", e);
        }
    }

    /**
     * Returns the file used for the most recent save or load in this session,
     * or empty if no save has been made and no game has been loaded yet.
     *
     * @return an Optional containing the current save file, or empty if none
     */
    public Optional<File> getCurrentSaveFile() {
        return Optional.ofNullable(currentSaveFile);
    }

    /**
     * Clears the stored save path so the next save will prompt for a location.
     */
    public void clearCurrentSaveFile() {
        this.currentSaveFile = null;
    }

    /**
     * Creates a new game with the given player name and starting capital.
     *
     * <p>Validates the player input first by constructing a {@link Player},
     * then loads default stock data from {@link #DEFAULT_STOCK_RESOURCE} and
     * instantiates an {@link Exchange}. The active game state is only mutated
     * once both steps succeed, leaving any previous game intact on failure.
     * Observers are notified once the new game state is active.</p>
     *
     * @param name    the name of the player
     * @param capital the starting capital for the player, in NOK
     * @throws NullPointerException     if name or capital is null
     * @throws IllegalArgumentException if name is blank, capital is negative,
     *                                  or the default stock file contains no stocks
     * @throws IllegalStateException    if the default stock data cannot be found
     * @throws UncheckedIOException     if the default stock data cannot be read
     */
    public void createNewGame(String name, BigDecimal capital) {
        Player newPlayer = new Player(name, capital);
        List<Stock> stocks = loadDefaultStocks();
        activate(newPlayer, stocks);
    }

    /**
     * Convenience overload of {@link #createNewGame(String, BigDecimal, File, Currency)}
     * that defaults the stock currency to USD.
     *
     * @param name      the player name
     * @param capital   the starting capital, in NOK
     * @param stockFile the CSV file with stock data
     * @throws NullPointerException      if any argument is null
     * @throws IllegalArgumentException  if name is blank, capital is negative,
     *                                   or the file contains no stocks
     * @throws InvalidStockDataException if the stock file contains malformed or no valid entries
     */
    public void createNewGame(String name, BigDecimal capital, File stockFile)
            throws InvalidStockDataException {
        createNewGame(name, capital, stockFile, DEFAULT_STOCK_CURRENCY);
    }

    /**
     * Creates a new game from the given stock file, tagging every parsed
     * {@link Stock} with {@code currency} so the {@link Exchange} converts
     * prices to NOK on every trade.
     *
     * @param name      the player name
     * @param capital   the starting capital, in NOK
     * @param stockFile the CSV file with stock data
     * @param currency  the currency the stock prices are quoted in
     * @throws NullPointerException      if any argument is null
     * @throws IllegalArgumentException  if name is blank, capital is negative,
     *                                   or the file contains no stocks
     * @throws InvalidStockDataException if the stock file contains malformed or no valid entries
     */
    public void createNewGame(String name, BigDecimal capital, File stockFile, Currency currency)
            throws InvalidStockDataException {
        Objects.requireNonNull(stockFile, "Stock file cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Player newPlayer = new Player(name, capital);
        StockFileHandler stockFileHandler = new CsvStockFileHandler();
        List<Stock> stocks = stockFileHandler.readStocks(stockFile.toPath(), currency);
        activate(newPlayer, stocks);
    }

    private void activate(Player newPlayer, List<Stock> stocks) {
        this.player = newPlayer;
        this.exchange = new Exchange(DEFAULT_EXCHANGE_NAME, stocks, new FixedRateCurrencyConverter());
        this.gameOver = false;
        this.currentSaveFile = null;
        notifyObservers();
    }

    private List<Stock> loadDefaultStocks() {
        StockFileHandler stockFileHandler = new CsvStockFileHandler();
        try (InputStream inputStream = GameService.class.getResourceAsStream(DEFAULT_STOCK_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Default stock data not found: " + DEFAULT_STOCK_RESOURCE);
            }
            return stockFileHandler.readStocks(inputStream, DEFAULT_STOCK_CURRENCY);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read default stock data", e);
        } catch (InvalidStockDataException e) {
            throw new IllegalStateException("Default stock data is malformed: " + e.getMessage(), e);
        }
    }

    /**
     * Loads a saved game state from a JSON file and notifies observers.
     *
     * @param file the file to load the game state from
     * @throws NullPointerException     if the file is null
     * @throws GameSaveCorruptException if the save file is corrupt or has missing fields
     */
    public void loadGame(File file) throws GameSaveCorruptException {
        Objects.requireNonNull(file, "File cannot be null");
        GameState state = gameFileHandler.loadGame(file);
        this.player = state.player();
        this.exchange = state.exchange();
        this.exchange.reinitialize(new FixedRateCurrencyConverter());
        this.gameOver = state.gameOver();
        this.currentSaveFile = file;
        notifyObservers();
    }

    /**
     * Buys the given quantity of a stock for the current player.
     *
     * @param symbol   the symbol of the stock to buy
     * @param quantity the quantity to buy
     * @return the completed purchase transaction
     * @throws IllegalStateException if the game is over
     */
    public Transaction buy(String symbol, BigDecimal quantity) {
        if (gameOver) throw new IllegalStateException("Game is over");
        Transaction transaction = exchange.buy(symbol, quantity, player);
        notifyObservers();
        return transaction;
    }

    /**
     * Sells the full quantity of a share for the current player.
     *
     * @param share the share to sell
     * @return the completed sale transaction
     * @throws NullPointerException  if share is null
     * @throws IllegalStateException if the game is over
     */
    public Transaction sell(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        return sell(share, share.getQuantity());
    }

    /**
     * Sells the given quantity of a share for the current player. When the
     * quantity is less than the share's full position, the remainder stays
     * in the player's portfolio with the original purchase price preserved.
     *
     * @param share    the share to sell from
     * @param quantity the quantity to sell
     * @return the completed sale transaction
     * @throws IllegalStateException if the game is over
     */
    public Transaction sell(Share share, BigDecimal quantity) {
        if (gameOver) throw new IllegalStateException("Game is over");
        Transaction transaction = exchange.sell(share, quantity, player);
        notifyObservers();
        return transaction;
    }

    /**
     * Advances the game by one week and notifies all registered observers.
     * Assumes the player has enough cash to cover all obligations (interest +
     * any maturing loan principals); use {@link #executeForcedSale} instead
     * when they cannot.
     *
     * @throws IllegalStateException if the game is over
     */
    public void advanceWeek() {
        if (gameOver) throw new IllegalStateException("Game is over");
        CurrencyConverter converter = exchange.getCurrencyConverter();
        player.setPreviousNetWorth(player.getNetWorth(converter));
        exchange.advance();
        int week = exchange.getWeek();
        player.collectWeeklyInterest(week);
        for (Loan loan : player.getLoansDueThisWeek(week)) {
            player.repayLoan(loan, week);
        }
        finishWeekAdvance();
    }

    /**
     * Sells the given shares, deducts all weekly obligations (interest plus any
     * maturing loan principals) from the player's cash, advances the week, and
     * notifies observers.
     *
     * <p>All-or-nothing: if the combined net sale value (after commission and tax,
     * converted to NOK) is less than the total obligations for {@code currentWeek},
     * an {@link InsufficientSaleProceedsException} is thrown and no state is mutated.
     *
     * @param shares      the shares the player has chosen to sell
     * @param currentWeek the game week being processed (the week after the current one)
     * @throws InsufficientSaleProceedsException if the net sale total is less than total obligations
     * @throws IllegalStateException             if the game is over
     */
    public void executeForcedSale(List<Share> shares, int currentWeek)
            throws InsufficientSaleProceedsException {
        if (gameOver) throw new IllegalStateException("Game is over");
        CurrencyConverter converter = exchange.getCurrencyConverter();

        List<Loan> maturingLoans = player.getLoansDueThisWeek(currentWeek);
        BigDecimal totalObligations = player.getTotalObligationsThisWeek(currentWeek);

        BigDecimal netTotal = shares.stream()
                .map(s -> SalesCalculator.calculateNetNok(s, converter))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (netTotal.compareTo(totalObligations) < 0) {
            throw new InsufficientSaleProceedsException(totalObligations.subtract(netTotal));
        }

        player.setPreviousNetWorth(player.getNetWorth(converter));

        for (Share share : shares) {
            exchange.sell(share, player);
        }
        player.withdrawMoney(totalObligations);

        exchange.advance();
        int week = exchange.getWeek();
        player.writeInterestLedgerEntries(week);
        for (Loan loan : maturingLoans) {
            player.settleMatureLoan(loan, week);
        }
        finishWeekAdvance();
    }

    private void finishWeekAdvance() {
        CurrencyConverter converter = exchange.getCurrencyConverter();
        player.recordNetWorth(converter);
        player.recordTotalDebt();
        notificationService.onWeekAdvanced(player, exchange, converter);
        notifyObservers();
    }

    public Player getPlayer() {
        return player;
    }

    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Returns the currency converter from the active exchange.
     * Convenience shortcut for {@code getExchange().getCurrencyConverter()}.
     *
     * @return the active currency converter
     */
    public CurrencyConverter getCurrencyConverter() {
        return exchange.getCurrencyConverter();
    }

    /**
     * Returns the player's net worth from before the last week advance.
     * Returns null if the week has not been advanced yet.
     *
     * @return the previous net worth, or null if not yet available
     */
    public BigDecimal getPreviousNetWorth() {
        return player.getPreviousNetWorth();
    }

    /**
     * Creates a loan against the given offer and disburses the principal to the player.
     *
     * @param offer  the loan offer the player is accepting
     * @param amount the principal to disburse; must be positive and within offer limits
     * @return the created {@link Loan}
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if amount exceeds the offer's maximum principal
     * @throws IllegalStateException    if the game is over
     * @throws edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException
     *         if the loan would breach the player's debt-to-net-worth limit
     */
    public Loan takeLoan(LoanOffer offer, BigDecimal amount) throws ExcessiveDebtException {
        if (gameOver) throw new IllegalStateException("Game is over");
        Objects.requireNonNull(offer, "Offer cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Loan loan = new Loan(offer, amount, exchange.getWeek());
        player.takeLoan(loan, exchange.getCurrencyConverter());
        notificationService.onLoanTaken(player, exchange.getWeek(), exchange.getCurrencyConverter());
        notifyObservers();
        return loan;
    }

    /**
     * Repays the given loan in full, withdrawing the principal from the player's
     * cash balance and removing the loan from their active list.
     *
     * @param loan the loan to repay
     * @throws NullPointerException     if loan is null
     * @throws IllegalArgumentException if the player cannot afford the repayment
     * @throws IllegalStateException    if the game is over
     */
    public void repayLoan(Loan loan) {
        if (gameOver) throw new IllegalStateException("Game is over");
        Objects.requireNonNull(loan, "Loan cannot be null");
        player.repayLoan(loan, exchange.getWeek());
        notifyObservers();
    }

    /**
     * Liquidates the entire portfolio at current market prices, records a
     * {@link Outcome#RETIRED} leaderboard entry, and persists the final state
     * to the current save file if one exists. The caller is responsible for
     * closing the application after this returns.
     */
    public void sellAllAndExit() {
        for (Share share : new ArrayList<>(player.getPortfolio().getShares())) {
            exchange.sell(share, player);
        }
        leaderboardService.recordOrUpdate(
                player, exchange, exchange.getCurrencyConverter(),
                gameOver ? Outcome.BANKRUPTCY : Outcome.RETIRED);
        if (currentSaveFile != null) {
            try {
                gameFileHandler.saveGame(player, exchange, gameOver, currentSaveFile);
            } catch (UncheckedIOException e) {
                LOGGER.log(Level.WARNING, "Could not write save file on retire", e);
            } catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE, "Unexpected error writing save file on retire", e);
            }
        }
        notifyObservers();
    }

    /**
     * Records the player's current standing to the leaderboard as an active game.
     * No-op if no game is currently active.
     */
    public void recordLeaderboardEntry() {
        if (player != null && exchange != null) {
            leaderboardService.recordOrUpdate(player, exchange, exchange.getCurrencyConverter(),
                    gameOver ? Outcome.BANKRUPTCY : Outcome.ACTIVE);
        }
    }

    /**
     * Clears all notifications from the current player and notifies observers.
     * No-op if no game is active.
     */
    public void clearAllNotifications() {
        if (player == null) return;
        player.clearAllNotifications();
        notifyObservers();
    }

    /**
     * Adds the given stock symbol to the player's watchlist at the current week.
     * No-op if the symbol is already on the watchlist or does not exist on the {@link Exchange}.
     *
     * @param symbol the ticker symbol to add
     * @throws NullPointerException if symbol is null
     */
    public void addToWatchlist(String symbol) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (player == null || !exchange.hasStock(symbol)) return;
        player.addToWatchlist(new WatchlistEntry(symbol, exchange.getWeek(), ""));
        notifyObservers();
    }

    /**
     * Removes the given stock symbol from the player's watchlist.
     * No-op if the symbol is not on the watchlist.
     *
     * @param symbol the ticker symbol to remove
     * @throws NullPointerException if symbol is null
     */
    public void removeFromWatchlist(String symbol) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (player == null) return;
        player.removeFromWatchlist(symbol);
        notifyObservers();
    }

    /**
     * Updates the note for the given symbol in the player's watchlist.
     * No-op if the symbol is not on the watchlist.
     *
     * @param symbol  the ticker symbol of the entry to update
     * @param newNote the new note text
     * @throws NullPointerException if symbol is null
     */
    public void updateWatchlistNote(String symbol, String newNote) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (player == null) return;
        player.updateWatchlistNote(symbol, newNote);
        notifyObservers();
    }

    private void notifyObservers() {
        observers.forEach(GameObserver::onGameUpdated);
    }
}