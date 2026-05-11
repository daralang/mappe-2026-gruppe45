package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Widget card displaying the player's net worth change for the current week.
 * Shows percentage change and absolute change since last week.
 * Displays a dash if no week has been advanced yet.
 */
public class WeeklyChangeCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText changeLabel = StyledText.widgetChange();

    public WeeklyChangeCard(GameService gameService) {
        super(gameService, "dashboard.weeklyChange");
        this.gameService = gameService;
        getChildren().addAll(titleLabel, changeLabel);
        refreshDisplay();
    }

    /**
     * Updates the displayed week change based on the current and previous net worth.
     * Shows a dash if no week has been advanced yet. Reads derived values from
     * {@link GameService} via facade methods.
     */
    @Override
    protected void refreshDisplay() {
        BigDecimal change = gameService.getPlayerWeeklyNetWorthChange();
        if (change == null) {
            changeLabel.setText("–");
            changeLabel.getStyleClass().removeAll("positive", "negative");
            return;
        }

        BigDecimal percentChange = gameService.getPlayerWeeklyNetWorthChangePercent();

        String arrow = change.compareTo(BigDecimal.ZERO) >= 0 ? "↗" : "↘";
        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        String formattedPercent = String.format(Locale.of("no"), "%.1f", percentChange);

        changeLabel.setText(arrow + " " + sign + formattedPercent + "%  "
                + sign + CurrencyFormatter.format(change.abs()));
        changeLabel.getStyleClass().removeAll("positive", "negative");
        changeLabel.getStyleClass().add(
                change.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "negative"
        );
    }
}