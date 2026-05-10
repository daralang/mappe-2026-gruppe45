package edu.ntnu.idatt2003.millions.model.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Map;
import java.util.Objects;


/**
 * A {@link CurrencyConverter} backed by a fixed table of exchange rates.
 *
 * <p> All rates are defined relative to NOK and used as the pivot for
 * conversion between any two supported currencies. Conversion follows
 * the formula: {@code (amount * fromRateToNok) / toRateToNok}.</p>
 *
 * <p> Supported currencies: NOK, USD, EUR, GBP. Conversion of an amount
 * to and from the same currency returns the original amount unchanged.</p>
 *
 * <p> Last updated currency: 8. May 2026</p>
 */
public class FixedRateCurrencyConverter implements CurrencyConverter {

    private static final Map<String, BigDecimal> RATES_TO_NOK = Map.of(
            "NOK", BigDecimal.ONE,
            "USD", new BigDecimal("9.21"),
            "EUR", new BigDecimal("10.84"),
            "GBP", new BigDecimal("12.54"),
            "SEK", new BigDecimal("1.00"),
            "DKK", new BigDecimal("1.45")
    );

    @Override
    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(from, "From currency cannot be null");
        Objects.requireNonNull(to, "To currency cannot be null");

        if (from.equals(to)) return amount;

        BigDecimal amountInNok = amount.multiply(rateFor(from));
        return amountInNok.divide(rateFor(to), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal rateFor(Currency currency) {
        BigDecimal rate = RATES_TO_NOK.get(currency.getCurrencyCode());
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + currency.getCurrencyCode());
        }
        return rate;
    }
}
