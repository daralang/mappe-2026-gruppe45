package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;
import java.io.File;

/**
 * Interface for reading and writing game state to and from files.
 * Implementations may support different file formats, such as JSON.
 */
public interface GameFileHandler {

    /**
     * Saves the current game state to a file.
     *
     * @param player   the player whose state should be saved
     * @param exchange the exchange whose state should be saved
     * @param file     the file to save the game state to
     * @throws NullPointerException if player, exchange or file is null
     */
    void saveGame(Player player, Exchange exchange, File file);

    /**
     * Loads a saved game state from a file.
     *
     * @param file the file to load the game state from
     * @return a {@link GameState} containing the deserialized player and exchange
     * @throws NullPointerException if the file is null
     */
    GameState loadGame(File file);
}
