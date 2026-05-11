package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the number of stocks that fell in price this week.
 * Updates automatically on each week advance via {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class LosersCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new LosersCard.
     *
     * @param gameManager the game manager containing the exchange
     */
    public LosersCard(GameManager gameManager) {
        super(gameManager, "exchange.overview.fellThisWeek");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed count of losers from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(
                gameManager.getExchange().getLosers(Integer.MAX_VALUE).size()));
    }
}
