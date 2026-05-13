package edu.ntnu.idatt2003.millions.util;

import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for formatting and styling change values as JavaFX labels.
 *
 * <p>Provides factory methods for percentage and amount change labels that
 * combine formatted text with colour styling via {@link ColourChange},
 * as well as plain string formatting helpers such as {@link #formatSignedPercent}
 * and {@link #formatPlain}.
 */
public class ChangeFormatter {

    private static final DecimalFormatSymbols SYMBOLS =
            new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));

    private static final DecimalFormat PERCENT_FORMAT =
            new DecimalFormat("+#,##0.0;-#,##0.0", SYMBOLS);

    private static final DecimalFormat AMOUNT_FORMAT =
            new DecimalFormat("#,##0.00", SYMBOLS);

    private ChangeFormatter() {
        // Utility class – should not be instantiated
    }

    /**
     * Creates a label displaying the given value as a signed percentage,
     * coloured green for positive and red for negative values.
     *
     * <p>Example output: +6,6% or -4,6%
     *
     * @param value      the percentage value to format (e.g. {@code 6.6} for 6.6%)
     * @param cssClasses additional CSS classes to apply to the label
     * @return a styled label with the formatted percentage
     * @throws NullPointerException if value is null
     */
    public static Label styledPercent(BigDecimal value, String... cssClasses) {
        Label label = new Label(PERCENT_FORMAT.format(value) + "%");
        label.getStyleClass().addAll(List.of(cssClasses));
        ColourChange.applyChangeStyle(label, value);
        return label;
    }

    /**
     * Returns the given percentage value as a signed, formatted string using Norwegian locale.
     *
     * <p>Example output: {@code +6,5%} or {@code -3,2%}
     *
     * <p>Useful when the text needs to be embedded inside a larger string,
     * such as in {@link edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.WeeklyChangeCard}
     * and {@link edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card.NetWorthCard}.
     *
     * @param value the percentage value to format (e.g. {@code 6.5} for 6.5%)
     * @return a formatted signed percentage string
     * @throws NullPointerException if value is null
     */
    public static String formatSignedPercent(BigDecimal value) {
        return PERCENT_FORMAT.format(value) + "%";
    }

    /**
     * Creates a label displaying the given value as a signed amount,
     * coloured green for positive and red for negative values.
     *
     * <p>Example output: +1 234,56 or -1 234,56</p>
     *
     * @param value      the amount to format
     * @param cssClasses additional CSS classes to apply to the label
     * @return a styled label with the formatted amount
     * @throws NullPointerException if value is null
     */
    public static Label styledAmount(BigDecimal value, String... cssClasses) {
        String formatted = (value.signum() >= 0 ? "+" : "") + AMOUNT_FORMAT.format(value);
        Label label = new Label(formatted);
        label.getStyleClass().addAll(List.of(cssClasses));
        ColourChange.applyChangeStyle(label, value);
        return label;
    }

    /**
     * Returns the given value as an unsigned, formatted number string using Norwegian locale.
     *
     * <p>Example output: {@code 1 234,56}
     *
     * @param value the value to format
     * @return a plain formatted number string without sign or currency symbol
     * @throws NullPointerException if value is null
     */
    public static String formatPlain(BigDecimal value) {
        return AMOUNT_FORMAT.format(value);
    }
}
