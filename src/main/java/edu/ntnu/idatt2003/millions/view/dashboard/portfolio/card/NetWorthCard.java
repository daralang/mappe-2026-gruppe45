package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.collections.ListChangeListener;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/**
 * Card displaying the player's net worth over time as an area chart.
 * Also shows total change in value and percentage since the start of the game.
 */
public class NetWorthCard extends Card {

    private final GameManager gameManager;
    private final Label titleLabel;
    private final Label netWorthLabel;
    private final Label changeLabel;
    private final NumberAxis xAxis;
    private final XYChart.Series<Number, Number> series;

    /**
     * Constructs a new NetWorthCard and initializes the display.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public NetWorthCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(4);

        titleLabel = new Label(LanguageManager.get("dashboard.netWorth"));
        titleLabel.getStyleClass().add("card-label");

        netWorthLabel = new Label();
        netWorthLabel.getStyleClass().add("card-value");

        changeLabel = new Label();

        List<BigDecimal> history = gameManager.getPlayer().getNetWorthHistory();
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
        updateDisplay();
    }

    /**
     * Loads existing net worth history into the chart.
     * Called once at construction to populate the chart with historical data.
     */
    private void loadHistory() {
        List<BigDecimal> history = gameManager.getPlayer().getNetWorthHistory();
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new XYChart.Data<>(i + 1, history.get(i).doubleValue()));
        }
    }

    /**
     * Updates the net worth label and change label with current values.
     */
    private void updateDisplay() {
        BigDecimal netWorth = gameManager.getNetWorthNok();
        BigDecimal startingMoney = gameManager.getPlayer().getStartingMoney();
        BigDecimal change = netWorth.subtract(startingMoney);
        BigDecimal percentChange = change
                .divide(startingMoney, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);

        String sign = change.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        String formattedPercent = String.format(Locale.of("no"), "%.1f", percentChange);

        netWorthLabel.setText(CurrencyFormatter.format(netWorth));
        changeLabel.setText(sign + CurrencyFormatter.format(change.abs())
                + "  " + sign + formattedPercent + "% "
                + LanguageManager.get("dashboard.sinceStart"));

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
        titleLabel.setText(LanguageManager.get("dashboard.netWorth"));
        updateDisplay();
    }

    /**
     * Called when the game state has changed.
     * Adds a new data point to the chart, extends the x-axis and refreshes the display.
     */
    @Override
    public void onGameUpdated() {
        int nextPoint = series.getData().size() + 1;
        double netWorth = gameManager.getNetWorthNok().doubleValue(); // her var feilen
        series.getData().add(new XYChart.Data<>(nextPoint, netWorth));
        xAxis.setUpperBound(nextPoint);
        xAxis.setTickUnit(Math.max(1, nextPoint / 8));
        updateDisplay();
    }
}