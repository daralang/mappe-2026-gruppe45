package edu.ntnu.idatt2003.millions.service.loan;

import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanPreview;
import edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LoanPreviewService}.
 * Covers {@link LoanPreviewService#preview} including weekly interest computation,
 * total-interest accumulation, repayment sum, HALF_UP rounding, and all documented
 * rejection rules (null arguments, non-positive principal).
 * All tests follow the AAA pattern.
 */
class LoanPreviewServiceTest {

    private LoanPreviewService service;

    @BeforeEach
    void setUp() {
        service = new LoanPreviewService();
    }

    private static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertEquals(0, expected.compareTo(actual),
                "Expected " + expected + " but was " + actual);
    }

    private static LoanOffer offer(String weeklyRate, int termWeeks) {
        return new LoanOffer("test", new BigDecimal(weeklyRate),
                termWeeks, new BigDecimal("1000000"), LoanRiskLevel.LOW);
    }

    @Nested
    @DisplayName("preview()")
    class Preview {

        @Test
        @DisplayName("Should compute weeklyInterestAmount as principal times weeklyRate")
        void computesWeeklyInterestAmount() {
            // Arrange - rate=1%, principal=1000; weeklyInterest=1000×0.01=10.00
            LoanOffer o = offer("0.01", 10);

            // Act
            LoanPreview preview = service.preview(o, new BigDecimal("1000"));

            // Assert
            assertBigDecimalEquals(new BigDecimal("10.00"), preview.weeklyInterestAmount());
        }

        @Test
        @DisplayName("Should compute totalInterest as weeklyInterestAmount times termWeeks")
        void computesTotalInterest() {
            // Arrange - weeklyInterest=10.00, term=10; totalInterest=10.00×10=100.00
            LoanOffer o = offer("0.01", 10);

            // Act
            LoanPreview preview = service.preview(o, new BigDecimal("1000"));

            // Assert
            assertBigDecimalEquals(new BigDecimal("100.00"), preview.totalInterest());
        }

        @Test
        @DisplayName("Should compute totalRepayment as principal plus totalInterest")
        void computesTotalRepayment() {
            // Arrange - principal=1000, totalInterest=100.00; totalRepayment=1100.00
            LoanOffer o = offer("0.01", 10);

            // Act
            LoanPreview preview = service.preview(o, new BigDecimal("1000"));

            // Assert
            assertBigDecimalEquals(new BigDecimal("1100.00"), preview.totalRepayment());
        }

        @Test
        @DisplayName("Should compute correct values for a different rate and term")
        void computesCorrectValuesForRateZeroPointZeroOneFive() {
            // Arrange - rate=1.5%, term=4, principal=500
            // weeklyInterest=500×0.015=7.50; totalInterest=7.50×4=30.00; totalRepayment=530.00
            LoanOffer o = offer("0.015", 4);

            // Act
            LoanPreview preview = service.preview(o, new BigDecimal("500"));

            // Assert
            assertBigDecimalEquals(new BigDecimal("7.50"), preview.weeklyInterestAmount());
            assertBigDecimalEquals(new BigDecimal("30.00"), preview.totalInterest());
            assertBigDecimalEquals(new BigDecimal("530.00"), preview.totalRepayment());
        }

        @Test
        @DisplayName("Should round weekly interest amount to 2 decimal places using HALF_UP")
        void roundsWeeklyInterestToTwoDecimalsHalfUp() {
            // Arrange - rate=0.001, principal=333
            // rawWeekly = 333×0.001 = 0.333 → HALF_UP scale 2 = 0.33
            // totalInterest = 0.33×10=3.30; totalRepayment=333+3.30=336.30
            LoanOffer o = offer("0.001", 10);

            // Act
            LoanPreview preview = service.preview(o, new BigDecimal("333"));

            // Assert
            assertBigDecimalEquals(new BigDecimal("0.33"), preview.weeklyInterestAmount());
            assertBigDecimalEquals(new BigDecimal("3.30"), preview.totalInterest());
            assertBigDecimalEquals(new BigDecimal("336.30"), preview.totalRepayment());
        }

        @Test
        @DisplayName("Should throw NullPointerException when offer is null")
        void throwsWhenOfferIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.preview(null, new BigDecimal("1000")));
        }

        @Test
        @DisplayName("Should throw NullPointerException when principal is null")
        void throwsWhenPrincipalIsNull() {
            // Arrange
            LoanOffer o = offer("0.01", 10);

            // Act & Assert
            assertThrows(NullPointerException.class,
                    () -> service.preview(o, null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when principal is zero")
        void throwsWhenPrincipalIsZero() {
            // Arrange - spec: principal must be positive
            LoanOffer o = offer("0.01", 10);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.preview(o, BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when principal is negative")
        void throwsWhenPrincipalIsNegative() {
            // Arrange
            LoanOffer o = offer("0.01", 10);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> service.preview(o, new BigDecimal("-500")));
        }
    }
}
