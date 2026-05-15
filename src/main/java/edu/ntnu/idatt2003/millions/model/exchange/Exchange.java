package edu.ntnu.idatt2003.millions.model.exchange;

import edu.ntnu.idatt2003.millions.factory.TransactionFactory;
import edu.ntnu.idatt2003.millions.file.game.JsonGameFileHandler;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.transaction.Sale;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a stock exchange where players can buy and sell shares.
 * The exchange keeps track of all listed stocks and the current week.
 * Prices are updated each week using the advance() method.
 *
 * <p>Buy and sell transactions are denominated in each stock's native currency
 * but converted to NOK via a {@link CurrencyConverter} before the player's
 * balance is adjusted.
 *
 * <p>Sales support both full and partial quantities. When a player sells less
 * than the full position, the original {@link Share} is split into a sold
 * portion (settled as a {@link Sale}) and a remainder that stays in the
 * portfolio with the original purchase price preserved.
 */
public class Exchange {
    private final String name;
    private int week;
    private final Map<String, Stock> stockMap;

    @SuppressWarnings("java:S2065") // transient is needed to prevent Gson from serializing the simulator
    private transient PriceSimulator simulator;

    private static final Currency NOK = Currency.getInstance("NOK");

    @SuppressWarnings("java:S2065") // transient is needed to prevent Gson from serializing the converter
    private transient CurrencyConverter currencyConverter;

    /**
     * Creates a new exchange with a {@link RandomPriceSimulator} as the default pricing model.
     * Delegates to {@link #Exchange(String, List, CurrencyConverter, PriceSimulator)}.
     *
     * @param name              the name of the exchange
     * @param stocks            the stocks that can be traded on this exchange
     * @param currencyConverter the converter used to translate stock-currency amounts to NOK
     * @throws NullPointerException     if name, stocks, currencyConverter, or any stock in the list is null
     * @throws IllegalArgumentException if name is blank, stocks is empty, contains null,
     *                                  or contains duplicate symbols
     */
    public Exchange(String name, List<Stock> stocks, CurrencyConverter currencyConverter) {
        this(name, stocks, currencyConverter, new RandomPriceSimulator());
    }

