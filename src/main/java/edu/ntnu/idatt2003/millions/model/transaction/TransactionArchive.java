// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.transaction;

import java.math.BigDecimal;
import java.util.*;

/**
 * Represents an archive of transactions.
 * Provides retrieval by week, by type ({@link Purchase} or {@link Sale}), and counting of distinct weeks of activity.
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
     * @throws NullPointerException if the transaction is null
     */
    public boolean add(Transaction transaction) {
        Objects.requireNonNull(transaction, "Transaction cannot be null");
        if (!transactions.contains(transaction)) {
            transactions.add(transaction);
            return true;
        }
        return false;
    }

    /**
     * Returns whether the archive contains no transactions.
     *
     * @return {@code true} if the archive is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    /**
     * Returns all transactions in the archive regardless of week.
     *
     * @return a defensive copy of all transactions
     */
    public List<Transaction> getAll() {
        return new ArrayList<>(transactions);
    }

    /**
     * Returns all transactions that took place in the specified week.
     *
     * @param week the week number to filter by
     * @return a list of transactions from the given week
     * @throws IllegalArgumentException if the week is less than 1
     */
    public List<Transaction> getTransactions(int week) {
        if (week < 1) throw new IllegalArgumentException("Week must be at least 1");
        return transactions.stream()
                .filter(transaction -> transaction.getWeek() == week)
                .toList();
    }

    /**
     * Returns all transactions in the given inclusive week range as a fresh mutable list,
     * in encounter order (within each week: insertion order; across weeks: low-to-high).
     * The caller is responsible for any additional sorting.
     *
     * @param fromWeek the first week to include (inclusive); must be at least 1
     * @param toWeek   the last week to include (inclusive); if less than fromWeek the
     *                 returned list is empty
     * @return a mutable list of transactions in the range; never null
     * @throws IllegalArgumentException if fromWeek is less than 1
     */
    public List<Transaction> getTransactionsInRange(int fromWeek, int toWeek) {
        if (fromWeek < 1) throw new IllegalArgumentException("fromWeek must be at least 1");
        return java.util.stream.IntStream.rangeClosed(fromWeek, toWeek)
                .boxed()
                .flatMap(w -> getTransactions(w).stream())
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    /**
     * Counts all purchase transactions in the given inclusive week range.
     *
     * @param fromWeek the first week to include (inclusive); must be at least 1
     * @param toWeek   the last week to include (inclusive); if less than fromWeek returns 0
     * @return the number of purchases in the range
     * @throws IllegalArgumentException if fromWeek is less than 1
     */
    public int countPurchasesInRange(int fromWeek, int toWeek) {
        if (fromWeek < 1) throw new IllegalArgumentException("fromWeek must be at least 1");
        return java.util.stream.IntStream.rangeClosed(fromWeek, toWeek)
                .map(w -> getPurchases(w).size())
                .sum();
    }

    /**
     * Counts all sale transactions in the given inclusive week range.
     *
     * @param fromWeek the first week to include (inclusive); must be at least 1
     * @param toWeek   the last week to include (inclusive); if less than fromWeek returns 0
     * @return the number of sales in the range
     * @throws IllegalArgumentException if fromWeek is less than 1
     */
    public int countSalesInRange(int fromWeek, int toWeek) {
        if (fromWeek < 1) throw new IllegalArgumentException("fromWeek must be at least 1");
        return java.util.stream.IntStream.rangeClosed(fromWeek, toWeek)
                .map(w -> getSales(w).size())
                .sum();
    }

    /**
     * Returns all purchase transactions that took place in the specified week.
     *
     * @param week the week number to filter by
     * @return a list of purchases from the given week
     * @throws IllegalArgumentException if the week is less than 1
     */
    public List<Purchase> getPurchases(int week) {
        if (week < 1) throw new IllegalArgumentException("Week must be at least 1");
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
     * @throws IllegalArgumentException if the week is less than 1
     */
    public List<Sale> getSales(int week) {
        if (week < 1) throw new IllegalArgumentException("Week must be at least 1");
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

    /**
     * Returns the total realized gain from all profitable sales, grouped by the
     * stock's native currency. Each entry maps a currency to the sum of profits
     * from sales in that currency. Sales with zero or negative profit are excluded.
     *
     * <p>The caller is responsible for converting to a display currency. This
     * separation keeps the domain free of currency-conversion concerns.</p>
     *
     * @return profits per currency; empty map if no profitable sales exist
     */
    public Map<Currency, BigDecimal> getRealizedGainsByCurrency() {
        return sumSalesByCurrency(profit -> profit.signum() > 0, profit -> profit);
    }

    /**
     * Returns the total realized loss from all losing sales, grouped by the stock's
     * native currency. Each entry maps a currency to the sum of absolute losses
     * (positive numbers) in that currency. Sales with zero or positive profit are
     * excluded.
     *
     * @return absolute losses per currency; empty map if no losing sales exist
     */
    public Map<Currency, BigDecimal> getRealizedLossesByCurrency() {
        return sumSalesByCurrency(profit -> profit.signum() < 0, BigDecimal::abs);
    }

    /**
     * Returns the total commission paid across all sales, grouped by the stock's
     * native currency.
     *
     * @return commission totals per currency; empty map if no sales exist
     */
    public Map<Currency, BigDecimal> getTotalSaleCommissionByCurrency() {
        return sumSalesAttribute(Sale::getCommission);
    }

    /**
     * Returns the total tax paid across all sales, grouped by the stock's native
     * currency.
     *
     * @return tax totals per currency; empty map if no sales exist
     */
    public Map<Currency, BigDecimal> getTotalSaleTaxByCurrency() {
        return sumSalesAttribute(Sale::getTax);
    }

    /**
     * Returns all sale transactions in the archive regardless of week.
     *
     * @return an unmodifiable list of all sales
     */
    public List<Sale> getAllSales() {
        return transactions.stream()
                .filter(Sale.class::isInstance)
                .map(Sale.class::cast)
                .toList();
    }

    /**
     * Returns the total number of completed sales in the archive.
     *
     * @return the number of sales
     */
    public int getSalesCount() {
        return (int) transactions.stream()
                .filter(Sale.class::isInstance)
                .count();
    }

    private Map<Currency, BigDecimal> sumSalesByCurrency(
            java.util.function.Predicate<BigDecimal> filter,
            java.util.function.UnaryOperator<BigDecimal> mapper) {
        Map<Currency, BigDecimal> result = new HashMap<>();
        for (Sale sale : getAllSales()) {
            BigDecimal profit = sale.getProfit();
            if (!filter.test(profit)) continue;
            Currency currency = sale.getShare().getStock().getCurrency();
            result.merge(currency, mapper.apply(profit), BigDecimal::add);
        }
        return result;
    }

    private Map<Currency, BigDecimal> sumSalesAttribute(
            java.util.function.Function<Sale, BigDecimal> extractor) {
        return getAllSales().stream()
                .collect(java.util.stream.Collectors.toMap(
                        sale -> sale.getShare().getStock().getCurrency(),
                        extractor,
                        BigDecimal::add));
    }
}
