package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.layout.VBox;

/**
 * Abstract base class for all card components in the application.
 * Provides consistent styling and registers itself as a {@link GameObserver}
 * so subclasses can react to game state changes.
 */
public abstract class Card extends VBox implements GameObserver {

    /**
     * Constructs a new Card and registers itself as a game observer.
     *
     * @param gameManager the game manager to observe
     */
    protected Card(GameManager gameManager) {
        getStyleClass().add("card");
        gameManager.addObserver(this);
        LanguageManager.addObserver(this::onLanguageChanged);
    }

    /**
     * Called when the language changes.
     * Subclasses must implement this to update their displayed text.
     */
    protected abstract void onLanguageChanged();

    /**
     * Called when the game state has changed.
     * Subclasses must implement this to update their displayed data.
     */
    @Override
    public abstract void onGameUpdated();
}