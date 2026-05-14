package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Loan} record.
 */
class LoanTest {

    private LoanOffer offer;

    @BeforeEach
    void setUp() {
        offer = new LoanOffer("test", new BigDecimal("0.01"), 4,
                new BigDecimal("1000.00"), LoanRiskLevel.LOW);
    }

    @Nested
    @DisplayName("isDueThisWeek()")
    class IsDueThisWeek {

        @Test
        @DisplayName("returns true when weeksRemaining equals zero at due week")
        void isDueThisWeek_trueAtDueWeek() {
            // Loan taken at week 1, term 4 → due at week 5
            Loan loan = new Loan(offer, new BigDecimal("100.00"), 1);
            assertTrue(loan.isDueThisWeek(5));
        }

        @Test
        @DisplayName("returns false one week before the due week")
        void isDueThisWeek_falseOneWeekBefore() {
            Loan loan = new Loan(offer, new BigDecimal("100.00"), 1);
            assertFalse(loan.isDueThisWeek(4));
        }

        @Test
        @DisplayName("returns false at the week the loan was taken")
        void isDueThisWeek_falseAtTakenWeek() {
            Loan loan = new Loan(offer, new BigDecimal("100.00"), 1);
            assertFalse(loan.isDueThisWeek(1));
        }

        @Test
        @DisplayName("returns false when week is before takenAtWeek")
        void isDueThisWeek_falseBeforeTakenWeek() {
            Loan loan = new Loan(offer, new BigDecimal("100.00"), 3);
            assertFalse(loan.isDueThisWeek(2));
        }
    }
}
