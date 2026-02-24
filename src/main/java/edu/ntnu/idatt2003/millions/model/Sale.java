package edu.ntnu.idatt2003.millions.model;

/**
 * Represents a sale transaction for a given share.
 * A sale records the share being sold and the week it was sold,
 * and uses a {@link SalesCalculator} to process the transaction.
 */
public class Sale extends Transaction {

    /**
     * Constructs a new Sale for the specified share and week.
     *
     * @param share the share being sold
     * @param week  the week in which the sale takes place
     */
    public Sale(Share share, int week) {
        super(share, week, new SalesCalculator(share));
    }

    /**
     * TO BE DOCUMENTED
     */
    @Override
    public void commit(Player player) {
        // TO BE IMPLEMENTED
    }
}
