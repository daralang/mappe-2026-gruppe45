package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InsufficientSaleProceedsException}.
 */
class InsufficientSaleProceedsExceptionTest {

    @Nested
    @DisplayName("InsufficientSaleProceedsException(BigDecimal shortfall)")
    class PayloadConstructor {

        @Test
        @DisplayName("message contains the shortfall amount")
        void messageContainsShortfall() {
            InsufficientSaleProceedsException ex =
                    new InsufficientSaleProceedsException(new BigDecimal("250.00"));
            assertTrue(ex.getMessage().contains("250.00"),
                    "message should contain the shortfall amount");
        }

        @Test
        @DisplayName("getShortfall() returns the supplied shortfall")
        void getShortfallReturnValue() {
            BigDecimal shortfall = new BigDecimal("250.00");
            InsufficientSaleProceedsException ex =
                    new InsufficientSaleProceedsException(shortfall);
            assertEquals(0, shortfall.compareTo(ex.getShortfall()));
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(
                    InsufficientSaleProceedsException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(
                    InsufficientSaleProceedsException.class));
        }
    }

    @Nested
    @DisplayName("InsufficientSaleProceedsException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            InsufficientSaleProceedsException ex =
                    new InsufficientSaleProceedsException("custom message", null);
            assertEquals("custom message", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            InsufficientSaleProceedsException ex =
                    new InsufficientSaleProceedsException("msg", root);
            assertSame(root, ex.getCause());
        }

        @Test
        @DisplayName("getShortfall() is null when using chaining constructor")
        void shortfallIsNullForChainingConstructor() {
            InsufficientSaleProceedsException ex =
                    new InsufficientSaleProceedsException("msg", null);
            assertNull(ex.getShortfall());
        }
    }
}
