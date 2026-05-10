package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
import javafx.scene.control.Label;

/**
 * Widget card displaying the player's currently available funds for trading.
 */
public class AvailableFundsCard extends WidgetCard {

    private final GameManager gameManager;
    private final Label valueLabel = new Label();

    public AvailableFundsCard(GameManager gameManager) {
        super(gameManager, "dashboard.availableFunds");
        this.gameManager = gameManager;
        valueLabel.getStyleClass().add("widget-value");
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(gameManager.getPlayer().getMoney()));
    }
}
