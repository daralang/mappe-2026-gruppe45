package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the total value of the player's portfolio.
 */
public class PortfolioValueCard extends WidgetCard {

    private final GameService gameService;
    private final PortfolioService portfolioService = new PortfolioService();
    private final StyledText valueLabel = StyledText.widgetValue();

    public PortfolioValueCard(GameService gameService) {
        super(gameService, "dashboard.portfolioValue");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
    }
}