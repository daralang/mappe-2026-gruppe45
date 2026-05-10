package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the player's currently available funds for trading.
 */
public class AvailableFundsCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    public AvailableFundsCard(GameManager gameManager) {
        super(gameManager, "dashboard.availableFunds");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(gameManager.getPlayer().getMoney()));
    }
}
