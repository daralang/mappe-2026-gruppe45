package edu.ntnu.idatt2003.millions.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CurrencyManager} class.
 * <p>
 * Verifies the active currency lifecycle: default value, updates,
 * and the list of supported currencies exposed for selection.
 * </p>
 */
class CurrencyManagerTest {

    @BeforeEach
    void resetToDefault() {
        CurrencyManager.setCurrency(Currency.getInstance("USD"));
    }

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("Should return USD as default currency")
        void returnsUsdByDefault() {
            assertEquals(Currency.getInstance("USD"), CurrencyManager.get());
        }

        @Test
        @DisplayName("Should return currency after it has been changed")
        void returnsUpdatedCurrency() {
            CurrencyManager.setCurrency(Currency.getInstance("NOK"));
            assertEquals(Currency.getInstance("NOK"), CurrencyManager.get());
        }
    }

    @Nested
    @DisplayName("setCurrency()")
    class SetCurrency {

        @Test
        @DisplayName("Should update the active currency")
        void updatesActiveCurrency() {
            CurrencyManager.setCurrency(Currency.getInstance("EUR"));
            assertEquals(Currency.getInstance("EUR"), CurrencyManager.get());
        }

        @Test
        @DisplayName("Should throw exception when currency is null")
        void throwsExceptionWhenCurrencyIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CurrencyManager.setCurrency(null));
        }

        @Test
        @DisplayName("Should allow setting back to USD after change")
        void allowsSettingBackToUsd() {
            CurrencyManager.setCurrency(Currency.getInstance("GBP"));
            CurrencyManager.setCurrency(Currency.getInstance("USD"));
            assertEquals(Currency.getInstance("USD"), CurrencyManager.get());
        }
    }

    @Nested
    @DisplayName("getSupportedCurrencies()")
    class GetSupportedCurrencies {

        @Test
        @DisplayName("Should return a non-empty list")
        void returnsNonEmptyList() {
            assertFalse(CurrencyManager.getSupportedCurrencies().isEmpty());
        }

        @Test
        @DisplayName("Should contain USD")
        void containsUsd() {
            assertTrue(CurrencyManager.getSupportedCurrencies()
                    .contains(Currency.getInstance("USD")));
        }

        @Test
        @DisplayName("Should return same size as SupportedCurrency enum")
        void matchesSupportedCurrencyEnum() {
            assertEquals(
                    SupportedCurrency.values().length,
                    CurrencyManager.getSupportedCurrencies().size()
            );
        }

        @Test
        @DisplayName("Should not contain duplicates")
        void containsNoDuplicates() {
            List<Currency> currencies = CurrencyManager.getSupportedCurrencies();
            long distinctCount = currencies.stream().distinct().count();
            assertEquals(currencies.size(), distinctCount);
        }
    }
}
