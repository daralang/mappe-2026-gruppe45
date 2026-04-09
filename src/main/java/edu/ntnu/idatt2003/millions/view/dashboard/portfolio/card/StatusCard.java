package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.scene.control.Label;

/**
 * Card displaying the player's current status level.
 */
public class StatusCard extends Card {

    private final GameManager gameManager;
    private final Label titleLabel;
    private final Label valueLabel;

    /**
     * Constructs a new StatusCard and initializes the display.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public StatusCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(4);

        titleLabel = new Label(LanguageManager.get("dashboard.status"));
        titleLabel.getStyleClass().add("card-label");

        valueLabel = new Label();
        valueLabel.getStyleClass().add("card-value");

        getChildren().addAll(titleLabel, valueLabel);

        valueLabel.setText(LanguageManager.get(
                gameManager.getPlayer().getStatus().getI18nKey()));
    }

    /**
     * Updates all text elements to the current language.
     */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get("dashboard.status"));
        valueLabel.setText(LanguageManager.get(
                gameManager.getPlayer().getStatus().getI18nKey()));
    }

    /**
     * Called when the game state has changed.
     * Refreshes the displayed status level.
     */
    @Override
    public void onGameUpdated() {
        valueLabel.setText(LanguageManager.get(
                gameManager.getPlayer().getStatus().getI18nKey()));
    }
}