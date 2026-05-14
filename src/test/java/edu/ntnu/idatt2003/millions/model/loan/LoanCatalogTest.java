package edu.ntnu.idatt2003.millions.model.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link LoanCatalog} utility class.
 *
 * <p>The catalog has very little behaviour - it owns a fixed list of offers
 * and exposes a lookup. The tests pin down the contract callers depend on:
 * the list is non-empty, every risk level is represented, the returned list
 * cannot be mutated, and lookup by id behaves predictably for known and
 * unknown ids.</p>
 *
 * <p>All tests follow the AAA pattern.</p>
 */
class LoanCatalogTest {

    @Nested
    @DisplayName("getOffers()")
    class GetOffers {

        @Test
        @DisplayName("Should return three offers")
        void returnsThreeOffers() {
            // Act
            List<LoanOffer> offers = LoanCatalog.getOffers();
            // Assert
            assertEquals(3, offers.size());
        }

        @Test
        @DisplayName("Should include every risk level exactly once")
        void includesEveryRiskLevel() {
            // Act
            Set<LoanRiskLevel> levels = LoanCatalog.getOffers().stream()
                    .map(LoanOffer::riskLevel)
                    .collect(java.util.stream.Collectors.toSet());
            // Assert
            assertEquals(Set.of(LoanRiskLevel.LOW, LoanRiskLevel.MEDIUM, LoanRiskLevel.HIGH), levels);
        }

        @Test
        @DisplayName("Should return only offers with valid invariants")
        void allOffersHaveValidInvariants() {
            // Act & Assert
            for (LoanOffer offer : LoanCatalog.getOffers()) {
                assertNotNull(offer.id());
                assertFalse(offer.id().isBlank());
                assertTrue(offer.weeklyInterestRate().signum() > 0);
                assertTrue(offer.termWeeks() >= 1);
                assertTrue(offer.maxPrincipal().signum() > 0);
                assertNotNull(offer.riskLevel());
            }
        }

        @Test
        @DisplayName("Should return an unmodifiable list")
        @SuppressWarnings("DataFlowIssue")
        void returnsUnmodifiableList() {
            // Arrange
            List<LoanOffer> offers = LoanCatalog.getOffers();
            LoanOffer first = offers.getFirst();
            // Act & Assert
            assertThrows(UnsupportedOperationException.class, () -> offers.add(first));
        }

        @Test
        @DisplayName("Should return offers with unique ids")
        void offerIdsAreUnique() {
            // Act
            long distinctIds = LoanCatalog.getOffers().stream()
                    .map(LoanOffer::id)
                    .distinct()
                    .count();
            // Assert
            assertEquals(LoanCatalog.getOffers().size(), distinctIds);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Should return the offer with the given id")
        void returnsMatchingOffer() {
            // Act
            LoanOffer offer = LoanCatalog.findById("standard");
            // Assert
            assertEquals("standard", offer.id());
        }

        @Test
        @DisplayName("Should throw exception when id does not match any offer")
        void throwsExceptionForUnknownId() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCatalog.findById("nonexistent"));
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void throwsExceptionWhenIdIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    LoanCatalog.findById(null));
        }
    }
}