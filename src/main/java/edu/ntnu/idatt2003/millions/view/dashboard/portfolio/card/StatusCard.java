package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoIcon;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.Tooltips;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
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
        HBox titleRow = new HBox(5, titleLabel, new InfoIcon());
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Tooltips.attach(titleRow, "tooltip.dashboard.status");
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