    /**
     * Creates a new exchange with the given name, list of stocks, currency converter,
     * and price simulator. The stocks are stored in a map using their symbol as the key.
     * Week starts at 1.
     *
     * @param name              the name of the exchange
     * @param stocks            the stocks that can be traded on this exchange
     * @param currencyConverter the converter used to translate stock-currency amounts to NOK
     * @param simulator         the strategy used to compute new stock prices each week
     * @throws NullPointerException     if name, stocks, currencyConverter, simulator,
     *                                  or any stock in the list is null
     * @throws IllegalArgumentException if name is blank, stocks is empty,
     *                                  contains null, or contains duplicate symbols
     */
    public Exchange(String name, List<Stock> stocks, CurrencyConverter currencyConverter,
                    PriceSimulator simulator) {
        Objects.requireNonNull(name, "Exchange name cannot be null");
        Objects.requireNonNull(stocks, "Exchange stocks cannot be null");
        Objects.requireNonNull(currencyConverter, "CurrencyConverter cannot be null");
        Objects.requireNonNull(simulator, "PriceSimulator cannot be null");

        if (name.isBlank()) throw new IllegalArgumentException("Exchange name cannot be blank");
        if (stocks.isEmpty()) throw new IllegalArgumentException("Exchange stocks cannot be empty");

        this.name = name;
        this.week = 1;
        this.stockMap = new HashMap<>();
        this.currencyConverter = currencyConverter;
        this.simulator = simulator;

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
     * Returns the currency converter used by this exchange to translate
     * stock-currency amounts to NOK.
     *
     * @return the currency converter
     */
    public CurrencyConverter getCurrencyConverter() {
        return currencyConverter;
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
     * Return all stocks listed on this exchange
     *
     * @return an unmodifiable list of all stocks.
     */
    public List<Stock> getStocks() {
        return List.copyOf(stockMap.values());
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
     * The search is not case-sensitive. If the search term is null or blank, the method
     * will return an empty list.
     *
     * @param searchTerm the word or phrase to search for
     * @return a list of stocks that match the search term
     * @throws NullPointerException if the search term is null
     * @throws IllegalArgumentException if the search term is blank
     */
    public List<Stock> findStocks(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) return List.of();

        return stockMap.values().stream()
                .filter(stock -> stock.matches(searchTerm))
                .toList();
    }

    /**
     * Buys a given quantity of a stock for a player.
     * The total cost is computed in the stock's native currency, converted to NOK
     * via the active {@link CurrencyConverter}, and passed to {@link TransactionFactory}.
     * The purchase commits the balance withdrawal and is then returned.
     *
     * @param symbol   the symbol of the stock to buy
     * @param quantity how many shares to buy
     * @param player   the player making the purchase
     * @return the completed purchase transaction
     * @throws NullPointerException     if symbol, quantity, or player is null
     * @throws IllegalArgumentException if the symbol is blank, not found,
     *                                  or quantity is not greater than zero
     * @throws IllegalArgumentException if the player does not have enough money
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
        Transaction purchaseWithoutSettlement = TransactionFactory.createPurchase(share, week);
        BigDecimal totalCost = purchaseWithoutSettlement.getCalculator().calculateTotal();
        BigDecimal totalCostInNok = currencyConverter.convert(totalCost, stock.getCurrency(), NOK);

        Transaction purchase = TransactionFactory.createPurchase(share, week, totalCostInNok);
        purchase.commit(player);
        return purchase;
    }

    /**
     * Sells the full quantity of a share for a player. Convenience overload that
     * delegates to {@link #sell(Share, BigDecimal, Player)} with the share's full
     * quantity.
     *
     * @param share  the share to sell
     * @param player the player selling the share
     * @return the completed sale transaction
     * @throws NullPointerException  if share or player is null
     * @throws IllegalStateException if the share is not in the player's portfolio
     */
    public Transaction sell(Share share, Player player) {
        Objects.requireNonNull(share, "Share cannot be null");
        return sell(share, share.getQuantity(), player);
    }

    /**
     * Sells a given quantity of a share for a player. The sold portion is settled
     * as a {@link Sale}; if the requested quantity is less than the full position,
     * the remainder stays in the player's portfolio with the original purchase
     * price preserved so per-share return is unchanged on the remaining position.
     *
     * <p>The net payout is computed in the stock's native currency, converted to
     * NOK via the active {@link CurrencyConverter}, and added to the player's
     * balance.</p>
     *
     * @param share    the share to sell from
     * @param quantity the quantity to sell (must be greater than zero and no more
     *                 than the share's current quantity)
     * @param player   the player selling the share
     * @return the completed sale transaction
     * @throws NullPointerException     if share, quantity or player is null
     * @throws IllegalArgumentException if quantity is not greater than zero or
     *                                  exceeds the share's quantity
     * @throws IllegalStateException    if the share is not in the player's portfolio
     */
    public Transaction sell(Share share, BigDecimal quantity, Player player) {
        Objects.requireNonNull(share, "Share cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        validatePlayer(player);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (quantity.compareTo(share.getQuantity()) > 0) {
            throw new IllegalArgumentException(
                    "Cannot sell more shares than the player owns");
        }
        if (!player.getPortfolio().contains(share)) {
            throw new IllegalStateException("Share is not in portfolio");
        }

        boolean isFullSale = quantity.compareTo(share.getQuantity()) == 0;

        Share soldPortion;
        Share remainder = null;
        if (isFullSale) {
            soldPortion = share;
        } else {
            soldPortion = new Share(share.getStock(), quantity, share.getPurchasePrice());
            remainder = new Share(
                    share.getStock(),
                    share.getQuantity().subtract(quantity),
                    share.getPurchasePrice());
            // Remove original; soldPortion is added first (no other share of this stock
            // in the portfolio now) so sale.commit() can remove it by reference.
            // remainder is added after commit to avoid triggering the GAV merge.
            player.getPortfolio().removeShare(share);
            player.getPortfolio().addShare(soldPortion);
        }

        Sale sale = (Sale) TransactionFactory.createSale(soldPortion, week);
        BigDecimal totalValueInNok = currencyConverter.convert(
                sale.getTotal(), share.getStock().getCurrency(), NOK);
        player.addMoney(totalValueInNok);

        sale.commit(player);

        if (remainder != null) {
            player.getPortfolio().addShare(remainder);
        }
        return sale;
    }

    /**
     * Moves the exchange forward by one week.
     * The week counter is incremented, and each stock gets a new price
     * computed by the injected {@link PriceSimulator}.
     */
    public void advance() {
        week++;
        for (Stock stock : stockMap.values()) {
            stock.addNewSalesPrice(simulator.nextPrice(stock.getSalesPrice()));
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
     * weekly percentage change first.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of top ranked stocks ordered by descending weekly percentage change
     * @throws IllegalArgumentException if limit is not greater than zero
     */
    public List<Stock> getGainers(int limit) {
        if (limit <= 0) throw new IllegalArgumentException("Limit must be greater than 0");
        return gainersStream()
                .sorted((a, b) -> b.getWeeklyChangePercent().compareTo(a.getWeeklyChangePercent()))
                .limit(limit)
                .toList();
    }

    /**
     * Returns the worst performing stocks since last week, sorted by the most negative
     * weekly percentage change first.
     *
     * @param limit the maximum number of stocks to return
     * @return a list of the worst performing stocks ordered by ascending weekly percentage change
     * @throws IllegalArgumentException if limit is not greater than zero
     */
    public List<Stock> getLosers(int limit) {
        if (limit <= 0) throw new IllegalArgumentException("Limit must be greater than 0");
        return losersStream()
                .sorted(Comparator.comparing(Stock::getWeeklyChangePercent))
                .limit(limit)
                .toList();
    }

    /**
     * Returns the number of stocks that had a positive weekly percentage change.
     *
     * @return the count of stocks with a weekly change above zero
     */
    public long countGainers() {
        return gainersStream().count();
    }

    /**
     * Returns the number of stocks that had a negative weekly percentage change.
     *
     * @return the count of stocks with a weekly change below zero
     */
    public long countLosers() {
        return losersStream().count();
    }

    /**
     * Returns a stream of stocks with a positive weekly percentage change.
     *
     * @return a stream of gaining stocks
     */
    private Stream<Stock> gainersStream() {
        return stockMap.values().stream()
                .filter(stock -> stock.getWeeklyChangePercent().compareTo(BigDecimal.ZERO) > 0);
    }

    /**
     * Returns a stream of stocks with a negative weekly percentage change.
     *
     * @return a stream of losing stocks
     */
    private Stream<Stock> losersStream() {
        return stockMap.values().stream()
                .filter(stock -> stock.getWeeklyChangePercent().compareTo(BigDecimal.ZERO) < 0);
    }

    /**
     * Reinitializes transient fields after deserialization.
     * Must be called by {@link JsonGameFileHandler} after loading a game from file,
     * since Gson does not invoke constructors and transient fields are not restored.
     *
     * @param currencyConverter the converter to use for the loaded game session
     * @throws NullPointerException if currencyConverter is null
     */
    public void reinitialize(CurrencyConverter currencyConverter) {
        Objects.requireNonNull(currencyConverter, "CurrencyConverter cannot be null");
        this.currencyConverter = currencyConverter;
        this.simulator = new RandomPriceSimulator();
    }
}