package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.ColourChange;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.InfoIcon;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.Tooltips;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
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
        HBox titleRow = new HBox(5, titleLabel, new InfoIcon());
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Tooltips.attach(titleRow, "tooltip.dashboard.weeklyChange");
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
