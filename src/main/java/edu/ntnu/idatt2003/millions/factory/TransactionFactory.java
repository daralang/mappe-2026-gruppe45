package edu.ntnu.idatt2003.millions.factory;

import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;

import java.math.BigDecimal;

/**
 * Factory for creating financial transactions.
 *
 * <p>Use this class to create {@link Purchase} and {@link Sale} transactions.
 * A purchase can either use its calculated share-currency cost or an explicit
 * settlement amount prepared by a coordinating service. To execute a transaction,
 * call <code>commit(player)</code> on the returned object.</p>
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
     * Creates a new purchase transaction for the given share and settlement amount.
     *
     * @param share            the share to purchase
     * @param week             the current week number
     * @param settlementAmount the amount withdrawn when {@link Purchase#commit} is called
     * @return a new {@link Purchase} ready to be committed
     * @throws NullPointerException     if share or settlement amount is null
     * @throws IllegalArgumentException if week is less than 1 or settlement amount is negative
     */
    public static Transaction createPurchase(Share share, int week, BigDecimal settlementAmount) {
        return new Purchase(share, week, settlementAmount);
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