package edu.ntnu.idatt2003.millions.model.currency;

import edu.ntnu.idatt2003.millions.model.currency.FixedRateCurrencyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FixedRateCurrencyConverter} class.
 * <p>
 * This test class verifies the behaviour of the FixedRateCurrencyConverter,
 * including conversion between supported currencies via the NOK pivot,
 * scale preservation, and validation of inputs.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class FixedRateCurrencyConverterTest {

    private FixedRateCurrencyConverter converter;
    private static final Currency NOK = Currency.getInstance("NOK");
    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency GBP = Currency.getInstance("GBP");
    private static final Currency SEK = Currency.getInstance("SEK");
    private static final Currency DKK = Currency.getInstance("DKK");
    private static final Currency JPY = Currency.getInstance("JPY");

    @BeforeEach
    void setUp() {
        converter = new FixedRateCurrencyConverter();
    }

    @Nested
    @DisplayName("convert()")
    class Convert {

        @Test
        @DisplayName("Should return same amount when from and to are equal")
        void returnsSameAmountForSameCurrency() {
            // Arrange
            BigDecimal amount = new BigDecimal("100.00");
            // Act
            BigDecimal result = converter.convert(amount, USD, USD);
            // Assert
            assertEquals(amount, result);
        }

        @Test
        @DisplayName("Should convert USD to NOK using rate")
        void convertsUsdToNok() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, USD, NOK);
            // Assert
            assertEquals(0, result.compareTo(new BigDecimal("9.21")));
        }

        @Test
        @DisplayName("Should convert NOK to USD using inverse rate")
        void convertsNokToUsd() {
            // Act
            BigDecimal result = converter.convert(new BigDecimal("9.21"), NOK, USD);
            // Assert
            assertEquals(0, result.compareTo(BigDecimal.ONE));
        }

        @Test
        @DisplayName("Should convert EUR to NOK using rate")
        void convertsEurToNok() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, EUR, NOK);
            // Assert
            assertEquals(0, result.compareTo(new BigDecimal("10.84")));
        }

        @Test
        @DisplayName("Should convert GBP to NOK using rate")
        void convertsGbpToNok() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, GBP, NOK);
            // Assert
            assertEquals(0, result.compareTo(new BigDecimal("12.54")));
        }

        @Test
        @DisplayName("Should convert SEK to NOK using rate")
        void convertsSekToNok() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, SEK, NOK);
            // Assert
            assertEquals(0, result.compareTo(new BigDecimal("1.00")));
        }

        @Test
        @DisplayName("Should convert DKK to NOK using rate")
        void convertsDkkToNok() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, DKK, NOK);
            // Assert
            assertEquals(0, result.compareTo(new BigDecimal("1.45")));
        }

        @Test
        @DisplayName("Should convert USD to EUR via NOK pivot")
        void convertsUsdToEurViaNokPivot() {
            // Arrange
            // 1 USD * 9.21 = 9.21 NOK; 9.21 NOK / 10.84 = 0.8496 EUR (HALF_UP, scale 4)
            BigDecimal expected = new BigDecimal("0.8496");
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, USD, EUR);
            // Assert
            assertEquals(0, result.compareTo(expected));
        }

        @Test
        @DisplayName("Should convert EUR to GBP via NOK pivot")
        void convertsEurToGbpViaNokPivot() {
            // Arrange
            // 1 EUR * 10.84 = 10.84 NOK; 10.84 NOK / 12.54 = 0.8644 GBP (HALF_UP, scale 4)
            BigDecimal expected = new BigDecimal("0.8644");
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, EUR, GBP);
            // Assert
            assertEquals(0, result.compareTo(expected));
        }

        @Test
        @DisplayName("Should return zero when amount is zero")
        void returnsZeroWhenAmountIsZero() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ZERO, USD, NOK);
            // Assert
            assertEquals(0, result.compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should preserve scale of four decimals on cross-currency conversion")
        void preservesScaleOfFourDecimals() {
            // Act
            BigDecimal result = converter.convert(BigDecimal.ONE, USD, EUR);
            // Assert
            assertEquals(4, result.scale());
        }

        @Test
        @DisplayName("Should handle large amounts without precision loss")
        void handlesLargeAmountsWithoutPrecisionLoss() {
            // Arrange
            BigDecimal largeAmount = new BigDecimal("1000000.00");
            BigDecimal expected = new BigDecimal("9210000.00");
            // Act
            BigDecimal result = converter.convert(largeAmount, USD, NOK);
            // Assert
            assertEquals(0, result.compareTo(expected));
        }

        @Test
        @DisplayName("Should throw exception when from currency is unsupported")
        void throwsExceptionWhenFromCurrencyIsUnsupported() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    converter.convert(BigDecimal.ONE, JPY, NOK));
        }

        @Test
        @DisplayName("Should throw exception when to currency is unsupported")
        void throwsExceptionWhenToCurrencyIsUnsupported() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    converter.convert(BigDecimal.ONE, NOK, JPY));
        }

        @Test
        @DisplayName("Should throw exception when amount is null")
        void throwsExceptionWhenAmountIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    converter.convert(null, USD, NOK));
        }

        @Test
        @DisplayName("Should throw exception when from currency is null")
        void throwsExceptionWhenFromCurrencyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    converter.convert(BigDecimal.ONE, null, NOK));
        }

        @Test
        @DisplayName("Should throw exception when to currency is null")
        void throwsExceptionWhenToCurrencyIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    converter.convert(BigDecimal.ONE, USD, null));
        }
    }
}
