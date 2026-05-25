// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.loan;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Centralised source of the standard {@link LoanOffer}s available in the game.
 */
public final class LoanCatalog {

    private static final List<LoanOffer> OFFERS = List.of(
            new LoanOffer("standard", new BigDecimal("0.0010"), 20,
                    new BigDecimal("25000.00"), LoanRiskLevel.LOW),
            new LoanOffer("fast", new BigDecimal("0.0015"), 15,
                    new BigDecimal("50000.00"), LoanRiskLevel.MEDIUM),
            new LoanOffer("high-risk", new BigDecimal("0.0025"), 10,
                    new BigDecimal("100000.00"), LoanRiskLevel.HIGH)
    );

    private LoanCatalog() {}

    /**
     * Returns the unmodifiable list of standard loan offers.
     *
     * @return all available loan offers
     */
    public static List<LoanOffer> getOffers() {
        return OFFERS;
    }

    /**
     * Looks up an offer by its stable id.
     *
     * @param id the offer id to search for
     * @return the matching offer
     * @throws NullPointerException     if {@code id} is null
     * @throws IllegalArgumentException if no offer has the given id
     */
    public static LoanOffer findById(String id) {
        Objects.requireNonNull(id, "Id cannot be null");
        return OFFERS.stream()
                .filter(offer -> offer.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No loan offer with id: " + id));
    }
}