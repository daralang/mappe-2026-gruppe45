package edu.ntnu.idatt2003.millions.model;

/**
 * Represents a purchase transaction for a given share.
 * A purchase records the share bought and the week it was acquired,
 * and uses a {@link PurchaseCalculator} to process the transaction.
 */
public class Purchase extends Transaction {

    /**
     * Constructs a new Purchase for the specified share and week.
     *
     * @param share the share being purchased
     * @param week  the week in which the purchase takes place
     */
    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    /**
     * TO BE DOCUMENTED
     */
    @Override
    public void commit(Player player) {
        // TO BE IMPLEMENTED
    }
}