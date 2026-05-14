package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExcessiveDebtException}.
 */
class ExcessiveDebtExceptionTest {

    @Nested
    @DisplayName("ExcessiveDebtException(BigDecimal debtAfter, BigDecimal capacity)")
    class PayloadConstructor {

        @Test
        @DisplayName("message contains both debtAfter and capacity")
        void messageContainsBothValues() {
            ExcessiveDebtException ex = new ExcessiveDebtException(
                    new BigDecimal("600.00"), new BigDecimal("500.00"));
            assertTrue(ex.getMessage().contains("600.00"),
                    "message should contain debtAfter");
            assertTrue(ex.getMessage().contains("500.00"),
                    "message should contain capacity");
        }

        @Test
        @DisplayName("getDebtAfter() returns the supplied debtAfter")
        void getDebtAfterReturnValue() {
            BigDecimal debtAfter = new BigDecimal("600.00");
            ExcessiveDebtException ex = new ExcessiveDebtException(
                    debtAfter, new BigDecimal("500.00"));
            assertEquals(0, debtAfter.compareTo(ex.getDebtAfter()));
        }

        @Test
        @DisplayName("getCapacity() returns the supplied capacity")
        void getCapacityReturnValue() {
            BigDecimal capacity = new BigDecimal("500.00");
            ExcessiveDebtException ex = new ExcessiveDebtException(
                    new BigDecimal("600.00"), capacity);
            assertEquals(0, capacity.compareTo(ex.getCapacity()));
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(ExcessiveDebtException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(ExcessiveDebtException.class));
        }
    }

    @Nested
    @DisplayName("ExcessiveDebtException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            ExcessiveDebtException ex = new ExcessiveDebtException(
                    "custom message", new RuntimeException("root"));
            assertEquals("custom message", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            ExcessiveDebtException ex = new ExcessiveDebtException("msg", root);
            assertSame(root, ex.getCause());
        }

        @Test
        @DisplayName("payload fields are null when using chaining constructor")
        void payloadIsNullForChainingConstructor() {
            ExcessiveDebtException ex = new ExcessiveDebtException("msg", null);
            assertNull(ex.getDebtAfter());
            assertNull(ex.getCapacity());
        }
    }
}
