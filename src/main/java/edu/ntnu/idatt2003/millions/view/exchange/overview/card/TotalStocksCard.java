package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;

/**
 * Widget card displaying the total number of stocks listed on the exchange.
 * Updates automatically on each week advance via {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class TotalStocksCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new TotalStocksCard.
     *
     * @param gameService the game manager containing the exchange
     */
    public TotalStocksCard(GameService gameService) {
        super(gameService, "exchange.overview.totalStocks");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed stock count from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(gameService.getExchange().getStocks().size()));
    }
}
