package edu.ntnu.idatt2003.millions.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for formatting monetary values.
 */
public class CurrencyFormatter {

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
     * Formats the given amount as a NOK string.
     *
     * @param amount the amount to format
     * @return the formatted string, e.g. "5 000,00 NOK"
     */
    public static String format(BigDecimal amount) {
        return FORMAT.format(amount) + " NOK";
    }
}