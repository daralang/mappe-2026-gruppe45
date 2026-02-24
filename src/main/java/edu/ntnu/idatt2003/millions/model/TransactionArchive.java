package edu.ntnu.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an archive of transactions.
 * The archive stores the transactions added and provides methods for retrieving
 * transactions by week, type ({@link Purchase} or {@link Sale}), and counting distinct weeks of activity.
 */
public class TransactionArchive {
    private final List<Transaction> transactions;

    /**
     * Constructs a new empty TransactionArchive.
     */
    public TransactionArchive() {
        transactions = new ArrayList<>();
    }

    /**
     * Adds a transaction to the archive if it is not already present.
     *
     * @param transaction the transaction to add
     * @return {@code true} if the transaction was added, {@code false} if it already existed
     */
    public boolean add(Transaction transaction) {
        if (!transactions.contains(transaction)) {
            transactions.add(transaction);
            return true;
        }
        return false;
    }

    /**
     * Returns whether the archive contains transactions or not.
     *
     * @return {@code true} if the archive is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    /**
     * Returns all transactions that took place in the specified week.
     *
     * @param week the week number to filter by
     * @return a list of transactions from the given week
     */
    public List<Transaction> getTransactions(int week) {
        return transactions.stream()
                .filter(transaction -> transaction.getWeek() == week)
                .toList();
    }

    /**
     * Returns all purchase transactions that took place in the specified week.
     *
     * @param week the week number to filter by
     * @return a list of purchases from the given week
     */
    public List<Purchase> getPurchases(int week) {
        return this.getTransactions(week).stream()
                .filter(Purchase.class::isInstance)
                .map(Purchase.class::cast)
                .toList();
    }

    /**
     * Returns all sale transactions that took place in the specified week.
     *
     * @param week the week number to filter by
     * @return a list of sales from the given week
     */
    public List<Sale> getSales(int week) {
        return this.getTransactions(week).stream()
                .filter(Sale.class::isInstance)
                .map(Sale.class::cast)
                .toList();
    }

    /**
     * Counts the number of weeks in which transactions have taken place.
     *
     * @return the number of weeks with transactions
     */
    public int countDistinctWeeks() {
        return (int) transactions.stream().map(Transaction::getWeek).distinct().count();
    }
}
