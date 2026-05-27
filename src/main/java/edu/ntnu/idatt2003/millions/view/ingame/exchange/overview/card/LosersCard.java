package edu.ntnu.idatt2003.millions.view.ingame.exchange.overview.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.WidgetCard;

/**
 * Widget card displaying the number of stocks that fell in price this week.
 */
public class LosersCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new LosersCard.
     *
     * @param gameService the game manager containing the exchange
     */
    public LosersCard(GameService gameService) {
        super(gameService, "exchange.overview.fellThisWeek");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed count of losers from the exchange.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(String.valueOf(gameService.getExchange().countLosers()));
    }
}
