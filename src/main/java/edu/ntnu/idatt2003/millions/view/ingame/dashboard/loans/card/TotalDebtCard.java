package edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.currency.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.Card;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.WidgetCard;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

/**
 * Large card on the Loans tab showing total outstanding debt and its
 * week-by-week history as a bar chart.
 *
 * <p>Extends {@link Card} directly (not {@link WidgetCard})
 * because it owns a chart and lifecycle logic beyond a single number tile,
 * mirroring how {@code NetWorthCard} is structured on the Portfolio tab.</p>
 *
 * <p>The change line uses INVERTED colour semantics: debt increasing is bad
 * (red), debt decreasing is good (green). This is the opposite of NetWorthCard.</p>
 */
public class TotalDebtCard extends Card {

    private final GameService gameService;

    private final StyledText titleLabel  = StyledText.widgetLabel();
    private final StyledText valueLabel  = StyledText.widgetValue();
    private final StyledText changeLabel = StyledText.widgetChange();

    private final XYChart.Series<String, Number> series = new XYChart.Series<>();

    public TotalDebtCard(GameService gameService) {
        super(gameService);
        this.gameService = gameService;

        setSpacing(4);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel(LanguageManager.get("app.week").toUpperCase());

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(true);
        yAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number n) {
                return String.format(Locale.of("no"), "%,.0f", n.doubleValue());
            }
            @Override
            public Number fromString(String s) { return null; }
        });

        // Install hover tooltips on each bar as it is added to the series.
        series.getData().addListener((ListChangeListener<XYChart.Data<String, Number>>) change -> {
            while (change.next()) {
                change.getAddedSubList().forEach(d -> {
                    if (d.getNode() != null) installBarTooltip(d);
                    // Node may not be ready yet; listen for it on the property.
                    else d.nodeProperty().addListener((obs, old, node) -> {
                        if (node != null) installBarTooltip(d);
                    });
                });
            }
        });

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("debt-bar-chart");

        InfoTooltip infoTooltip = new InfoTooltip("tooltip.loans.totalDebt");
        HBox titleRow = new HBox(5, titleLabel, infoTooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        infoTooltip.attachToParent(titleRow);

        getChildren().addAll(titleRow, valueLabel, changeLabel, chart);

        loadHistory();
        refreshDisplay();
    }

    private void loadHistory() {
        List<BigDecimal> history = gameService.getPlayer().getTotalDebtHistory();
        for (int i = 0; i < history.size(); i++) {
            series.getData().add(new XYChart.Data<>(String.valueOf(i + 1), history.get(i).doubleValue()));
        }
    }

    private void installBarTooltip(XYChart.Data<String, Number> d) {
        String weekLabel = d.getXValue();
        String amount = CurrencyFormatter.format(
                new BigDecimal(d.getYValue().toString()).setScale(2, java.math.RoundingMode.HALF_UP));
        String text = MessageFormat.format(
                LanguageManager.get("loans.debtChart.barTooltip"), weekLabel, amount);
        Tooltip tooltip = new Tooltip(text);
        Tooltip.install(d.getNode(), tooltip);
    }

    private void refreshDisplay() {
        titleLabel.setText(LanguageManager.get("dashboard.loans.totalDebt"));

        BigDecimal debt = gameService.getPlayer().getTotalDebt();
        valueLabel.setText(CurrencyFormatter.format(debt));

        List<BigDecimal> history = gameService.getPlayer().getTotalDebtHistory();
        if (history.size() < 2) {
            changeLabel.setText("–");
            changeLabel.getStyleClass().removeAll("positive", "negative");
        } else {
            BigDecimal prev = history.get(history.size() - 2);
            BigDecimal curr = history.getLast();
            BigDecimal delta = curr.subtract(prev);

            String arrow = delta.compareTo(BigDecimal.ZERO) >= 0 ? "↗" : "↘";
            String sign  = delta.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";

            BigDecimal pct = prev.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : delta.multiply(BigDecimal.valueOf(100))
                            .divide(prev, 1, java.math.RoundingMode.HALF_UP);

            changeLabel.setText(arrow + " " + ChangeFormatter.formatSignedPercent(pct)
                    + "  " + sign + CurrencyFormatter.format(delta.abs()));

            // Debt semantics are inverted: an increase is bad (red), a decrease is good (green).
            // ColourChange.applyChangeStyle would colour debt-increase green — the opposite of what
            // we want — so we apply the modifier classes manually here.
            changeLabel.getStyleClass().removeAll("positive", "negative");
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                changeLabel.getStyleClass().add("negative"); // debt went up → red
            } else if (delta.compareTo(BigDecimal.ZERO) < 0) {
                changeLabel.getStyleClass().add("positive"); // debt went down → green
            }
        }

    }

    @Override
    public void onGameUpdated() {
        List<BigDecimal> history = gameService.getPlayer().getTotalDebtHistory();
        if (!history.isEmpty()) {
            int week = history.size();
            double debtValue = history.getLast().doubleValue();
            series.getData().add(new XYChart.Data<>(String.valueOf(week), debtValue));
        }
        refreshDisplay();
    }

    @Override
    protected void onLanguageChanged() {
        refreshDisplay();
    }
}
