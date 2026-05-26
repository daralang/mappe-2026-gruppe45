package edu.ntnu.idatt2003.millions.view.ingame.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.player.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.format.ColourChange;
import edu.ntnu.idatt2003.millions.util.currency.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.ingame.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

/**
 * Widget card displaying the player's net worth change for the current week.
 * Shows percentage change and absolute change since last week.
 * Displays a dash if no week has been advanced yet.
 */
public class WeeklyChangeCard extends WidgetCard {

    private final GameService gameService;
    private final PlayerStatsService statsService = new PlayerStatsService();
    private final StyledText changeLabel = StyledText.widgetChange();

    public WeeklyChangeCard(GameService gameService) {
        super(gameService, "dashboard.weeklyChange");
        this.gameService = gameService;
        InfoTooltip infoTooltip = new InfoTooltip("tooltip.dashboard.weeklyChange");
        HBox titleRow = new HBox(5, titleLabel, infoTooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        infoTooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, changeLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed week change based on the current and previous net worth.
     * Shows a dash if no week has been advanced yet. Reads derived values from
     * {@link GameService} via facade methods.
     */
    @Override
    protected void refreshDisplay() {
        BigDecimal change = statsService.getWeeklyNetWorthChange(gameService.getPlayer(), gameService.getCurrencyConverter());
        if (change == null) {
            changeLabel.setText("–");
            changeLabel.getStyleClass().removeAll("positive", "negative");
            return;
        }

        BigDecimal percentChange = statsService.getWeeklyNetWorthChangePercent(gameService.getPlayer(), gameService.getCurrencyConverter());

        String arrow = change.compareTo(BigDecimal.ZERO) >= 0 ? "↗" : "↘";
        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

        changeLabel.setText(arrow + " " + ChangeFormatter.formatSignedPercent(percentChange)
                + "  " + sign + CurrencyFormatter.format(change.abs()));
        ColourChange.applyChangeStyle(changeLabel, change);
    }
}
