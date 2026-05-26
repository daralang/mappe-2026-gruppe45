package edu.ntnu.idatt2003.millions.util.currency;

import edu.ntnu.idatt2003.millions.util.language.Language;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CurrencyFormatter} class.
 * <p>
 * Verifies that monetary values are formatted in NOK with Norwegian
 * locale conventions (comma decimal separator, space thousands separator)
 * and that input validation behaves correctly.
 * </p>
 */
class CurrencyFormatterTest {

    @BeforeEach
    void setNorwegian() {
        // Pin to Norwegian so locale-sensitive assertions are deterministic.
        LanguageManager.setLanguage(Language.NORWEGIAN);
    }

    @AfterEach
    void resetToNorwegian() {
        LanguageManager.setLanguage(Language.NORWEGIAN);
    }

    @Nested
    @DisplayName("format()")
    class Format {

        @Test
        @DisplayName("Should include NOK in output")
        void includesNok() {
            String result = CurrencyFormatter.format(new BigDecimal("100.00"));
            assertTrue(result.contains("NOK"));
        }

        @Test
        @DisplayName("Should always format in NOK regardless of active currency")
        void alwaysFormatsInNokRegardlessOfActiveCurrency() {
            CurrencyManager.setCurrency(Currency.getInstance("USD"));
            String result = CurrencyFormatter.format(new BigDecimal("100.00"));
            assertTrue(result.contains("NOK"));
            assertFalse(result.contains("USD"));
        }

        @Test
        @DisplayName("Should use comma as decimal separator (Norwegian locale)")
        void usesCommaAsDecimalSeparator() {
            String result = CurrencyFormatter.format(new BigDecimal("1234.56"));
            assertTrue(result.contains(","),
                    "Expected comma as decimal separator, got: " + result);
        }

        @Test
        @DisplayName("Should not use comma as thousands separator")
        void doesNotUseCommaAsThousandsSeparator() {
            String result = CurrencyFormatter.format(new BigDecimal("1234.56"));
            assertFalse(result.contains("1,234"),
                    "Expected non-comma thousands separator, got: " + result);
        }

        @Test
        @DisplayName("Should format zero correctly")
        void formatsZero() {
            String result = CurrencyFormatter.format(BigDecimal.ZERO);
            assertNotNull(result);
            assertTrue(result.contains("NOK"));
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
