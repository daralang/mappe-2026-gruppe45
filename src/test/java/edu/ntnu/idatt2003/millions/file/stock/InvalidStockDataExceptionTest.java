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
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            assertEquals(5, ex.getLineNumber());
        }

        @Test
        @DisplayName("getLineContent() returns the supplied line content")
        void lineContentIsPreserved() {
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            assertEquals("AAPL,Apple,abc", ex.getLineContent());
        }

        @Test
        @DisplayName("message contains the line number")
        void messageContainsLineNumber() {
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            assertTrue(ex.getMessage().contains("5"),
                    "message should contain the line number");
        }

        @Test
        @DisplayName("message contains the line content")
        void messageContainsLineContent() {
            InvalidStockDataException ex = new InvalidStockDataException(5, "AAPL,Apple,abc");
            assertTrue(ex.getMessage().contains("AAPL,Apple,abc"),
                    "message should contain the raw line content");
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(InvalidStockDataException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(InvalidStockDataException.class));
        }
    }

    @Nested
    @DisplayName("InvalidStockDataException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("custom message", null);
            assertEquals("custom message", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            InvalidStockDataException ex = new InvalidStockDataException("msg", root);
            assertSame(root, ex.getCause());
        }

        @Test
        @DisplayName("getLineNumber() is -1 and getLineContent() is null for chaining constructor")
        void payloadIsDefaultForChainingConstructor() {
            InvalidStockDataException ex = new InvalidStockDataException("msg", null);
            assertEquals(-1, ex.getLineNumber());
            assertNull(ex.getLineContent());
        }
    }
}
