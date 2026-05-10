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
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(LanguageManager.get(
                gameManager.getPlayer().getStatus(
                        gameManager.getExchange().getCurrencyConverter()).getI18nKey()));
    }
}
