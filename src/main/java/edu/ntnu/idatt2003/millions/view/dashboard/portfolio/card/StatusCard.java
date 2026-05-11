package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

/**
 * Widget card displaying the player's current status level.
 */
public class StatusCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    public StatusCard(GameService gameService) {
        super(gameService, "dashboard.status");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        String key = switch (gameService.getPlayerStatus()) {
            case NOVICE -> "status.novice";
            case INVESTOR -> "status.investor";
            case SPECULATOR -> "status.speculator";
        };
        valueLabel.setText(LanguageManager.get(key));
    }
}