package edu.ntnu.idatt2003.millions.view.component.chart;

import javafx.collections.ListChangeListener;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Reusable area chart component for plotting a series of {@link BigDecimal} values over time.
 *
 * <p>Extends {@link AreaChart} with the conventions used across the game's charts:
 * manual x-axis bounds (lower = 1, upper = data size + 0.5), Norwegian-locale y-axis
 * formatting, hidden data-point nodes, no animation, and the shared {@code area-chart}
 * CSS class.
 */
public class TimeSeriesChart extends AreaChart<Number, Number> {

  private final NumberAxis xAxis;
  private final XYChart.Series<Number, Number> series;

  /**
   * Constructs a {@code TimeSeriesChart} pre-loaded with the given values.
   *
   * <p>The chart hides individual data-point nodes so only the filled area and
   * line are visible.</p>
   *
   * @param values     the initial data points to plot, oldest first; must not be {@code null}
   * @param xAxisLabel the label shown on the x-axis (e.g. «UKE»); must not be {@code null}
   * @throws NullPointerException if {@code values} or {@code xAxisLabel} is {@code null}
   */
  public TimeSeriesChart(List<BigDecimal> values, String xAxisLabel) {
    super(new NumberAxis(), new NumberAxis());
    Objects.requireNonNull(values, "values must not be null");
    Objects.requireNonNull(xAxisLabel, "xAxisLabel must not be null");

    xAxis = (NumberAxis) getXAxis();
    NumberAxis yAxis = (NumberAxis) getYAxis();

    xAxis.setLabel(xAxisLabel);
    xAxis.setAutoRanging(false);
    xAxis.setForceZeroInRange(false);
    xAxis.setLowerBound(1);
    xAxis.setTickUnit(1);
    xAxis.setTickLabelFormatter(new StringConverter<>() {
      @Override
      public String toString(Number n) {
        double v = n.doubleValue();
        return v == Math.floor(v) ? String.valueOf((int) v) : "";
      }

      @Override
      public Number fromString(String s) {
        return null;
      }
    });

    yAxis.setAutoRanging(true);
    yAxis.setForceZeroInRange(false);
    yAxis.setTickLabelFormatter(new StringConverter<>() {
      @Override
      public String toString(Number n) {
        return String.format(Locale.of("no"), "%,.0f", n.doubleValue());
      }

      @Override
      public Number fromString(String s) {
        return null;
      }
    });

    series = new XYChart.Series<>();
    series.getData().addListener((ListChangeListener<XYChart.Data<Number, Number>>) change -> {
      while (change.next()) {
        change.getAddedSubList().forEach(d -> {
          if (d.getNode() != null) {
            d.getNode().setVisible(false);
          }
        });
      }
    });

    getData().add(series);
    setLegendVisible(false);
    setAnimated(false);
    getStyleClass().add("area-chart");

    for (int i = 0; i < values.size(); i++) {
      series.getData().add(new XYChart.Data<>(i + 1, values.get(i).doubleValue()));
    }
    updateXAxis();
  }

  /**
   * Appends a new data point at the next x position and extends the x-axis accordingly.
   *
   * @param value the value to append; must not be {@code null}
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public void addPoint(BigDecimal value) {
    Objects.requireNonNull(value, "value must not be null");
    int next = series.getData().size() + 1;
    series.getData().add(new XYChart.Data<>(next, value.doubleValue()));
    updateXAxis();
  }

  /**
   * Replaces all data points with the given values, rebuilding the series from scratch.
   * x-positions are assigned positionally (1, 2, 3, …) to match constructor semantics.
   *
   * @param values the new data points, oldest first; must not be {@code null}
   * @throws NullPointerException if {@code values} is {@code null}
   */
  public void setPoints(List<BigDecimal> values) {
    Objects.requireNonNull(values, "values must not be null");
    series.getData().clear();
    for (int i = 0; i < values.size(); i++) {
      series.getData().add(new XYChart.Data<>(i + 1, values.get(i).doubleValue()));
    }
    updateXAxis();
  }

  private void updateXAxis() {
    int count = series.getData().size();
    xAxis.setUpperBound(Math.max(2, count) + 0.5);
    xAxis.requestAxisLayout();
  }
}
