package edu.ntnu.idatt2003.millions.file.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EmptyStockFileException}.
 */
class EmptyStockFileExceptionTest {

    @Nested
    @DisplayName("EmptyStockFileException(String filePath)")
    class Constructor {

        @Test
        @DisplayName("Message contains the supplied file path")
        void messageContainsFilePath() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("/data/stocks.csv");
            // Assert
            assertTrue(ex.getMessage().contains("/data/stocks.csv"));
        }

        @Test
        @DisplayName("Message is not blank")
        void messageIsNotBlank() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("/data/stocks.csv");
            // Assert
            assertFalse(ex.getMessage().isBlank());
        }

        @Test
        @DisplayName("Message contains the supplied stream identifier")
        void messageContainsStreamIdentifier() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("<stream>");
            // Assert
            assertTrue(ex.getMessage().contains("<stream>"));
        }

        @Test
        @DisplayName("Message still contains context when file path is empty string")
        void messageIsNotBlankForEmptyFilePath() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("");
            // Assert
            assertFalse(ex.getMessage().isBlank());
        }
    }

    @Nested
    @DisplayName("Type hierarchy")
    class TypeHierarchy {

        @Test
        @DisplayName("Is a subtype of InvalidStockDataException")
        void isSubtypeOfInvalidStockDataException() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("/data/stocks.csv");
            // Assert
            assertInstanceOf(InvalidStockDataException.class, ex);
        }

        @Test
        @DisplayName("Is a checked exception")
        void isChecked() {
            // Arrange & Act & Assert
            assertTrue(Exception.class.isAssignableFrom(EmptyStockFileException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(EmptyStockFileException.class));
        }

        @Test
        @DisplayName("Can be caught as InvalidStockDataException")
        void canBeCaughtAsParent() {
            // Arrange
            boolean caught = false;
            // Act
            try {
                throw new EmptyStockFileException("/data/stocks.csv");
            } catch (InvalidStockDataException e) {
                caught = true;
            }
            // Assert
            assertTrue(caught);
        }
    }

    @Nested
    @DisplayName("Inherited fields from InvalidStockDataException")
    class InheritedFields {

        @Test
        @DisplayName("getLineNumber() is -1 since the error is file-level, not line-level")
        void lineNumberIsMinusOne() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("/data/stocks.csv");
            // Assert
            assertEquals(-1, ex.getLineNumber());
        }

        @Test
        @DisplayName("getLineContent() is null since the error is file-level, not line-level")
        void lineContentIsNull() {
            // Arrange & Act
            EmptyStockFileException ex = new EmptyStockFileException("/data/stocks.csv");
            // Assert
            assertNull(ex.getLineContent());
        }
    }
}
