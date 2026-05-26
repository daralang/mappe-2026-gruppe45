// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.file.game;

import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.player.Player;

/**
 * Snapshot of the full game state returned by {@link GameFileHandler#loadGame}.
 *
 * @param player   the deserialized player
 * @param exchange the deserialized exchange
 * @param gameOver whether the game had ended when the save was written
 */
public record GameState(Player player, Exchange exchange, boolean gameOver) {
}
