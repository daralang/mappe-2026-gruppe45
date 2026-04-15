package edu.ntnu.idatt2003.millions.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

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
     * Formats the given amount using the active currency from {@link CurrencyManager}.
     *
     * @param amount the amount to format
     * @return the formatted string, e.g. "5 000,00 USD"
     * @throws NullPointerException if amount is null
     */
    public static String format(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        return FORMAT.format(amount) + " " + CurrencyManager.get().getCurrencyCode();
    }
}