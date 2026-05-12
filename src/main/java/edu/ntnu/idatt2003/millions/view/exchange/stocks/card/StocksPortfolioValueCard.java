package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the total market value of the player's portfolio in NOK.
 * Updates automatically on each game state change via
 * {@link edu.ntnu.idatt2003.millions.observer.GameObserver}.
 */
public class StocksPortfolioValueCard extends WidgetCard {

    private final GameService gameService;
    private final PortfolioService portfolioService = new PortfolioService();
    private final StyledText valueLabel = StyledText.widgetValue();

    /**
     * Constructs a new StocksPortfolioValueCard.
     *
     * @param gameService the game service containing player and exchange state
     */
    public StocksPortfolioValueCard(GameService gameService) {
        super(gameService, "exchange.stocks.portfolio");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed portfolio value by reading from {@link PortfolioService}.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(
                portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
    }
}
