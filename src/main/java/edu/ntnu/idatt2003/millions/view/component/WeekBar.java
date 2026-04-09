package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A reusable component displaying the current week and an advance week button.
 * Implements {@link GameObserver} to automatically update the week number
 * when the game state changes.
 */
public class WeekBar extends HBox implements GameObserver {

    private final GameManager gameManager;
    private final Label weekLabel;
    private final Button advanceButton;

    /**
     * Constructs a new WeekBar and registers itself as a game observer.
     *
     * @param gameManager the game manager to observe
     */
    public WeekBar(GameManager gameManager) {
        this.gameManager = gameManager;
        gameManager.addObserver(this);

        setSpacing(24);
        setAlignment(Pos.CENTER_RIGHT);

        weekLabel = new Label(LanguageManager.get("app.week") + " "
                + gameManager.getExchange().getWeek());
        weekLabel.getStyleClass().add("week-label");

        advanceButton = new Button(LanguageManager.get("app.advanceWeek"));
        advanceButton.getStyleClass().add("advance-button");

        getChildren().addAll(weekLabel, advanceButton);

        LanguageManager.addObserver(this::onLanguageChanged);
    }

    /**
     * Updates text elements to the current language.
     * Called automatically when the language changes.
     */
    private void onLanguageChanged() {
        advanceButton.setText(LanguageManager.get("app.advanceWeek"));
        weekLabel.setText(LanguageManager.get("app.week") + " "
                + gameManager.getExchange().getWeek());
    }

    /**
     * Updates the week label when the game state changes.
     */
    @Override
    public void onGameUpdated() {
        weekLabel.setText(LanguageManager.get("app.week") + " "
                + gameManager.getExchange().getWeek());
    }

    /**
     * Returns the advance week button.
     *
     * @return the advance button
     */
    public Button getAdvanceButton() {
        return advanceButton;
    }
}