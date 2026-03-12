package edu.ntnu.idatt2003.millions.model;

/**
 * Represents an abstract financial transaction involving a share.
 * A transaction records the share, the week it takes place, and the calculator
 * used to process it. Subclasses must implement {@link #commit(Player)}.
 */
public abstract class Transaction {
    private final Share share;
    private final int week;
    private final TransactionCalculator calculator;
    private boolean committed;

    /**
     * Constructs a new Transaction with the specified share, week, and calculator.
     *
     * @param share      the share involved in the transaction
     * @param week       the week in which the transaction takes place
     * @param calculator the calculator used to process the transaction
     */
    protected Transaction(Share share, int week, TransactionCalculator calculator) {
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
     *
     * @return the transaction calculator
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