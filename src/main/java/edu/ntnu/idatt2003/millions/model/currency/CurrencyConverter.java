package edu.ntnu.idatt2003.millions.model.currency;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Converts monetary amounts between currencies.
 *
 * <p>Implementations are expected to be stateless and thread-safe.
 * The {@link FixedRateCurrencyConverter} provides hardcoded rates;
 * other implementations may use live exchange rates in the future.
 */
public interface CurrencyConverter {

    /**
     * Converts the given amount from one currency to another.
     *
     * @param amount the amount to convert
     * @param from   the currency the amount is expressed in
     * @param to     the currency to convert the amount into
     * @return the converted amount
     * @throws NullPointerException     if any argument is null
     * @throws IllegalArgumentException if either currency is not supported
     */
    BigDecimal convert(BigDecimal amount, Currency from, Currency to);
}