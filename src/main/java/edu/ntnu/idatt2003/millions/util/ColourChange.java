package edu.ntnu.idatt2003.millions.util;

import javafx.scene.control.Label;
import java.math.BigDecimal;

/**
 * Utility class for common JavaFX view operations.
 */
public class ColourChange {

    private ColourChange() {}

    /**
     * Applies a positive or negative CSS style class to a label
     * based on the sign of the given value.
     *
     * @param label the label to style
     * @param value the value determining the style
     */
    public static void applyChangeStyle(Label label, BigDecimal value) {
        label.getStyleClass().removeAll("card-value-positive", "card-value-negative");
        label.getStyleClass().add(
                value.compareTo(BigDecimal.ZERO) >= 0
                        ? "card-value-positive"
                        : "card-value-negative"
        );
    }
}