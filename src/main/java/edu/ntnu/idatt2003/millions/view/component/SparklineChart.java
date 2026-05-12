package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.math.BigDecimal;
import java.util.List;

/**
 * A small Canvas-based sparkline that plots a series of prices as a trend line.
 *
 * <p>The line is drawn green when the last price is at or above the first price,
 * and red when it has fallen.
 */
public class SparklineChart extends Canvas {

    private static final double DEFAULT_WIDTH  = 80;
    private static final double DEFAULT_HEIGHT = 28;
    private static final double PADDING = 2.5;
    private static final double LINE_WIDTH = 1.5;

    private static final Color COLOUR_POSITIVE = Color.web("#27ae60");
    private static final Color COLOUR_NEGATIVE = Color.web("#e74c3c");

    /**
     * Constructs an empty sparkline with default dimensions (80 × 28 px).
     */
    public SparklineChart() {
        super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Renders the given price series onto the canvas.
     *
     * @param prices the ordered list of prices to plot, oldest first
     */
    public void update(List<BigDecimal> prices) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        if (prices == null || prices.size() < 2) {
            return;
        }

        double min = prices.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .min()
                .orElse(0);
        double max = prices.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .max()
                .orElse(1);
        double range = (max == min) ? 1 : max - min;

        double drawWidth  = getWidth()  - 2 * PADDING;
        double drawHeight = getHeight() - 2 * PADDING;

        boolean positive = prices.getLast().compareTo(prices.getFirst()) >= 0;
        gc.setStroke(positive ? COLOUR_POSITIVE : COLOUR_NEGATIVE);
        gc.setLineWidth(LINE_WIDTH);
        gc.beginPath();

        for (int i = 0; i < prices.size(); i++) {
            double x = PADDING + (i / (double) (prices.size() - 1)) * drawWidth;
            double y = PADDING + drawHeight
                    - ((prices.get(i).doubleValue() - min) / range) * drawHeight;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                gc.lineTo(x, y);
            }
        }

        gc.stroke();
    }
}
