package edu.ntnu.idatt2003.millions.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formats monetary amounts as plain number strings using the convention of
 * the currently active UI language (grouping and decimal separators follow the
 * locale; always two decimals; no currency suffix).
 *
 * <p>Single source of truth for amount formatting. Callers append any currency
 * code or symbol themselves.</p>
 *
 * <p>The active locale is read from {@link LanguageManager} on each call, so a
 * language switch is reflected the next time a value is formatted (after the
 * observer-driven UI refresh).</p>
 *
 * <p>One {@link DecimalFormat} is cached per locale. {@code DecimalFormat} is not
 * thread-safe; consistent with existing usage — all formatting runs on the
 * JavaFX Application Thread.</p>
 */
public final class MoneyFormatter {

    private static final Map<Locale, DecimalFormat> CACHE = new ConcurrentHashMap<>();

    private MoneyFormatter() {
        // Utility class — no instances
    }

    /**
     * Formats the amount using the active UI language convention.
     *
     * <p>Examples: {@code 5000.5} → {@code "5 000,50"} (Norwegian) or
     * {@code "5,000.50"} (English). No currency suffix is added.</p>
     *
     * @param amount the amount to format, must not be null
     * @return the formatted number string
     */
    public static String format(BigDecimal amount) {
        Locale locale = LanguageManager.getCurrentLanguage().locale;
        return CACHE.computeIfAbsent(locale, MoneyFormatter::buildFormat).format(amount);
    }

    private static DecimalFormat buildFormat(Locale locale) {
        return new DecimalFormat("#,##0.00", new DecimalFormatSymbols(locale));
    }
}
