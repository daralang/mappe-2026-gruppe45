package edu.ntnu.idatt2003.millions.factory;

import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;

/**
 * Factory for creating financial transactions.
 *
 * <p>Use this class to create {@link Purchase} and {@link Sale} transactions.
 * To execute a transaction, call <code>commit(player)</code> on the
 * returned object.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 *   Transaction t = TransactionFactory.createPurchase(share, week);
 *   t.commit(player);
 * </pre>
 */
public class TransactionFactory {

    private TransactionFactory() {
        // Utility class - should not be instantiated
    }

    /**
     * Creates a new purchase transaction for the given share.
     *
     * @param share the share to purchase
     * @param week  the current week number
     * @return a new {@link Purchase} ready to be committed
     * @throws NullPointerException     if share is null
     * @throws IllegalArgumentException if week is less than 1
     */
    public static Transaction createPurchase(Share share, int week) {
        return new Purchase(share, week);
    }

    /**
     * Creates a new sale transaction for the given share.
     *
     * @param share the share to sell
     * @param week  the current week number
     * @return a new {@link Sale} ready to be committed
     * @throws NullPointerException     if share is null
     * @throws IllegalArgumentException if week is less than 1
     */
    public static Transaction createSale(Share share, int week) {
        return new Sale(share, week);
    }
}