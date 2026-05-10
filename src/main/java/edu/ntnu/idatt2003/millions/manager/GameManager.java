package edu.ntnu.idatt2003.millions.manager;

import edu.ntnu.idatt2003.millions.file.game.GameFileHandler;
import edu.ntnu.idatt2003.millions.file.game.GameState;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.CsvStockFileHandler;
import edu.ntnu.idatt2003.millions.file.stock.StockFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.observer.GameObserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Service-layer manager for the game lifecycle: creates new games, loads and
 * saves game state, and advances the game week. Owns the active {@link Player}
 * and {@link Exchange}, and notifies registered {@link GameObserver}s after
 * every state-changing operation. Delegates persistence to {@link GameFileHandler}.
 *
 * <p>{@code createNewGame} validates player input before performing any file I/O
 * and only swaps in the new state once the player and exchange are fully
 * constructed, so failures leave the previous game intact. The four-argument
 * overload accepts the {@link Currency} of the stock prices so the
 * {@link Exchange} can convert to NOK through its {@link CurrencyConverter};
 * the three-argument overload defaults to USD.</p>
 *
 * <p>Also exposes facade query methods (net worth, weekly change, status,
 * portfolio value) so views can read derived values without composing
 * {@link Player} and {@link Exchange} through the converter themselves.</p>
 */
public class GameManager {

    private static final String DEFAULT_EXCHANGE_NAME = "MainExchange";
    private static final String DEFAULT_STOCK_RESOURCE = "/data/sp500.csv";
    private static final Currency DEFAULT_STOCK_CURRENCY = Currency.getInstance("USD");

    private Player player;
    private Exchange exchange;
    private final GameFileHandler gameFileHandler;
    private final List<GameObserver> observers = new ArrayList<>();

