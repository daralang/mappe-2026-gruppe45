package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the player's current status level.
 */
public class StatusCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    public StatusCard(GameManager gameManager) {
        super(gameManager, "dashboard.status");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);

        valueLabel.setText(LanguageManager.get(gameManager.getPlayerStatus().getI18nKey()));
    }

    /**
     * Updates all text elements to the current language.
     * Reads the status from {@link GameManager} via the facade.
     */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get("dashboard.status"));
        valueLabel.setText(LanguageManager.get(gameManager.getPlayerStatus().getI18nKey()));
    }

    /**
     * Called when the game state has changed.
     * Refreshes the displayed status level via the {@link GameManager} facade.
     */
    @Override
    public void onGameUpdated() {
        valueLabel.setText(LanguageManager.get(gameManager.getPlayerStatus().getI18nKey()));
    }
}
