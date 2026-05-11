package edu.ntnu.idatt2003.millions.util;

import javafx.scene.control.Label;
import java.math.BigDecimal;

/**
 * Utility class for colour change texts/labels based on values.
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
        label.getStyleClass().removeAll("positive", "negative");
        label.getStyleClass().add(
                value.compareTo(BigDecimal.ZERO) >= 0
                        ? "positive"
                        : "negative"
        );
    }
}