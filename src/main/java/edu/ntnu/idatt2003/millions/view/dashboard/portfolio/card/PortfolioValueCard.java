package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.InfoIcon;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.Tooltips;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

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
        HBox titleRow = new HBox(5, titleLabel, new InfoIcon());
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Tooltips.attach(titleRow, "tooltip.dashboard.portfolioValue");
        getChildren().addAll(titleRow, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
    }
}