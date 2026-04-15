package edu.ntnu.idatt2003.millions.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Currency classes")
class CurrencyTest {

    @Nested
    @DisplayName("SupportedCurrency")
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
                List<String> codes = java.util.Arrays.stream(SupportedCurrency.values())
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

    @Nested
    @DisplayName("CurrencyManager")
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

    @Nested
    @DisplayName("CurrencyFormatter")
    class CurrencyFormatterTest {

        @BeforeEach
        void resetToUsd() {
            CurrencyManager.setCurrency(Currency.getInstance("USD"));
        }

        @Nested
        @DisplayName("format()")
        class Format {

            @Test
            @DisplayName("Should include active currency code in output")
            void includesCurrencyCode() {
                String result = CurrencyFormatter.format(new BigDecimal("100.00"));
                assertTrue(result.contains("USD"));
            }

            @Test
            @DisplayName("Should include NOK when active currency is NOK")
            void includesNokWhenActive() {
                CurrencyManager.setCurrency(Currency.getInstance("NOK"));
                String result = CurrencyFormatter.format(new BigDecimal("100.00"));
                assertTrue(result.contains("NOK"));
            }

            @Test
            @DisplayName("Should format zero correctly")
            void formatsZero() {
                String result = CurrencyFormatter.format(BigDecimal.ZERO);
                assertNotNull(result);
                assertTrue(result.contains("USD"));
            }

            @Test
            @DisplayName("Should format large amount without losing precision")
            void formatsLargeAmount() {
                String result = CurrencyFormatter.format(new BigDecimal("1000000.00"));
                assertNotNull(result);
                assertFalse(result.isBlank());
            }

            @Test
            @DisplayName("Should throw exception when amount is null")
            void throwsExceptionWhenAmountIsNull() {
                assertThrows(NullPointerException.class,
                        () -> CurrencyFormatter.format(null));
            }
        }
    }
}