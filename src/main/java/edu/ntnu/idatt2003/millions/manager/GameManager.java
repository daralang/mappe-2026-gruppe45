package edu.ntnu.idatt2003.millions.manager;

import edu.ntnu.idatt2003.millions.file.GameFileHandler;
import edu.ntnu.idatt2003.millions.file.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.Exchange;
import edu.ntnu.idatt2003.millions.model.Player;
import java.io.File;
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

    public GameManager() {
        this.gameFileHandler = new JsonGameFileHandler();
    }

    public void saveGame(File file) {
        Objects.requireNonNull(file, "File cannot be null");

        // IllegalStateException - for internal state
        if (player == null || exchange == null) {
            throw new IllegalStateException("No active game to save");
        }
        gameFileHandler.saveGame(player, exchange, file);
    }

    public void createNewGame(String name, String capital, File stockFile) {
        // TODO: implementeres senere
    }

    public void loadGame(File file) {
        gameFileHandler.loadGame(file);
    }
}