    /**
     * Constructs a new GameManager.
     * Initializes the file handler for JSON serialization.
     */
    public GameManager() {
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

    /**
     * Saves the current game state to a JSON file.
     * Delegates the file operation to the game file handler.
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
        gameFileHandler.saveGame(player, exchange, file);
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
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if name is blank, capital is negative,
     *                                  or the file contains no stocks
     */
    public void createNewGame(String name, BigDecimal capital, File stockFile) {
        createNewGame(name, capital, stockFile, DEFAULT_STOCK_CURRENCY);
    }

    /**
     * Creates a new game from the given stock file, tagging every parsed
     * {@link Stock} with {@code currency} so the {@link Exchange} converts
     * prices to NOK on every trade. See class-level Javadoc for validation
     * order and observer semantics.
     *
     * @param name      the player name
     * @param capital   the starting capital, in NOK
     * @param stockFile the CSV file with stock data
     * @param currency  the currency the stock prices are quoted in
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if name is blank, capital is negative,
     *                                  or the file contains no stocks
     */
    public void createNewGame(String name, BigDecimal capital, File stockFile, Currency currency) {
        Objects.requireNonNull(stockFile, "Stock file cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        Player newPlayer = new Player(name, capital);
        StockFileHandler stockFileHandler = new CsvStockFileHandler();
        List<Stock> stocks = stockFileHandler.readStocks(stockFile.toPath(), currency);
        activate(newPlayer, stocks);
    }

    /**
     * Activates a new game state once the player and stocks have been
     * successfully constructed and loaded. Replaces the active player and
     * exchange atomically and notifies observers.
     *
     * @param newPlayer the validated player instance
     * @param stocks    the stocks to list on the exchange
     */
    private void activate(Player newPlayer, List<Stock> stocks) {
        this.player = newPlayer;
        this.exchange = new Exchange(DEFAULT_EXCHANGE_NAME, stocks, new FixedRateCurrencyConverter());
        notifyObservers();
    }

    /**
     * Loads default stock data from the application resources.
     * Streams the resource directly through the {@link StockFileHandler}
     * so no intermediate file is written to disk.
     *
     * @return the default stocks for a new game
     * @throws IllegalStateException if the default stock data cannot be found
     * @throws UncheckedIOException  if the default stock data cannot be read
     */
    private List<Stock> loadDefaultStocks() {
        StockFileHandler stockFileHandler = new CsvStockFileHandler();
        try (InputStream inputStream = GameManager.class.getResourceAsStream(DEFAULT_STOCK_RESOURCE)) {
            if (inputStream == null) {
                throw new IllegalStateException("Default stock data not found: " + DEFAULT_STOCK_RESOURCE);
            }
            return stockFileHandler.readStocks(inputStream, DEFAULT_STOCK_CURRENCY);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read default stock data", e);
        }
    }

    /**
     * Loads a saved game state from a JSON file.
     * Delegates the file operation to the game file handler and reinitializes
     * the exchange with a {@link FixedRateCurrencyConverter}, since the
     * converter is transient and not restored by Gson. Notifies observers
     * once the loaded state is in place.
     *
     * @param file the file to load the game state from
     * @throws NullPointerException if the file is null
     */
    public void loadGame(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        GameState state = gameFileHandler.loadGame(file);
        this.player = state.player();
        this.exchange = state.exchange();
        this.exchange.reinitialize(new FixedRateCurrencyConverter());
        notifyObservers();
    }

    /**
     * Buys the given quantity of a stock for the current player.
     * Delegates the actual transaction to {@link Exchange} and notifies
     * observers on success.
     *
     * @param symbol   the symbol of the stock to buy
     * @param quantity the quantity to buy
     * @return the completed purchase transaction
     */
    public Transaction buy(String symbol, BigDecimal quantity) {
        Transaction transaction = exchange.buy(symbol, quantity, player);
        notifyObservers();
        return transaction;
    }

    /**
     * Sells the given share for the current player.
     * Delegates the actual transaction to {@link Exchange} and notifies
     * observers on success.
     *
     * @param share the share to sell
     * @return the completed sale transaction
     */
    public Transaction sell(Share share) {
        Transaction transaction = exchange.sell(share, player);
        notifyObservers();
        return transaction;
    }

    /**
     * Advances the game by one week and notifies all registered observers.
     * Records the player's current net worth before advancing so that
     * weekly change and historical net worth data are available after
     * the week has passed. The {@link CurrencyConverter} is fetched from
     * the {@link Exchange} so the player's portfolio value can be translated
     * to NOK.
     */
    public void advanceWeek() {
        CurrencyConverter converter = exchange.getCurrencyConverter();
        player.setPreviousNetWorth(player.getNetWorth(converter));
        exchange.advance();
        player.recordNetWorth(converter);
        notifyObservers();
    }

    /**
     * Returns the current player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the current exchange.
     *
     * @return the exchange
     */
    public Exchange getExchange() {
        return exchange;
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
     * Returns the player's current net worth in NOK.
     * Facade method that fetches the converter from the active {@link Exchange}
     * and delegates to {@link Player}.
     *
     * @return the player's net worth in NOK
     */
    public BigDecimal getPlayerNetWorth() {
        return player.getNetWorth(exchange.getCurrencyConverter());
    }

    /**
     * Returns the absolute change in the player's net worth since the start of the game,
     * in NOK. Facade method that delegates to {@link Player}.
     *
     * @return current net worth minus starting money, in NOK
     */
    public BigDecimal getPlayerNetWorthChangeSinceStart() {
        return player.getNetWorthChangeSinceStart(exchange.getCurrencyConverter());
    }

    /**
     * Returns the percentage change in the player's net worth since the start of the game.
     * Facade method that delegates to {@link Player}.
     *
     * @return percent change since start, e.g. 12.5 means +12.5%
     */
    public BigDecimal getPlayerNetWorthChangePercentSinceStart() {
        return player.getNetWorthChangePercentSinceStart(exchange.getCurrencyConverter());
    }

    /**
     * Returns the absolute change in the player's net worth since the previous week, in NOK.
     * Returns null if no week has been advanced yet. Facade method that delegates to {@link Player}.
     *
     * @return current net worth minus previous net worth, or null if not available
     */
    public BigDecimal getPlayerWeeklyNetWorthChange() {
        return player.getWeeklyNetWorthChange(exchange.getCurrencyConverter());
    }

    /**
     * Returns the percentage change in the player's net worth since the previous week.
     * Returns null if no week has been advanced yet. Facade method that delegates to {@link Player}.
     *
     * @return percent change since last week, or null if not available
     */
    public BigDecimal getPlayerWeeklyNetWorthChangePercent() {
        return player.getWeeklyNetWorthChangePercent(exchange.getCurrencyConverter());
    }

    /**
     * Returns the player's current status level. Facade method that delegates to
     * {@link Player#getStatus(CurrencyConverter)}.
     *
     * @return the player's status level
     */
    public PlayerStatusLevel getPlayerStatus() {
        return player.getStatus(exchange.getCurrencyConverter());
    }

    /**
     * Returns the total value of the player's portfolio in NOK.
     * Facade method that delegates to {@link Player}.
     *
     * @return the portfolio value in NOK
     */
    public BigDecimal getPortfolioValue() {
        return player.getPortfolio().getNetWorth(exchange.getCurrencyConverter());
    }

    /**
     * Notifies all registered observers that the game state has changed.
     */
    private void notifyObservers() {
        observers.forEach(GameObserver::onGameUpdated);
    }
}
