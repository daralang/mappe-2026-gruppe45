package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link LoanOffer} record.
 *
 * <p>Covers the invariants enforced in the compact constructor and the
 * accessors that callers read offer terms through.</p>
 *
 * <p>All tests follow the AAA pattern.</p>
 */
class LoanOfferTest {

    private static final String VALID_ID = "standard";
    private static final BigDecimal VALID_RATE = new BigDecimal("0.0010");
    private static final int VALID_TERM = 20;
    private static final BigDecimal VALID_MAX = new BigDecimal("25000.00");
    private static final LoanRiskLevel VALID_RISK = LoanRiskLevel.LOW;

    @Nested
    @DisplayName("LoanOffer()")
    class Constructor {

        @Test
        @DisplayName("Should construct a valid offer when all arguments are valid")
        void constructsValidOffer() {
            // Act
            LoanOffer offer = new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK);
            // Assert
            assertEquals(VALID_ID, offer.id());
            assertEquals(0, VALID_RATE.compareTo(offer.weeklyInterestRate()));
            assertEquals(VALID_TERM, offer.termWeeks());
            assertEquals(0, VALID_MAX.compareTo(offer.maxPrincipal()));
            assertEquals(VALID_RISK, offer.riskLevel());
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void throwsExceptionWhenIdIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new LoanOffer(null, VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when id is blank")
        void throwsExceptionWhenIdIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer("  ", VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when weekly interest rate is null")
        void throwsExceptionWhenRateIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new LoanOffer(VALID_ID, null, VALID_TERM, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when weekly interest rate is zero")
        void throwsExceptionWhenRateIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, BigDecimal.ZERO, VALID_TERM, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when weekly interest rate is negative")
        void throwsExceptionWhenRateIsNegative() {
            // Arrange
            BigDecimal negativeRate = new BigDecimal("-0.0010");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, negativeRate, VALID_TERM, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when term is zero")
        void throwsExceptionWhenTermIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, 0, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when term is negative")
        void throwsExceptionWhenTermIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, -1, VALID_MAX, VALID_RISK));
        }

        @Test
        @DisplayName("Should accept a term of one week as the minimum valid value")
        void acceptsTermOfOneWeek() {
            // Act
            LoanOffer offer = new LoanOffer(VALID_ID, VALID_RATE, 1, VALID_MAX, VALID_RISK);
            // Assert
            assertEquals(1, offer.termWeeks());
        }

        @Test
        @DisplayName("Should throw exception when max principal is null")
        void throwsExceptionWhenMaxPrincipalIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, null, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when max principal is zero")
        void throwsExceptionWhenMaxPrincipalIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, BigDecimal.ZERO, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when max principal is negative")
        void throwsExceptionWhenMaxPrincipalIsNegative() {
            // Arrange
            BigDecimal negativeMax = new BigDecimal("-1.00");
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, negativeMax, VALID_RISK));
        }

        @Test
        @DisplayName("Should throw exception when risk level is null")
        void throwsExceptionWhenRiskLevelIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, VALID_MAX, null));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("Should treat two offers with identical fields as equal")
        void identicalOffersAreEqual() {
            // Arrange
            LoanOffer a = new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK);
            LoanOffer b = new LoanOffer(VALID_ID, VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK);
            // Act & Assert
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Should treat offers with different ids as unequal")
        void differentIdsAreUnequal() {
            // Arrange
            LoanOffer a = new LoanOffer("standard", VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK);
            LoanOffer b = new LoanOffer("fast", VALID_RATE, VALID_TERM, VALID_MAX, VALID_RISK);
            // Act & Assert
            assertNotEquals(a, b);
        }
    }
}