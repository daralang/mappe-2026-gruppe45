package edu.ntnu.idatt2003.millions.util;

import edu.ntnu.idatt2003.millions.service.transaction.TransactionStatsService;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Utility class for formatting monetary values.
 *
 * <p>All amounts are formatted in NOK, since portfolio values and net worth
 * are converted to NOK by {@code GameService} before being passed in.
 * Language-based display currency is tracked separately.</p>
 */
public class CurrencyFormatter {

    private CurrencyFormatter() {
        // Utility class - should not be instantiated
    }

    /**
     * Formats the given amount in NOK.
     *
     * @param amount the amount to format
     * @return the formatted string, e.g. {@code "5 000,00 NOK"}
     * @throws NullPointerException if amount is null
     */
    public static String format(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        return MoneyFormatter.format(amount) + " NOK";
    }

    /**
     * Returns the display symbol for the given currency.
     *
     * <p>Supported mappings:
     * USD > $, NOK > kr, EUR > €, GBP > £, SEK > Skr, DKK → Dkr.
     * Falls back to the ISO code for unsupported currencies.</p>
     *
     * @param currency the currency to look up
     * @return the display symbol
     * @throws NullPointerException if currency is null
     */
    public static String symbol(Currency currency) {
        Objects.requireNonNull(currency, "Currency cannot be null");
        return switch (currency.getCurrencyCode()) {
            case "USD" -> "$";
            case "NOK" -> "kr";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "SEK" -> "Skr";
            case "DKK" -> "Dkr";
            default    -> currency.getCurrencyCode();
        };
    }

    /**
     * Returns the display symbol for the currency identified by the given ISO code.
     *
     * <p>Convenience overload of {@link #symbol(Currency)} for call sites that
     * already hold the ISO code as a {@link String}, such as
     * {@link TransactionStatsService.TransactionStats}.</p>
     *
     * @param isoCode the ISO 4217 currency code, e.g. {@code "USD"}
     * @return the display symbol
     * @throws NullPointerException     if isoCode is null
     * @throws IllegalArgumentException if isoCode is not a known ISO currency code
     */
    public static String symbol(String isoCode) {
        return symbol(Currency.getInstance(isoCode));
    }
}