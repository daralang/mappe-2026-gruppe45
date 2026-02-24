package edu.ntnu.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.List;

public class TransactionArchive {
    private final List<Transaction> transactions;

    public TransactionArchive() {
        transactions = new ArrayList<>();
    }

    public boolean add(Transaction transaction) {
        if (!transactions.contains(transaction)) {
            transactions.add(transaction);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public List<Transaction> getTransactions(int week) {
        return transactions.stream()
                .filter(transaction -> transaction.getWeek() == week)
                .toList();
    }

    public List<Purchase> getPurchases(int week) {
        return this.getTransactions(week).stream()
                .filter(Purchase.class::isInstance)
                .map(Purchase.class::cast)
                .toList();
    }

    public List<Sale> getSales(int week) {
        return this.getTransactions(week).stream()
                .filter(Sale.class::isInstance)
                .map(Sale.class::cast)
                .toList();
    }

    public int countDistinctWeeks() {
        return (int) transactions.stream().map(Transaction::getWeek).distinct().count();
    }
}
