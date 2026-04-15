package edu.ntnu.idatt2003.millions.util;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Manages the active display currency for the application.
 *
 * <p>Supported currencies are defined in {@link SupportedCurrency}.
 * The active currency is set once at game start and used by
 * {@link CurrencyFormatter} to format all monetary values consistently.
 *
 * <p>Follows the same utility pattern as {@link LanguageManager}.
 */
public class CurrencyManager {

    private static Currency current = Currency.getInstance("USD");

    private CurrencyManager() {}

    /**
     * Returns the list of currencies available for selection,
     * derived from {@link SupportedCurrency}.
     *
     * @return an unmodifiable list of supported currencies
     */
    public static List<Currency> getSupportedCurrencies() {
        return Arrays.stream(SupportedCurrency.values())
                .map(SupportedCurrency::getCurrency)
                .toList();
    }

    /**
     * Sets the active currency for the session.
     *
     * @param currency the currency to use
     * @throws NullPointerException if currency is null
     */
    public static void setCurrency(Currency currency) {
        Objects.requireNonNull(currency, "Currency cannot be null");
        current = currency;
    }

    /**
     * Returns the currently active currency.
     *
     * @return the active currency
     */
    public static Currency get() {
        return current;
    }
}