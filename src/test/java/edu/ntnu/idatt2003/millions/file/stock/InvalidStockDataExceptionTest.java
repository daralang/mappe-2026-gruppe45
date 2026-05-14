package edu.ntnu.idatt2003.millions.file.stock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InvalidStockDataException}.
 */
class InvalidStockDataExceptionTest {

    @Nested
    @DisplayName("InvalidStockDataException(int lineNumber, String lineContent)")
    class PayloadConstructor {

        @Test
        @DisplayName("getLineNumber() returns the supplied line number")
        void lineNumberIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            // Assert
            assertEquals(5, ex.getLineNumber());
        }

        @Test
        @DisplayName("getLineContent() returns the supplied line content")
        void lineContentIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            // Assert
            assertEquals("AAPL,Apple,abc", ex.getLineContent());
        }

        @Test
        @DisplayName("message contains the line number")
        void messageContainsLineNumber() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            // Assert
            assertTrue(ex.getMessage().contains("5"),
                    "message should contain the line number");
        }

        @Test
        @DisplayName("message contains the line content")
        void messageContainsLineContent() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            // Assert
            assertTrue(ex.getMessage().contains("AAPL,Apple,abc"),
                    "message should contain the raw line content");
        }

        @Test
        @DisplayName("getLineNumber() returns 1 for the first line")
        void lineNumberOneIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(1, "BAD");
            // Assert
            assertEquals(1, ex.getLineNumber());
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            // Arrange & Act & Assert
            assertTrue(Exception.class.isAssignableFrom(InvalidStockDataException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(InvalidStockDataException.class));
        }
    }

    @Nested
    @DisplayName("InvalidStockDataException(int lineNumber, String lineContent, String reason)")
    class ReasonConstructor {

        @Test
        @DisplayName("getLineNumber() returns the supplied line number")
        void lineNumberIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(3, ",Apple,100", "blank symbol");
            // Assert
            assertEquals(3, ex.getLineNumber());
        }

        @Test
        @DisplayName("getLineContent() returns the supplied line content")
        void lineContentIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(3, ",Apple,100", "blank symbol");
            // Assert
            assertEquals(",Apple,100", ex.getLineContent());
        }

        @Test
        @DisplayName("message contains the reason")
        void messageContainsReason() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(3, ",Apple,100", "blank symbol");
            // Assert
            assertTrue(ex.getMessage().contains("blank symbol"));
        }

        @Test
        @DisplayName("message contains the line number")
        void messageContainsLineNumber() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(3, ",Apple,100", "blank symbol");
            // Assert
            assertTrue(ex.getMessage().contains("3"));
        }

        @Test
        @DisplayName("message contains the line content")
        void messageContainsLineContent() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(3, ",Apple,100", "blank symbol");
            // Assert
            assertTrue(ex.getMessage().contains(",Apple,100"));
        }

        @Test
        @DisplayName("message contains reason when reason describes a non-positive price")
        void messageContainsNonPositivePriceReason() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(
                    7, "AAPL,Apple,-5", "non-positive price \"-5\"");
            // Assert
            assertTrue(ex.getMessage().contains("non-positive price"));
        }

        @Test
        @DisplayName("message still contains line number and content when reason is empty")
        void emptyReasonStillIncludesLineContext() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException(2, "MSFT,Microsoft,0", "");
            // Assert
            assertTrue(ex.getMessage().contains("2"));
            assertTrue(ex.getMessage().contains("MSFT,Microsoft,0"));
        }
    }

    @Nested
    @DisplayName("InvalidStockDataException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            // Arrange & Act
            InvalidStockDataException ex =
                    new InvalidStockDataException("custom message", null);
            // Assert
            assertEquals("custom message", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            // Arrange
            RuntimeException root = new RuntimeException("root");
            // Act
            InvalidStockDataException ex = new InvalidStockDataException("msg", root);
            // Assert
            assertSame(root, ex.getCause());
        }

        @Test
        @DisplayName("getCause() is null when no cause is supplied")
        void causeIsNullWhenNotSupplied() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException("msg", null);
            // Assert
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("getLineNumber() is -1 and getLineContent() is null for chaining constructor")
        void payloadIsDefaultForChainingConstructor() {
            // Arrange & Act
            InvalidStockDataException ex = new InvalidStockDataException("msg", null);
            // Assert
            assertEquals(-1, ex.getLineNumber());
            assertNull(ex.getLineContent());
        }
    }
}
