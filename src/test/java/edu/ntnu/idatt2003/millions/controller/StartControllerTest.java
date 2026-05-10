package edu.ntnu.idatt2003.millions.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for validation helpers in {@link StartController}.
 *
 * <p>The tests cover controller input parsing without starting JavaFX.</p>
 */
class StartControllerTest {

    @Nested
    @DisplayName("parseCapital()")
    class ParseCapital {

        @Test
        @DisplayName("Should return capital when input is valid")
        void returnsCapitalWhenInputIsValid() {
            // Arrange
            String capital = "10000.00";
            // Act
            BigDecimal result = StartController.parseCapital(capital);
            // Assert
            assertEquals(0, new BigDecimal("10000.00").compareTo(result));
        }

        @Test
        @DisplayName("Should throw exception when input is null")
        void throwsExceptionWhenInputIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartController.parseCapital(null));
        }

        @Test
        @DisplayName("Should throw exception when input is blank")
        void throwsExceptionWhenInputIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartController.parseCapital(" "));
        }

        @Test
        @DisplayName("Should throw exception when input is not a valid number")
        void throwsExceptionWhenInputIsNotValidNumber() {
            // Act & Assert
            assertThrows(NumberFormatException.class, () ->
                    StartController.parseCapital("not-a-number"));
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
            File file = StartController.requireFilePath(path, "File must be selected");
            // Assert
            assertEquals(path, file.getPath());
        }

        @Test
        @DisplayName("Should throw exception when path is null")
        void throwsExceptionWhenPathIsNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartController.requireFilePath(null, "File must be selected"));
        }

        @Test
        @DisplayName("Should throw exception when path is blank")
        void throwsExceptionWhenPathIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    StartController.requireFilePath(" ", "File must be selected"));
        }

        @Test
        @DisplayName("Should use provided exception message when path is blank")
        void usesProvidedExceptionMessageWhenPathIsBlank() {
            // Arrange
            String message = "Stock file must be selected";
            // Act
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    StartController.requireFilePath(" ", message));
            // Assert
            assertEquals(message, exception.getMessage());
        }
    }
}
