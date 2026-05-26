package edu.ntnu.idatt2003.millions.util;

import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChangeFormatter")
class ChangeFormatterTest {

    @Nested
    @DisplayName("formatSignedPercent")
    class FormatSignedPercent {

        @Test
        @DisplayName("positive value ends with exactly one percent sign")
        void positiveValueEndsWithSinglePercent() {
            String result = ChangeFormatter.formatSignedPercent(new BigDecimal("5.5"));
            assertTrue(result.endsWith("%"), "Should end with %");
            assertFalse(result.contains("%%"), "Should not contain %%");
        }

        @Test
        @DisplayName("negative value ends with exactly one percent sign")
        void negativeValueEndsWithSinglePercent() {
            String result = ChangeFormatter.formatSignedPercent(new BigDecimal("-3.2"));
            assertTrue(result.endsWith("%"), "Should end with %");
            assertFalse(result.contains("%%"), "Should not contain %%");
        }

        @Test
        @DisplayName("zero value ends with exactly one percent sign")
        void zeroValueEndsWithSinglePercent() {
            String result = ChangeFormatter.formatSignedPercent(BigDecimal.ZERO);
            assertTrue(result.endsWith("%"), "Should end with %");
            assertFalse(result.contains("%%"), "Should not contain %%");
        }

        @Test
        @DisplayName("positive value starts with plus sign")
        void positiveValueStartsWithPlus() {
            String result = ChangeFormatter.formatSignedPercent(new BigDecimal("6.5"));
            assertTrue(result.startsWith("+"), "Positive value should start with +");
        }

        @Test
        @DisplayName("negative value does not start with plus sign")
        void negativeValueDoesNotStartWithPlus() {
            String result = ChangeFormatter.formatSignedPercent(new BigDecimal("-6.5"));
            assertFalse(result.startsWith("+"), "Negative value should not start with +");
        }
    }
}
