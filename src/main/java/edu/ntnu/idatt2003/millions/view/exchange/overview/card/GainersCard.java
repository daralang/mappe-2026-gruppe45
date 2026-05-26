package edu.ntnu.idatt2003.millions.view.exchange.overview.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;

/**
 * Widget card displaying the number of stocks that rose in price this week.
 * Updates automatically on each week advance via {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class GainersCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new GainersCard.
     *
     * @param gameService the game manager containing the exchange
     */
    public GainersCard(GameService gameService) {
        super(gameService, "exchange.overview.roseThisWeek");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed count of gainers from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(gameService.getExchange().countGainers()));
    }
}
