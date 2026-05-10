package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the total value of the player's portfolio.
 */
public class PortfolioValueCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    public PortfolioValueCard(GameManager gameManager) {
        super(gameManager, "dashboard.portfolioValue");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);

        valueLabel.setText(CurrencyFormatter.format(gameManager.getPortfolioValue()));
    }

    /**
     * Updates the title label to the current language.
     */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get("dashboard.portfolioValue"));
    }

    /**
     * Called when the game state has changed.
     * Refreshes the displayed portfolio value via the {@link GameManager} facade.
     */
    @Override
    public void onGameUpdated() {
        valueLabel.setText(CurrencyFormatter.format(gameManager.getPortfolioValue()));
    }
}
