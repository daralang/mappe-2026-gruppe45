package edu.ntnu.idatt2003.millions.model.exchange;

import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Represents a stock exchange where players can buy and sell shares.
 * The exchange keeps track of all listed stocks and the current week.
 * Prices are updated each week using the advance() method.
 */
public class Exchange {
    private final String name;
    private int week;
    private final Map<String, Stock> stockMap;

    @SuppressWarnings("java:S2065") // transient is needed to prevent Gson from serializing Random
    private final transient Random random = new Random();

    /** The lowest price a stock can have after a weekly update. */
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");

    /** The maximum amount a stock price can change per week (10%). */
    private static final BigDecimal MAX_WEEKLY_CHANGE = new BigDecimal("0.10");

    /**
     * Creates a new exchange with the given name and a list of stocks.
     * The stocks are stored in a map using their symbol as the key.
     * Week starts at 1.
     *
     * @param name the name of the exchange
     * @param stocks the stocks that can be traded on this exchange
     * @throws NullPointerException if the name, stocks, or any stock in the list is null
     * @throws IllegalArgumentException if name is blank, stocks is empty,
     *                                  contains null, or contains duplicate symbols
     */
    public Exchange(String name, List<Stock> stocks) {
        Objects.requireNonNull(name, "Exchange name cannot be null");
        Objects.requireNonNull(stocks, "Exchange stocks cannot be null");

        if (name.isBlank()) throw new IllegalArgumentException("Exchange name cannot be blank");
        if (stocks.isEmpty()) throw new IllegalArgumentException("Exchange stocks cannot be empty");

        this.name = name;
        this.week = 1;
        this.stockMap = new HashMap<>();

        for (Stock stock : stocks) {
            Objects.requireNonNull(stock, "Stock list cannot contain null");
            if (stockMap.containsKey(stock.getSymbol())) {
                throw new IllegalArgumentException("Stock already exists in exchange: " + stock.getSymbol());
            }
            stockMap.put(stock.getSymbol(), stock);
        }
    }

    /**
     * Returns the name of the exchange.
     *
     * @return the exchange name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current trading week.
     *
     * @return the current week number
     */
    public int getWeek() {
        return week;
    }

    /**
     * Returns the stock with the given symbol.
     *
     * @param symbol the stock symbol to look up
     * @return the stock matching the symbol
     * @throws NullPointerException if the symbol is null
     * @throws IllegalArgumentException if the symbol is blank or not found
     */
    public Stock getStock(String symbol) {
        validateSymbol(symbol);
        Stock stock = stockMap.get(symbol);
        if (stock == null) throw new IllegalArgumentException("Exchange does not contain stock: " + symbol);
        return stock;
    }

    /**
     * Checks if a stock with the given symbol is listed on the exchange.
     * Returns false instead of throwing an exception if the symbol is null or blank,
     * since this method is meant to be used as a safe check before calling getStock().
     *
     * @param symbol the stock symbol to check
     * @return true if the stock is listed, false otherwise
     */
    public boolean hasStock(String symbol) {
        if (symbol == null || symbol.isBlank()) return false;

        return stockMap.containsKey(symbol);
    }

    /**
     * Searches for stocks where the symbol or company name contains the search term.
     * The search is not case-sensitive.
     *
     * @param searchTerm the word or phrase to search for
     * @return a list of stocks that match the search term
     * @throws NullPointerException if the search term is null
     * @throws IllegalArgumentException if the search term is blank
     */
    public List<Stock> findStocks(String searchTerm) {
        Objects.requireNonNull(searchTerm, "Search term cannot be null");
        if (searchTerm.isBlank()) throw new IllegalArgumentException("Search term cannot be blank");

        String normalized = searchTerm.toLowerCase();
        return stockMap.values().stream()
                .filter(stock ->
                        stock.getSymbol().toLowerCase().contains(normalized)
                                || stock.getCompany().toLowerCase().contains(normalized))
                .toList();
    }

    /**
     * Buys a given quantity of a stock for a player.
     * A new share is created at the current sales price, and the transaction
     * is committed right away. The transaction is then returned.
     *
     * @param symbol the symbol of the stock to buy
     * @param quantity how many shares to buy
     * @param player the player making the purchase
     * @return the completed purchase transaction
     * @throws NullPointerException if symbol, quantity, or player is null
     * @throws IllegalArgumentException if the symbol is blank, not found,
     *                                  or quantity is not greater than zero
     * @throws IllegalStateException if the player does not have enough money
     */
    public Transaction buy(String symbol, BigDecimal quantity, Player player) {
        validateSymbol(symbol);
        validatePlayer(player);
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Stock stock = getStock(symbol);
        Share share = new Share(stock, quantity, stock.getSalesPrice());
        Purchase purchase = new Purchase(share, week);
        purchase.commit(player);
        return purchase;
    }

    /**
     * Sells a share for a player.
     * A sale transaction is created and committed, then returned.
     *
     * @param share the share to sell
     * @param player the player selling the share
     * @return the completed sale transaction
     * @throws NullPointerException if share or player is null
     * @throws IllegalStateException if the share is not in the player's portfolio
     */
    public Transaction sell(Share share, Player player) {
        Objects.requireNonNull(share, "Share cannot be null");
        validatePlayer(player);

        Sale sale = new Sale(share, week);
        sale.commit(player);
        return sale;
    }

    /**
     * Moves the exchange forward by one week.
     * The week counter is incremented, and each stock gets a new price
     * based on a random change of up to ±10%. No stock can go below MIN_PRICE.
     */
    public void advance() {
        week++;
        for (Stock stock : stockMap.values()) {
            BigDecimal currentPrice = stock.getSalesPrice();

            double randomFraction = (random.nextDouble() * 2.0) - 1.0;
            BigDecimal percentChange = MAX_WEEKLY_CHANGE.multiply(BigDecimal.valueOf(randomFraction));

            BigDecimal newPrice = currentPrice
                    .multiply(BigDecimal.ONE.add(percentChange))
                    .setScale(2, RoundingMode.HALF_UP);

            if (newPrice.compareTo(MIN_PRICE) < 0) {
                newPrice = MIN_PRICE;
            }

            stock.addNewSalesPrice(newPrice);
        }
    }

    //--- Private validation helpers ---

    /**
     * Checks that the given symbol is not null or blank.
     *
     * @param symbol the symbol to validate
     * @throws NullPointerException if the symbol is null
     * @throws IllegalArgumentException if the symbol is blank
     */
    private void validateSymbol(String symbol) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }
    }

    /**
     * Checks that the given player is not null.
     *
     * @param player the player to validate
     * @throws NullPointerException if the player is null
     */
    private void validatePlayer(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
    }

    /**
     * Returns the top ranking stocks since last week, sorted by the highest positive
     * price change first.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of top ranked stocks
     * @throws IllegalArgumentException if limit is not greater than zero.
     */
    public List<Stock> getGainers(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        return stockMap.values().stream()
                .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b)-> b.getLatestPriceChange().compareTo(a.getLatestPriceChange()))
                .limit(limit)
                .toList();
    }

    /**
     * Returns the worst performing stocks since last week, sorted by the most negative
     * price change first.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of the worst performing stocks, capped at limit
     * @throws IllegalArgumentException if limit is not greater than zero
     */
    public List<Stock> getLosers(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        return stockMap.values().stream()
                .filter(stock -> stock.getLatestPriceChange().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(Stock::getLatestPriceChange))
                .limit(limit)
                .toList();
    }

}