package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.scene.control.Label;

/**
 * Card displaying the total value of the player's portfolio.
 */
public class PortfolioValueCard extends Card {

    private final GameManager gameManager;
    private final Label titleLabel;
    private final Label valueLabel;

    /**
     * Constructs a new PortfolioValueCard and initializes the display.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public PortfolioValueCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(4);

        titleLabel = new Label(LanguageManager.get("dashboard.portfolioValue"));
        titleLabel.getStyleClass().add("card-label");

        valueLabel = new Label();
        valueLabel.getStyleClass().add("card-value");

        getChildren().addAll(titleLabel, valueLabel);

        valueLabel.setText(CurrencyFormatter.format(
                gameManager.getPlayer().getPortfolio().getNetWorth()));
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
     * Refreshes the displayed portfolio value.
     */
    @Override
    public void onGameUpdated() {
        valueLabel.setText(CurrencyFormatter.format(gameManager.getPlayer().getPortfolio().getNetWorth()));
    }
}