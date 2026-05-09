package edu.ntnu.idatt2003.millions.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * Utility class for formatting monetary values.
 *
 * <p>All amounts are formatted in NOK, since portfolio values and net worth
 * are converted to NOK by {@code GameManager} before being passed in.
 * Language-based display currency is tracked separately.
 */
public class CurrencyFormatter {

    private static final String DISPLAY_CURRENCY_CODE = "NOK";
    private static final NumberFormat FORMAT;

    static {
        FORMAT = NumberFormat.getNumberInstance(Locale.of("no"));
        FORMAT.setMinimumFractionDigits(2);
        FORMAT.setMaximumFractionDigits(2);
    }

    private CurrencyFormatter() {
        // Utility class - should not be instantiated
    }

    /**
     * Formats the given amount in NOK.
     *
     * @param amount the amount to format
     * @return the formatted string, e.g. "5 000,00 NOK"
     * @throws NullPointerException if amount is null
     */
    public static String format(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        return FORMAT.format(amount) + " " + DISPLAY_CURRENCY_CODE;
    }
}