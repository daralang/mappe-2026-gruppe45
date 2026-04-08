package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Card displaying the player's net worth change for the current week.
 * Shows percentage change and absolute change in NOK since last week.
 * Displays a dash if no week has been advanced yet.
 */
public class WeeklyChangeCard extends Card {

    private final GameManager gameManager;
    private final Label changeLabel;
    private final Label titleLabel;

    /**
     * Constructs a new WeeklyChangeCard.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public WeeklyChangeCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(4);

        titleLabel = new Label(LanguageManager.get("dashboard.weeklyChange"));
        titleLabel.getStyleClass().add("card-label");

        changeLabel = new Label();
        changeLabel.getStyleClass().add("card-value");

        getChildren().addAll(titleLabel, changeLabel);

        updateDisplay();
    }

    /**
     * Updates the displayed week change based on the current and previous net worth.
     * Shows a dash if no week has been advanced yet.
     */
    private void updateDisplay() {
        BigDecimal previousNetWorth = gameManager.getPreviousNetWorth();
        if (previousNetWorth == null) {
            changeLabel.setText("–");
            return;
        }

        BigDecimal currentNetWorth = gameManager.getPlayer().getNetWorth();
        BigDecimal change = currentNetWorth.subtract(previousNetWorth);
        BigDecimal percentChange = change
                .divide(previousNetWorth, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        String arrow = change.compareTo(BigDecimal.ZERO) >= 0 ? "↗" : "↘";
        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

        String formattedPercent = String.format(Locale.of("no"), "%.1f", percentChange);
        changeLabel.setText(arrow + " " + sign + formattedPercent + "%  "
                + sign + CurrencyFormatter.format(change.abs()));
        changeLabel.getStyleClass().removeAll("card-value-positive", "card-value-negative");
        changeLabel.getStyleClass().add(
                change.compareTo(BigDecimal.ZERO) >= 0 ? "card-value-positive" : "card-value-negative"
        );
    }

    /**
     * Updates the title label to the current language.
     * Also refreshes the display in case number formatting changes.
     */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get("dashboard.weeklyChange"));
        updateDisplay();
    }

    /**
     * Called when the game state has changed.
     * Refreshes the displayed week change.
     */
    @Override
    public void onGameUpdated() {
        updateDisplay();
    }
}