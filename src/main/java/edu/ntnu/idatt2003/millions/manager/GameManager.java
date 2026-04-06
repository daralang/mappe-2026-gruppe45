package edu.ntnu.idatt2003.millions.manager;

import edu.ntnu.idatt2003.millions.file.game.GameFileHandler;
import edu.ntnu.idatt2003.millions.file.game.GameState;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import java.io.File;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Manages the overall game lifecycle.
 * Responsible for creating new games, loading saved games,
 * saving the current game state ... TO BE CONTINUED
 * Delegates file operations to GameFileHandler.
 */
public class GameManager {

    private Player player;
    private Exchange exchange;
    private final GameFileHandler gameFileHandler;

    /**
     * Constructs a new GameManager.
     * Initializes the file handler for JSON serialization.
     */
    public GameManager() {
        this.gameFileHandler = new JsonGameFileHandler();
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
     * @param name       the name of the player
     * @param capital    the starting capital for the player
     * @param stockFile  the file containing stock data to load
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

    public void advanceWeek() {
        exchange.advance();
    }
}