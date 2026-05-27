package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Loan} record.
 * Covers the compact constructor (all documented rejection rules) and every public
 * method — weeklyInterest, totalInterest, totalRepayment, weeksElapsed, weeksRemaining,
 * interestPaid, interestSaved, and isDueThisWeek.
 * All expected values are derived by hand from domain rules; none are read from the
 * implementation. All tests follow the AAA pattern.
 */
class LoanTest {

    // rate=1%/week, term=4 weeks, maxPrincipal=10000.00, LOW risk
    private static final LoanOffer STANDARD_OFFER = new LoanOffer(
            "standard", new BigDecimal("0.01"), 4,
            new BigDecimal("10000.00"), LoanRiskLevel.LOW);

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    // ─── Constructor ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Loan()")
    class LoanConstructor {

        @Test
        @DisplayName("Should throw NullPointerException when offer is null")
        void throwsNullPointerExceptionWhenOfferIsNull() {
            // Act & Assert
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> new Loan(null, new BigDecimal("100.00"), 1));
            assertEquals("Offer cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw NullPointerException when principal is null")
        void throwsNullPointerExceptionWhenPrincipalIsNull() {
            // Act & Assert
            NullPointerException ex = assertThrows(NullPointerException.class,
                    () -> new Loan(STANDARD_OFFER, null, 1));
            assertEquals("Principal cannot be null", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when principal is zero")
        void throwsIllegalArgumentExceptionWhenPrincipalIsZero() {
            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Loan(STANDARD_OFFER, BigDecimal.ZERO, 1));
            assertEquals("Principal must be positive", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when principal is negative")
        void throwsIllegalArgumentExceptionWhenPrincipalIsNegative() {
            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Loan(STANDARD_OFFER, new BigDecimal("-500.00"), 1));
            assertEquals("Principal must be positive", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when principal exceeds the offer maximum")
        void throwsIllegalArgumentExceptionWhenPrincipalExceedsMaximum() {
            // Act & Assert — STANDARD_OFFER maxPrincipal=10000.00; 10000.01 is strictly greater
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Loan(STANDARD_OFFER, new BigDecimal("10000.01"), 1));
            assertTrue(ex.getMessage().contains("exceeds offer maximum"),
                    "Message should name the excess: " + ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when takenAtWeek is negative")
        void throwsIllegalArgumentExceptionWhenTakenAtWeekIsNegative() {
            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Loan(STANDARD_OFFER, new BigDecimal("100.00"), -1));
            assertEquals("takenAtWeek must be non-negative", ex.getMessage());
        }

        @Test
        @DisplayName("Should accept a principal exactly equal to the offer maximum")
        void acceptsPrincipalEqualToOfferMaximum() {
            // Act & Assert — principal == maxPrincipal: compareTo returns 0, not > 0; must not throw
            assertDoesNotThrow(
                    () -> new Loan(STANDARD_OFFER, new BigDecimal("10000.00"), 1));
        }

        @Test
        @DisplayName("Should accept takenAtWeek of zero")
        void acceptsZeroAsTakenAtWeek() {
            // Act & Assert — zero is the minimum valid value for takenAtWeek
            assertDoesNotThrow(
                    () -> new Loan(STANDARD_OFFER, new BigDecimal("500.00"), 0));
        }

        @Test
        @DisplayName("Should store offer, principal, and takenAtWeek as supplied")
        void storesFieldsAsSupplied() {
            // Arrange
            BigDecimal principal = new BigDecimal("750.00");
            // Act
            Loan loan = new Loan(STANDARD_OFFER, principal, 3);
            // Assert
            assertSame(STANDARD_OFFER, loan.offer());
            assertBigDecimalEquals(principal, loan.principal());
            assertEquals(3, loan.takenAtWeek());
        }
    }

    // ─── weeklyInterest() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("weeklyInterest()")
    class WeeklyInterest {

        @Test
        @DisplayName("Should return principal times weeklyInterestRate rounded to 2 decimal places")
        void returnsRateTimesPrincipal() {
            // Arrange — principal=1000.00, rate=0.01; 1000.00 × 0.01 = 10.00
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("10.00"), loan.weeklyInterest());
        }

        @Test
        @DisplayName("Should round to 2 decimal places using HALF_UP when the raw product is not exact")
        void roundsToTwoDecimalsHalfUp() {
            // Arrange — rate=0.001, principal=333; raw = 333 × 0.001 = 0.333 → HALF_UP 2dp → 0.33
            // (third decimal digit is 3, which is < 5, so rounds down)
            LoanOffer roundingOffer = new LoanOffer("r", new BigDecimal("0.001"), 10,
                    new BigDecimal("10000.00"), LoanRiskLevel.LOW);
            Loan loan = new Loan(roundingOffer, new BigDecimal("333"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("0.33"), loan.weeklyInterest());
        }
    }

    // ─── totalInterest() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("totalInterest()")
    class TotalInterest {

        @Test
        @DisplayName("Should return weeklyInterest multiplied by the number of term weeks")
        void returnsWeeklyInterestTimesTermWeeks() {
            // Arrange — principal=1000.00, rate=0.01, term=4;
            // weeklyInterest=10.00; totalInterest=10.00 × 4 = 40.00
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("40.00"), loan.totalInterest());
        }

        @Test
        @DisplayName("Should use the rounded weekly amount, not the raw principal-times-rate product")
        void usesRoundedWeeklyInterestNotRawProduct() {
            // Arrange — rate=0.001, term=10, principal=333;
            // rounded weekly=0.33; totalInterest=0.33 × 10 = 3.30
            // (raw product 0.333 × 10 = 3.33 would be wrong — rounding applies first)
            LoanOffer roundingOffer = new LoanOffer("r", new BigDecimal("0.001"), 10,
                    new BigDecimal("10000.00"), LoanRiskLevel.LOW);
            Loan loan = new Loan(roundingOffer, new BigDecimal("333"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("3.30"), loan.totalInterest());
        }
    }

    // ─── totalRepayment() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("totalRepayment()")
    class TotalRepayment {

        @Test
        @DisplayName("Should return principal plus totalInterest")
        void returnsPrincipalPlusTotalInterest() {
            // Arrange — principal=1000.00, totalInterest=40.00; totalRepayment=1000.00+40.00=1040.00
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("1040.00"), loan.totalRepayment());
        }

        @Test
        @DisplayName("Should include the rounded total interest when weekly rounding is in effect")
        void includesRoundedTotalInterest() {
            // Arrange — rate=0.001, term=10, principal=333;
            // rounded weekly=0.33; totalInterest=3.30; totalRepayment=333+3.30=336.30
            LoanOffer roundingOffer = new LoanOffer("r", new BigDecimal("0.001"), 10,
                    new BigDecimal("10000.00"), LoanRiskLevel.LOW);
            Loan loan = new Loan(roundingOffer, new BigDecimal("333"), 1);
            // Act & Assert
            assertBigDecimalEquals(new BigDecimal("336.30"), loan.totalRepayment());
        }
    }

    // ─── weeksElapsed() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("weeksElapsed()")
    class WeeksElapsed {

        @Test
        @DisplayName("Should return zero at the week the loan was taken")
        void returnsZeroAtTheTakenWeek() {
            // Arrange — takenAtWeek=3, currentWeek=3; elapsed = 3 - 3 = 0
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 3);
            // Act & Assert
            assertEquals(0, loan.weeksElapsed(3));
        }

        @Test
        @DisplayName("Should return one after one week has passed")
        void returnsOneAfterOneWeek() {
            // Arrange — takenAtWeek=1, currentWeek=2; elapsed = 2 - 1 = 1
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
            // Act & Assert
            assertEquals(1, loan.weeksElapsed(2));
        }

        @Test
        @DisplayName("Should return the term length at the due week")
        void returnsTermLengthAtDueWeek() {
            // Arrange — takenAtWeek=1, term=4, currentWeek=5 (due week); elapsed = 5 - 1 = 4
            Loan loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
            // Act & Assert
            assertEquals(4, loan.weeksElapsed(5));
        }
    }

    // ─── weeksRemaining() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("weeksRemaining()")
    class WeeksRemaining {

        private Loan loan;

        /**
         * Initializes test fixtures before each test.
         * Test data values were generated with AI assistance and reviewed manually.
         */
        @BeforeEach
        void setUp() {
            // principal=1000.00, rate=0.01, term=4, takenAtWeek=1; due week = 1+4 = 5
            loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
        }

        @Test
        @DisplayName("Should return the full term length at the week the loan was taken")
        void returnsFullTermAtTakenWeek() {
            // Act & Assert — currentWeek=1; elapsed=0; remaining = max(0, 4-0) = 4
            assertEquals(4, loan.weeksRemaining(1));
        }

        @Test
        @DisplayName("Should return one in the week immediately before the due week")
        void returnsOneOneWeekBeforeDue() {
            // Act & Assert — currentWeek=4; elapsed=3; remaining = max(0, 4-3) = 1
            assertEquals(1, loan.weeksRemaining(4));
        }

        @Test
        @DisplayName("Should return zero at the due week")
        void returnsZeroAtDueWeek() {
            // Act & Assert — currentWeek=5; elapsed=4; remaining = max(0, 4-4) = 0
            assertEquals(0, loan.weeksRemaining(5));
        }

        @Test
        @DisplayName("Should return zero one week past the due week — clamped, not negative")
        void returnsZeroOneWeekPastDue() {
            // Act & Assert — currentWeek=6; elapsed=5; max(0, 4-5) = max(0, -1) = 0
            assertEquals(0, loan.weeksRemaining(6));
        }

        @Test
        @DisplayName("Should return zero when well past the due week")
        void returnsZeroWhenWellPastDue() {
            // Act & Assert — currentWeek=20; elapsed=19; max(0, 4-19) = 0
            assertEquals(0, loan.weeksRemaining(20));
        }
    }

    // ─── interestPaid() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("interestPaid()")
    class InterestPaid {

        private Loan loan;

        /**
         * Initializes test fixtures before each test.
         * Test data values were generated with AI assistance and reviewed manually.
         */
        @BeforeEach
        void setUp() {
            // principal=1000.00, rate=0.01 → weekly=10.00; term=4, takenAtWeek=1
            loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
        }

        @Test
        @DisplayName("Should return zero at the week the loan was taken")
        void returnsZeroAtTakenWeek() {
            // Act & Assert — elapsed=0; paid = 10.00 × 0 = 0
            assertBigDecimalEquals(BigDecimal.ZERO, loan.interestPaid(1));
        }

        @Test
        @DisplayName("Should return weekly interest times elapsed weeks after several weeks")
        void returnsWeeklyTimesElapsedAfterSeveralWeeks() {
            // Act & Assert — currentWeek=4; elapsed=3; paid = 10.00 × 3 = 30.00
            assertBigDecimalEquals(new BigDecimal("30.00"), loan.interestPaid(4));
        }

        @Test
        @DisplayName("Should return the full total interest at the due week")
        void returnsFullTotalInterestAtDueWeek() {
            // Act & Assert — currentWeek=5; elapsed=4; paid = 10.00 × 4 = 40.00 (= totalInterest)
            assertBigDecimalEquals(new BigDecimal("40.00"), loan.interestPaid(5));
        }
    }

    // ─── interestSaved() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("interestSaved()")
    class InterestSaved {

        private Loan loan;

        /**
         * Initializes test fixtures before each test.
         * Test data values were generated with AI assistance and reviewed manually.
         */
        @BeforeEach
        void setUp() {
            // principal=1000.00, rate=0.01 → weekly=10.00; term=4, takenAtWeek=1; due week=5
            loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
        }

        @Test
        @DisplayName("Should return the full total interest at the week the loan was taken")
        void returnsFullInterestAtTakenWeek() {
            // Act & Assert — currentWeek=1; remaining=4; saved = 10.00 × 4 = 40.00 (= totalInterest)
            assertBigDecimalEquals(new BigDecimal("40.00"), loan.interestSaved(1));
        }

        @Test
        @DisplayName("Should return one week's interest when repaid one week before the due week")
        void returnsOneWeeklyInterestOneWeekBeforeDue() {
            // Act & Assert — currentWeek=4; remaining=1; saved = 10.00 × 1 = 10.00
            assertBigDecimalEquals(new BigDecimal("10.00"), loan.interestSaved(4));
        }

        @Test
        @DisplayName("Should return zero at the due week — nothing left to save")
        void returnsZeroAtDueWeek() {
            // Act & Assert — currentWeek=5; remaining=0; saved = 10.00 × 0 = 0
            assertBigDecimalEquals(BigDecimal.ZERO, loan.interestSaved(5));
        }

        @Test
        @DisplayName("Should return zero after the due week has passed")
        void returnsZeroAfterDueWeekHasPassed() {
            // Act & Assert — currentWeek=6; remaining=0 (clamped); saved = 10.00 × 0 = 0
            assertBigDecimalEquals(BigDecimal.ZERO, loan.interestSaved(6));
        }
    }

    // ─── isDueThisWeek() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isDueThisWeek()")
    class IsDueThisWeek {

        private Loan loan;

        /**
         * Initializes test fixtures before each test.
         * Test data values were generated with AI assistance and reviewed manually.
         */
        @BeforeEach
        void setUp() {
            // takenAtWeek=1, term=4; due week = 1 + 4 = 5
            loan = new Loan(STANDARD_OFFER, new BigDecimal("1000.00"), 1);
        }

        @Test
        @DisplayName("Should return false at the week the loan was taken")
        void returnsFalseAtTakenWeek() {
            // Act & Assert — currentWeek=1; remaining=4 ≠ 0
            assertFalse(loan.isDueThisWeek(1));
        }

        @Test
        @DisplayName("Should return false one week before the due week")
        void returnsFalseOneWeekBeforeDue() {
            // Act & Assert — currentWeek=4; remaining=1 ≠ 0
            assertFalse(loan.isDueThisWeek(4));
        }

        @Test
        @DisplayName("Should return true at the exact due week")
        void returnsTrueAtExactDueWeek() {
            // Act & Assert — currentWeek=5 = takenAtWeek + term = 1 + 4; remaining=0
            assertTrue(loan.isDueThisWeek(5));
        }

        @Test
        @DisplayName("Should return true one week past the due week — at-or-past semantics")
        void returnsTrueOneWeekPastDue() {
            // Act & Assert — currentWeek=6; remaining=max(0, 4-5)=0 → true.
            // Contract: isDueThisWeek returns true for currentWeek >= takenAtWeek + termWeeks.
            // This prevents a skipped due week from silently leaving a loan unsettled.
            assertTrue(loan.isDueThisWeek(6));
        }
    }
}
