// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.controller;

import java.util.function.BooleanSupplier;

/**
 * Bundles the four end-of-game action callbacks passed to
 * {@link GameOverController} and forwarded through the modal/dialog chain.
 *
 * @param onNewGame     pure navigation executed after the current session ends
 *                      (e.g. return to start screen, close the window)
 * @param saveAction    saves the game; returns {@code true} on success so
 *                      callers can gate navigation on a successful save
 * @param sellAllAction liquidates all positions and records the leaderboard
 *                      entry before navigating
 * @param noSaveAction  pre-navigation action for the no-save path
 *                      (e.g. {@code gameService::recordLeaderboardEntry})
 */
public record EndGameActions(
        Runnable onNewGame,
        BooleanSupplier saveAction,
        Runnable sellAllAction,
        Runnable noSaveAction
) {}
