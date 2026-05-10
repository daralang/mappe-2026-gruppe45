package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
import javafx.scene.control.Label;

/**
 * Widget card displaying the total value of the player's portfolio.
 */
public class PortfolioValueCard extends WidgetCard {

    private final GameManager gameManager;
    private final Label valueLabel = new Label();

    public PortfolioValueCard(GameManager gameManager) {
        super(gameManager, "dashboard.portfolioValue");
        this.gameManager = gameManager;
        valueLabel.getStyleClass().add("widget-value");
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(
                gameManager.getPlayer().getPortfolio().getNetWorth(
                        gameManager.getExchange().getCurrencyConverter())));
    }
}
