package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.calculator.TransactionCalculator;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.util.Objects;

/**
 * Represents an abstract financial transaction involving a share.
 * A transaction records the share, the week it takes place, and the calculator
 * used to process it. Subclasses must implement {@link #commit(Player)}.
 */
public abstract class Transaction {
    private final Share share;
    private final int week;
    private boolean committed;

    @SuppressWarnings("java:S2065") // transient is needed to prevent Gson from attempting to instantiate the TransactionCalculator interface
    private final transient TransactionCalculator calculator;

    /**
     * Constructs a new Transaction with the specified share, week, and calculator.
     *
     * @param share      the share involved in the transaction
     * @param week       the week in which the transaction takes place
     * @param calculator the calculator used to process the transaction
     * @throws NullPointerException     if the share is null
     * @throws IllegalArgumentException if the week is not between 1 and 52
     */
    protected Transaction(Share share, int week, TransactionCalculator calculator) {
        Objects.requireNonNull(share, "Share cannot be null");
        if (week < 1 || week > 52) throw new IllegalArgumentException("Week must be between 1 and 52");

        this.share = share;
        this.week = week;
        this.calculator = calculator;
        this.committed = false;
    }

    /**
     * Gets the share involved in this transaction.
     *
     * @return the share
     */
    public Share getShare() {
        return share;
    }

    /**
     * Gets the week in which this transaction takes place.
     *
     * @return the week number
     */
    public int getWeek() {
        return week;
    }

    /**
     * Gets the calculator used to process this transaction.
     * <p>
     * Note: returns null for transactions loaded from a saved game,
     * as the calculator field is transient and not persisted to JSON.
     * This is not a problem in practice because all transactions in the
     * archive are already committed before saving. Since commit() throws
     * an IllegalStateException if called on an already committed transaction,
     * the calculator will never be needed again after a game is resumed.
     * Any new transactions created after resuming will create their own
     * fresh calculators.
     *
     * @return the transaction calculator, or null if loaded from file
     */
    public TransactionCalculator getCalculator() {
        return calculator;
    }

    /**
     * Returns whether this transaction has been committed.
     *
     * @return {@code true} if the transaction has been committed, {@code false} otherwise
     */
    public boolean isCommitted() {
        return committed;
    }

    /**
     * Sets whether this transaction has been committed.
     *
     * @param committed {@code true} to mark the transaction as committed, {@code false} otherwise
     */
    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    /**
     * Commits this transaction for the given player.
     * Must be implemented by subclasses.
     *
     * @param player the player executing the transaction
     */
    public abstract void commit(Player player);
}