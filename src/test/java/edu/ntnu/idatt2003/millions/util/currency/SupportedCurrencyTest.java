package edu.ntnu.idatt2003.millions.util.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link SupportedCurrency} enum.
 * <p>
 * Verifies that each constant resolves to the correct {@link Currency}
 * instance and that the enum exposes the expected set of currencies.
 * </p>
 */
class SupportedCurrencyTest {

    @Nested
    @DisplayName("getCurrency()")
    class GetCurrency {

        @Test
        @DisplayName("Should return correct Currency instance for USD")
        void returnsUsd() {
            assertEquals(Currency.getInstance("USD"),
                    SupportedCurrency.USD.getCurrency());
        }

        @Test
        @DisplayName("Should return correct Currency instance for NOK")
        void returnsNok() {
            assertEquals(Currency.getInstance("NOK"),
                    SupportedCurrency.NOK.getCurrency());
        }

        @Test
        @DisplayName("Should return correct Currency instance for EUR")
        void returnsEur() {
            assertEquals(Currency.getInstance("EUR"),
                    SupportedCurrency.EUR.getCurrency());
        }
    }

    @Nested
    @DisplayName("values()")
    class Values {

        @Test
        @DisplayName("Should contain USD, EUR, NOK, GBP, SEK, DKK")
        void containsExpectedCurrencies() {
            List<String> codes = Arrays.stream(SupportedCurrency.values())
                    .map(c -> c.getCurrency().getCurrencyCode())
                    .toList();

            assertTrue(codes.containsAll(List.of("USD", "EUR", "NOK", "GBP", "SEK", "DKK")));
        }

        @Test
        @DisplayName("Should not be empty")
        void isNotEmpty() {
            assertTrue(SupportedCurrency.values().length > 0);
        }
    }
}
