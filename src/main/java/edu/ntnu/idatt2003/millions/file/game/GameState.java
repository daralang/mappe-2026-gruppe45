package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;

/**
 * Represents the full game state returned after loading a saved game.
 * Acts as a simple data container for transferring the player and exchange
 * state from a file handler to managers.
 * Used as the return type of {@link GameFileHandler#loadGame(java.io.File)}.
 *
 * @param player   the player state loaded from file
 * @param exchange the exchange state loaded from file
 * @param gameOver whether the game had ended (bankruptcy) when it was saved
 */
public record GameState(Player player, Exchange exchange, boolean gameOver) {
}
