package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.ColourChange;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.chart.TimeSeriesChart;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

/**
 * Widget card displaying the player's net worth over time as an area chart.
 * Also shows total change in value and percentage since the start of the game.
 *
 * <p>Graph rendering is delegated to {@link StockPriceChart}, which owns all
 * chart configuration and data management.
 */
public class NetWorthCard extends WidgetCard {

    private final GameService gameService;
    private final PlayerStatsService statsService = new PlayerStatsService();
    private final StyledText netWorthLabel = StyledText.widgetValue();
    private final StyledText changeLabel = StyledText.widgetChange();
    private final TimeSeriesChart chart;

    /**
     * Constructs a {@code NetWorthCard} pre-loaded with the player's existing net-worth history.
     *
     * @param gameService the game service used to read player state and currency conversion;
     *                    must not be {@code null}
     */
    public NetWorthCard(GameService gameService) {
        super(gameService, "dashboard.netWorth");
        this.gameService = gameService;

        chart = new TimeSeriesChart(
                gameService.getPlayer().getNetWorthHistory(),
                LanguageManager.get("app.week").toUpperCase());

        InfoTooltip infoTooltip = new InfoTooltip("tooltip.dashboard.netWorth");
        HBox titleRow = new HBox(5, titleLabel, infoTooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        infoTooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, netWorthLabel, changeLabel, chart);

        refreshDisplay();
    }

    /**
     * Updates the net worth label and change label with current values.
     * Reads derived values from {@link GameService} via facade methods.
     */
    @Override
    protected void refreshDisplay() {
        BigDecimal netWorth = statsService.getNetWorth(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal change = statsService.getNetWorthChangeSinceStart(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal percentChange = statsService.getNetWorthChangePercentSinceStart(gameService.getPlayer(), gameService.getCurrencyConverter());

        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

        netWorthLabel.setText(CurrencyFormatter.format(netWorth));
        changeLabel.setText(sign + CurrencyFormatter.format(change.abs())
                + "  " + ChangeFormatter.formatSignedPercent(percentChange)
                + " " + LanguageManager.get("dashboard.sinceStart"));

        ColourChange.applyChangeStyle(changeLabel, change);
    }

    /**
     * Called when the game state has changed.
     * Appends a new data point to the chart via {@link StockPriceChart#addPoint(BigDecimal)}
     * and refreshes the displayed labels.
     * Overrides {@link WidgetCard#onGameUpdated()} because this card has additional
     * update logic (chart point) beyond just refreshing text.
     */
    @Override
    public void onGameUpdated() {
        BigDecimal netWorth = statsService.getNetWorth(gameService.getPlayer(), gameService.getCurrencyConverter());
        chart.addPoint(netWorth);
        refreshDisplay();
    }
}
