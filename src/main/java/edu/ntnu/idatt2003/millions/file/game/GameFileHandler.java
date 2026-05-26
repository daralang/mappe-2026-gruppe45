// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;

import java.io.File;

/**
 * Interface for reading and writing game state to and from files.
 */
public interface GameFileHandler {

    /**
     * Saves the current game state to a file.
     *
     * @param player   the player whose state should be saved
     * @param exchange the exchange whose state should be saved
     * @param gameOver whether the game has ended
     * @param file     the file to save the game state to
     * @throws NullPointerException if player, exchange or file is null
     */
    void saveGame(Player player, Exchange exchange, boolean gameOver, File file);

    /**
     * Loads a saved game state from a file.
     *
     * @param file the file to load the game state from
     * @return a {@link GameState} containing the deserialized player and exchange
     * @throws NullPointerException     if the file is null
     * @throws GameSaveCorruptException if the file is not valid JSON or is missing
     *                                  required fields
     */
    GameState loadGame(File file) throws GameSaveCorruptException;
}
