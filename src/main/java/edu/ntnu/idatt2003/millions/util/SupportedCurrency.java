package edu.ntnu.idatt2003.millions.util;

import java.util.Currency;

/**
 * Represents the currencies supported by the application.
 *
 * <p>Each constant wraps a {@link Currency} instance identified
 * by its ISO 4217 currency code.
 */
public enum SupportedCurrency {

    USD("USD"),
    EUR("EUR"),
    NOK("NOK"),
    GBP("GBP"),
    SEK("SEK"),
    DKK("DKK");

    private final Currency currency;

    /**
     * Constructs a supported currency from the given ISO 4217 code.
     *
     * @param code the ISO 4217 currency code
     */
    SupportedCurrency(String code) {
        this.currency = Currency.getInstance(code);
    }

    /**
     * Returns the {@link Currency} instance for this supported currency.
     *
     * @return the currency
     */
    public Currency getCurrency() {
        return currency;
    }
}