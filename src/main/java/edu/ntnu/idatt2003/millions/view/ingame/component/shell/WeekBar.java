package edu.ntnu.idatt2003.millions.view.ingame.component.shell;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * A reusable component displaying the current week and an advance week button.
 * Implements {@link GameObserver} to automatically update the week number
 * when the game state changes.
 */
public class WeekBar extends HBox implements GameObserver {

    private final GameService gameService;
    private final StyledText weekLabel;
    private final Button advanceButton;

    /**
     * Constructs a new WeekBar and registers itself as a game observer.
     *
     * @param gameService    the game manager to observe
     * @param onAdvanceWeek  callback invoked when the user clicks "Advance week"
     */
    public WeekBar(GameService gameService, Runnable onAdvanceWeek) {
        this.gameService = gameService;
        gameService.addObserver(this);

        setSpacing(24);
        setAlignment(Pos.CENTER_RIGHT);

        weekLabel = StyledText.weekLabel(LanguageManager.get("app.week").toUpperCase() + " "
                + gameService.getExchange().getWeek());

        advanceButton = new Button(LanguageManager.get("app.advanceWeek"));
        advanceButton.getStyleClass().add("advance-button");
        advanceButton.setOnAction(e -> onAdvanceWeek.run());

        getChildren().addAll(weekLabel, advanceButton);

        LanguageManager.addObserver(this::onLanguageChanged);
    }

    /**
     * Updates text elements to the current language.
     * Called automatically when the language changes.
     */
    private void onLanguageChanged() {
        advanceButton.setText(LanguageManager.get("app.advanceWeek"));
        weekLabel.setText(LanguageManager.get("app.week").toUpperCase() + " "
                + gameService.getExchange().getWeek());
    }

    /**
     * Updates the week label and disables the advance button when the game is over.
     */
    @Override
    public void onGameUpdated() {
        weekLabel.setText(LanguageManager.get("app.week").toUpperCase() + " "
                + gameService.getExchange().getWeek());
        advanceButton.setDisable(gameService.isGameOver());
    }
}