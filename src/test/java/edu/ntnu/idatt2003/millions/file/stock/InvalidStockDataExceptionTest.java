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
    @DisplayName("InvalidStockDataException(String i18nKey, Object[] args)")
    class NoCauseConstructor {

        @Test
        @DisplayName("getI18nKey() returns the supplied key")
        void i18nKeyIsPreserved() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.invalidStockData", new Object[]{5, "AAPL,Apple,abc"});
            assertEquals("error.stock.csv.invalidStockData", ex.getI18nKey());
        }

        @Test
        @DisplayName("getArgs() returns a copy with the supplied arguments")
        void argsArePreserved() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.blankSymbol", new Object[]{3, ",Apple,100"});
            Object[] args = ex.getArgs();
            assertEquals(3, args[0]);
            assertEquals(",Apple,100", args[1]);
        }

        @Test
        @DisplayName("getArgs() returns a defensive copy — mutating it does not affect stored args")
        void argsAreDefensiveCopy() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.blankSymbol", new Object[]{3, ",Apple,100"});
            Object[] copy = ex.getArgs();
            copy[0] = 999;
            assertEquals(3, ex.getArgs()[0]);
        }

        @Test
        @DisplayName("null args treated as empty — getArgs() returns empty array")
        void nullArgsBecomesEmpty() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.invalidStockData", null);
            assertEquals(0, ex.getArgs().length);
        }

        @Test
        @DisplayName("getMessage() contains the key when args are empty")
        void messageContainsKeyWhenNoArgs() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.invalidStockData", null);
            assertTrue(ex.getMessage().contains("error.stock.csv.invalidStockData"));
        }

        @Test
        @DisplayName("getMessage() contains key and args in fallback form")
        void messageContainsKeyAndArgs() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.blankSymbol", new Object[]{5, "AAPL,Apple,abc"});
            assertTrue(ex.getMessage().contains("error.stock.csv.blankSymbol"));
            assertTrue(ex.getMessage().contains("5"));
            assertTrue(ex.getMessage().contains("AAPL,Apple,abc"));
        }

        @Test
        @DisplayName("getCause() is null")
        void causeIsNull() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.blankName", new Object[]{1, "BAD"});
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(InvalidStockDataException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(InvalidStockDataException.class));
        }
    }

    @Nested
    @DisplayName("InvalidStockDataException(String i18nKey, Object[] args, Throwable cause)")
    class CauseConstructor {

        @Test
        @DisplayName("getI18nKey() returns the supplied key")
        void i18nKeyIsPreserved() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.nonPositivePrice",
                            new Object[]{"-5", 7, "AAPL,Apple,-5"}, null);
            assertEquals("error.stock.csv.nonPositivePrice", ex.getI18nKey());
        }

        @Test
        @DisplayName("getArgs() returns the supplied arguments")
        void argsArePreserved() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.nonPositivePrice",
                            new Object[]{"-5", 7, "AAPL,Apple,-5"}, null);
            Object[] args = ex.getArgs();
            assertEquals("-5", args[0]);
            assertEquals(7, args[1]);
            assertEquals("AAPL,Apple,-5", args[2]);
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.nonNumericPrice",
                            new Object[]{"abc", 3, "AAPL,Apple,abc"}, root);
            assertSame(root, ex.getCause());
        }

        @Test
        @DisplayName("getCause() is null when no cause is supplied")
        void causeIsNullWhenNotSupplied() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.blankSymbol", new Object[]{1, "BAD"}, null);
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("null args treated as empty")
        void nullArgsBecomesEmpty() {
            InvalidStockDataException ex =
                    new InvalidStockDataException("error.stock.csv.emptyFile", null, null);
            assertEquals(0, ex.getArgs().length);
        }
    }
}
