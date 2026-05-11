package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the number of stocks that rose in price this week.
 * Updates automatically on each week advance via {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class GainersCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new GainersCard.
     *
     * @param gameManager the game manager containing the exchange
     */
    public GainersCard(GameManager gameManager) {
        super(gameManager, "exchange.overview.roseThisWeek");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed count of gainers from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(gameManager.getExchange().countGainers()));
    }
}
