package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

/**
 * Widget card displaying the player's current status level.
 */
public class StatusCard extends WidgetCard {

    private final GameService gameService;
    private final PlayerStatsService statsService = new PlayerStatsService();
    private final StyledText valueLabel = StyledText.widgetValue();

    public StatusCard(GameService gameService) {
        super(gameService, "dashboard.status");
        this.gameService = gameService;
        InfoTooltip infoTooltip = new InfoTooltip("tooltip.dashboard.status");
        HBox titleRow = new HBox(5, titleLabel, infoTooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        infoTooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        String key = switch (statsService.getStatus(gameService.getPlayer(), gameService.getCurrencyConverter())) {
            case NOVICE -> "status.novice";
            case INVESTOR -> "status.investor";
            case SPECULATOR -> "status.speculator";
        };
        valueLabel.setText(LanguageManager.get(key));
    }
}