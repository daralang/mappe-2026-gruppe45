package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
import javafx.scene.control.Label;

/**
 * Widget card displaying the player's current status level.
 */
public class StatusCard extends WidgetCard {

    private final GameManager gameManager;
    private final Label valueLabel = new Label();

    public StatusCard(GameManager gameManager) {
        super(gameManager, "dashboard.status");
        this.gameManager = gameManager;
        valueLabel.getStyleClass().add("widget-value");
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
