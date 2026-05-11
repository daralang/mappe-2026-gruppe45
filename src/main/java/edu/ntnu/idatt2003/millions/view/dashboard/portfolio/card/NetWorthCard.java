package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PlayerStatsService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WidgetCard;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Widget card displaying the player's net worth over time as an area chart.
 * Also shows total change in value and percentage since the start of the game.
 */
public class NetWorthCard extends WidgetCard {

    private final GameService gameService;
    private final PlayerStatsService statsService = new PlayerStatsService();
    private final StyledText netWorthLabel = StyledText.widgetValue();
    private final StyledText changeLabel = StyledText.widgetChange();
    private final NumberAxis xAxis;
    private final XYChart.Series<Number, Number> series;

    public NetWorthCard(GameService gameService) {
        super(gameService, "dashboard.netWorth");
        this.gameService = gameService;

        List<BigDecimal> history = gameService.getPlayer().getNetWorthHistory();
        int historySize = history.size();

        xAxis = new NumberAxis(1, Math.max(historySize, 1), Math.max(1, historySize / 8));
        xAxis.setAutoRanging(false);
        xAxis.setLabel(LanguageManager.get("app.week"));

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        yAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return String.format(Locale.of("no"), "%,.0f", n.doubleValue());
            }
            @Override
            public Number fromString(String s) { return null; }
        });

        series = new XYChart.Series<>();
        series.getData().addListener((ListChangeListener<XYChart.Data<Number, Number>>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(d -> {
                    if (d.getNode() != null) d.getNode().setVisible(false);
                });
            }
        });

        AreaChart<Number, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("area-chart");

        getChildren().addAll(titleLabel, netWorthLabel, changeLabel, chart);

        loadHistory();
        refreshDisplay();
    }

    private void loadHistory() {
        List<BigDecimal> history = gameService.getPlayer().getNetWorthHistory();
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, history.get(i).doubleValue()));
        }
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
        String formattedPercent = String.format(Locale.of("no"), "%.1f", percentChange);

        netWorthLabel.setText(CurrencyFormatter.format(netWorth));
        changeLabel.setText(sign + CurrencyFormatter.format(change.abs())
                + "  " + sign + formattedPercent + "% "
                + LanguageManager.get("dashboard.sinceStart"));

        changeLabel.getStyleClass().removeAll("positive", "negative");
        changeLabel.getStyleClass().add(
                change.compareTo(BigDecimal.ZERO) >= 0 ? "positive" : "negative"
        );
    }

    /**
     * Called when the game state has changed.
     * Adds a new data point to the chart, extends the x-axis and refreshes the display.
     * Overrides {@link WidgetCard#onGameUpdated()} because this card has additional
     * update logic (chart point) beyond just refreshing text.
     */
    @Override
    public void onGameUpdated() {
        int nextPoint = series.getData().size() + 1;
        double netWorth = statsService.getNetWorth(gameService.getPlayer(), gameService.getCurrencyConverter()).doubleValue();
        series.getData().add(new XYChart.Data<>(nextPoint, netWorth));
        xAxis.setUpperBound(nextPoint);
        xAxis.setTickUnit(Math.max(1, nextPoint / 8));
        refreshDisplay();
    }
}