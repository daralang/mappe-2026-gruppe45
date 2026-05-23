package edu.ntnu.idatt2003.millions.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MoneyFormatter")
class MoneyFormatterTest {

    @AfterEach
    void resetToNorwegian() {
        LanguageManager.setLanguage(Language.NORWEGIAN);
    }

    @Nested
    @DisplayName("Norwegian locale")
    class NorwegianLocale {

        @BeforeEach
        void setNorwegian() {
            LanguageManager.setLanguage(Language.NORWEGIAN);
        }

        @Test
        @DisplayName("uses comma as decimal separator")
        void commaAsDecimalSeparator() {
            String result = MoneyFormatter.format(new BigDecimal("1234.56"));
            assertTrue(result.contains(","), "Expected comma decimal separator, got: " + result);
        }

        @Test
        @DisplayName("does not use comma as thousands separator")
        void noCommaAsThousandsSeparator() {
            String result = MoneyFormatter.format(new BigDecimal("1234.56"));
            assertFalse(result.contains("1,234"), "Expected non-comma grouping, got: " + result);
        }

        @Test
        @DisplayName("always formats with two decimal places")
        void twoDecimalPlaces() {
            String result = MoneyFormatter.format(new BigDecimal("5000.50"));
            assertTrue(result.endsWith(",50"), "Expected two decimals with comma, got: " + result);
        }

        @Test
        @DisplayName("formats zero with two decimal places")
        void zeroHasTwoDecimals() {
            String result = MoneyFormatter.format(BigDecimal.ZERO);
            assertTrue(result.contains(",00"), "Expected 0,00, got: " + result);
        }

        @Test
        @DisplayName("formats negative value with leading minus")
        void negativeValue() {
            String result = MoneyFormatter.format(new BigDecimal("-500.00"));
            assertTrue(result.startsWith("-"), "Expected minus sign, got: " + result);
        }

        @Test
        @DisplayName("negative value uses ASCII hyphen-minus not Unicode minus sign")
        void negativeValueUsesAsciiMinus() {
            // Spec: MoneyFormatter normalises the minus character to ASCII U+002D
            // for all locales. Without setMinusSign('-'), the nb-NO locale returns
            // U+2212 (MINUS SIGN) on some platforms (e.g. Windows), making output
            // inconsistent with ChangeFormatter and the rest of the display layer.
            // Arrange
            BigDecimal amount = new BigDecimal("-500.00");
            // Act
            String result = MoneyFormatter.format(amount);
            // Assert: first character must be U+002D HYPHEN-MINUS, not U+2212 MINUS SIGN
            assertEquals('-', result.charAt(0),
                    "Expected ASCII hyphen-minus U+002D but got U+" +
                    Integer.toHexString(result.charAt(0)).toUpperCase() + ": " + result);
        }

        @Test
        @DisplayName("formats large value with grouping")
        void largeValueHasGrouping() {
            String result = MoneyFormatter.format(new BigDecimal("1000000.00"));
            assertNotNull(result);
            assertFalse(result.isBlank());
            assertFalse(result.contains("1000000"), "Expected grouping separators, got: " + result);
        }

        @Test
        @DisplayName("rounds to two decimals")
        void roundsToTwoDecimals() {
            String result = MoneyFormatter.format(new BigDecimal("1.005"));
            assertTrue(result.contains(","), "Expected comma decimal, got: " + result);
        }
    }

    @Nested
    @DisplayName("English locale")
    class EnglishLocale {

        @BeforeEach
        void setEnglish() {
            LanguageManager.setLanguage(Language.ENGLISH);
        }

        @Test
        @DisplayName("uses dot as decimal separator")
        void dotAsDecimalSeparator() {
            String result = MoneyFormatter.format(new BigDecimal("1234.56"));
            assertTrue(result.contains("."), "Expected dot decimal separator, got: " + result);
        }

        @Test
        @DisplayName("uses comma as thousands separator")
        void commaAsThousandsSeparator() {
            String result = MoneyFormatter.format(new BigDecimal("1234.56"));
            assertTrue(result.contains("1,234"), "Expected comma grouping, got: " + result);
        }

        @Test
        @DisplayName("always formats with two decimal places")
        void twoDecimalPlaces() {
            String result = MoneyFormatter.format(new BigDecimal("5000.50"));
            assertTrue(result.endsWith(".50"), "Expected two decimals with dot, got: " + result);
        }

        @Test
        @DisplayName("formats zero with two decimal places")
        void zeroHasTwoDecimals() {
            String result = MoneyFormatter.format(BigDecimal.ZERO);
            assertTrue(result.contains(".00"), "Expected 0.00, got: " + result);
        }

        @Test
        @DisplayName("formats negative value with leading minus")
        void negativeValue() {
            String result = MoneyFormatter.format(new BigDecimal("-500.00"));
            assertTrue(result.startsWith("-"), "Expected minus sign, got: " + result);
        }

        @Test
        @DisplayName("formats large value with grouping")
        void largeValueHasGrouping() {
            String result = MoneyFormatter.format(new BigDecimal("1000000.00"));
            assertTrue(result.contains("1,000,000"), "Expected comma grouping, got: " + result);
        }
    }

    @Nested
    @DisplayName("locale switch")
    class LocaleSwitch {

        @Test
        @DisplayName("reflects language change on next call without restarting")
        void reflectsLanguageChangeImmediately() {
            LanguageManager.setLanguage(Language.NORWEGIAN);
            String norwegian = MoneyFormatter.format(new BigDecimal("1234.56"));

            LanguageManager.setLanguage(Language.ENGLISH);
            String english = MoneyFormatter.format(new BigDecimal("1234.56"));

            assertNotEquals(norwegian, english,
                    "Norwegian and English formats should differ");
            assertTrue(norwegian.contains(","), "Norwegian should have comma decimal");
            assertTrue(english.contains("."), "English should have dot decimal");
        }
    }
}
