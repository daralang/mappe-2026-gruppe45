package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.Exchange;
import edu.ntnu.idatt2003.millions.model.Player;
import java.io.File;

/**
 * Interface for reading and writing game state to and from files.
 * Implementations may support different file formats, such as JSON.
 */
public interface GameFileHandler {

    void saveGame(Player player, Exchange exchange, File file);

    GameState loadGame(File file);
}
