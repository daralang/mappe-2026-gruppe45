package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
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

    private final GameManager gameManager;
    private final StyledText changeLabel = StyledText.widgetChange();

    public WeeklyChangeCard(GameManager gameManager) {
        super(gameManager, "dashboard.weeklyChange");
        this.gameManager = gameManager;
        getChildren().addAll(titleLabel, changeLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        var converter = gameManager.getExchange().getCurrencyConverter();
        var player = gameManager.getPlayer();

        BigDecimal change = player.getWeeklyNetWorthChange(converter);
        if (change == null) {
            changeLabel.setText("–");
            changeLabel.getStyleClass().removeAll("positive", "negative");
            return;
        }

        BigDecimal percentChange = player.getWeeklyNetWorthChangePercent(converter);
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
