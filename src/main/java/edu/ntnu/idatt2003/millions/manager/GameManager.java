package edu.ntnu.idatt2003.millions.manager;

import edu.ntnu.idatt2003.millions.file.game.GameFileHandler;
import edu.ntnu.idatt2003.millions.file.game.GameState;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.observer.GameObserver;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages the overall game lifecycle.
 * Responsible for creating new games, loading saved games,
 * saving the current game state, and advancing the game week.
 * Notifies registered {@link GameObserver}s when the game state changes.
 * Delegates file operations to {@link GameFileHandler}.
 */
public class GameManager {

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
     * Creates a new game with the given player name, starting capital
     * and stock data file.
     *
     * @param name      the name of the player
     * @param capital   the starting capital for the player
     * @param stockFile the file containing stock data to load
     */
    public void createNewGame(String name, BigDecimal capital, File stockFile) {
        // TODO: implementeres senere
    }

    /**
     * Loads a saved game state from a JSON file.
     * Delegates the file operation to the game file handler.
     *
     * @param file the file to load the game state from
     * @throws NullPointerException if the file is null
     */
    public void loadGame(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        GameState state = gameFileHandler.loadGame(file);
        this.player = state.player();
        this.exchange = state.exchange();
    }

    /**
     * Advances the game by one week and notifies all registered observers.
     */
    public void advanceWeek() {
        exchange.advance();
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
     * Notifies all registered observers that the game state has changed.
     */
    private void notifyObservers() {
        observers.forEach(GameObserver::onGameUpdated);
    }
}