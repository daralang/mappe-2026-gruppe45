package edu.ntnu.idatt2003.millions.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StartInputValidator}.
 *
 * <p>The tests cover controller input parsing without starting JavaFX.</p>
 */
class StartInputValidatorTest {

    @Nested
    @DisplayName("requireName()")
    class RequireName {

        @Test
        @DisplayName("Should return trimmed name when input is valid")
        void returnsTrimmedNameWhenInputIsValid() {
            // Arrange
            String name = "  Dara  ";
            // Act
            String result = StartInputValidator.requireName(name);
            // Assert
            assertEquals("Dara", result);
        }

        @Test
        @DisplayName("Should throw exception when input is null")
        void throwsExceptionWhenInputIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireName(null));
        }

        @Test
        @DisplayName("Should throw exception when input is blank")
        void throwsExceptionWhenInputIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireName("   "));
        }
    }

    @Nested
    @DisplayName("parseCapital()")
    class ParseCapital {

        @Test
        @DisplayName("Should return capital when input is valid")
        void returnsCapitalWhenInputIsValid() {
            // Arrange
            String capital = "10000.00";
            // Act
            BigDecimal result = StartInputValidator.parseCapital(capital);
            // Assert
            assertEquals(0, new BigDecimal("10000.00").compareTo(result));
        }

        @Test
        @DisplayName("Should throw exception when input is null")
        void throwsExceptionWhenInputIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.parseCapital(null));
        }

        @Test
        @DisplayName("Should throw exception when input is blank")
        void throwsExceptionWhenInputIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.parseCapital(" "));
        }

        @Test
        @DisplayName("Should throw exception when input is not a valid number")
        void throwsExceptionWhenInputIsNotValidNumber() {
            // Act & Assert
            assertThrows(NumberFormatException.class, () ->
                    StartInputValidator.parseCapital("not-a-number"));
        }
    }

    @Nested
    @DisplayName("requireFilePath()")
    class RequireFilePath {

        @Test
        @DisplayName("Should return file when path is valid")
        void returnsFileWhenPathIsValid() {
            // Arrange
            String path = "stocks.csv";
            // Act
            File file = StartInputValidator.requireFilePath(path, "File must be selected");
            // Assert
            assertEquals(path, file.getPath());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireFilePath(null, "File must be selected"));
        }

        @Test
        @DisplayName("Should throw exception when path is blank")
        void throwsExceptionWhenPathIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireFilePath(" ", "File must be selected"));
        }

        @Test
        @DisplayName("Should use provided exception message when path is blank")
        void usesProvidedExceptionMessageWhenPathIsBlank() {
            // Arrange
            String message = "Stock file must be selected";
            // Act
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireFilePath(" ", message));
            // Assert
            assertEquals(message, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("requireCurrency()")
    class RequireCurrency {

        @Test
        @DisplayName("Should return currency when input is valid")
        void returnsCurrencyWhenInputIsValid() {
            // Arrange
            Currency currency = Currency.getInstance("USD");
            // Act
            Currency result = StartInputValidator.requireCurrency(currency);
            // Assert
            assertEquals(currency, result);
        }

        @Test
        @DisplayName("Should throw exception when input is null")
        void throwsExceptionWhenInputIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireCurrency(null));
        }
    }

    @Nested
    @DisplayName("requireCsvFilePath()")
    class RequireCsvFilePath {

        @Test
        @DisplayName("Should return file when path ends with csv")
        void returnsFileWhenPathEndsWithCsv() {
            // Arrange
            String path = "stocks.csv";
            // Act
            File file = StartInputValidator.requireCsvFilePath(path);
            // Assert
            assertEquals(path, file.getPath());
        }

        @Test
        @DisplayName("Should return file when path ends with uppercase CSV")
        void returnsFileWhenPathEndsWithUppercaseCsv() {
            // Arrange
            String path = "stocks.CSV";
            // Act
            File file = StartInputValidator.requireCsvFilePath(path);
            // Assert
            assertEquals(path, file.getPath());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireCsvFilePath(null));
        }

        @Test
        @DisplayName("Should throw exception when path is blank")
        void throwsExceptionWhenPathIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireCsvFilePath(" "));
        }

        @Test
        @DisplayName("Should throw exception when path is not csv")
        void throwsExceptionWhenPathIsNotCsv() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartInputValidator.requireCsvFilePath("stocks.json"));
        }
    }
}
