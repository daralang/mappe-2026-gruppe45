package edu.ntnu.idatt2003.millions.observer;

/**
 * Observer for changes in the game state.
 * Implement this interface in any class that needs to update
 * when the game state changes, such as when the week advances,
 * a purchase or sale is made, or a game is loaded.
 */
public interface GameObserver {

    /**
     * Called when the game state has changed.
     * Implementing classes should update their UI or internal state accordingly.
     */
    void onGameUpdated();
}