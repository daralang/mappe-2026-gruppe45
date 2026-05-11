package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the total number of stocks listed on the exchange.
 * Updates automatically on each week advance via {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class TotalStocksCard extends WidgetCard {

    private final GameManager gameManager;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new TotalStocksCard.
     *
     * @param gameManager the game manager containing the exchange
     */
    public TotalStocksCard(GameManager gameManager) {
        super(gameManager, "exchange.overview.totalStocks");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed stock count from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(gameManager.getExchange().getStocks().size()));
    }
}
