// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.stock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock that can be traded on an exchange.
 * A stock has a trading symbol, company name, and price history.
 * Stocks are traded through purchases and sales, with players acquiring shares in them.
 */
// Not a record: addNewSalesPrice() mutates the internal price list — fundamental mutable state.
@SuppressWarnings("ClassCanBeRecord")
public class Stock {
    private final String symbol;
    private final String company;
    private final List<BigDecimal> prices;

    private final Currency currency;

    /**
     * Constructs a new Stock with the specified symbol, company name, and price history.
     *
     * @param symbol  the stock's trading symbol (e.g., "AAPL")
     * @param company the company name (e.g., "Apple Inc.")
     * @param prices  the list of historical prices
     * @param currency the currency the stock is traded in (e.g., USD)
     * @throws NullPointerException     if the symbol, company, prices or currency is null
     * @throws IllegalArgumentException if the symbol is blank
     * @throws IllegalArgumentException if the company is blank
     * @throws IllegalArgumentException if the list of prices is empty
     */
    public Stock(String symbol, String company, List<BigDecimal> prices, Currency currency) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(company, "Company cannot be null");
        Objects.requireNonNull(prices, "Prices cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol cannot be blank");
        }
        if (company.isBlank()) {
            throw new IllegalArgumentException("Company cannot be blank");
        }
        if (prices.isEmpty()) {
            throw new IllegalArgumentException("Prices cannot be empty");
        }

        this.symbol = symbol;
        this.company = company;
        this.prices = prices;
        this.currency = currency;
    }


    /**
     * Constructs a new Stock with the specified symbol, company name, and price history.
     * Defaults to USD as the currency.
     *
     * @param symbol  the stock's trading symbol (e.g., "AAPL")
     * @param company the company name (e.g., "Apple Inc.")
     * @param prices  the list of historical prices
     * @throws NullPointerException     if the symbol, company, or prices is null
     * @throws IllegalArgumentException if the symbol is blank
     * @throws IllegalArgumentException if the company is blank
     * @throws IllegalArgumentException if the list of prices is empty
     */
    public Stock(String symbol, String company, List<BigDecimal> prices) {
        this(symbol, company, prices, Currency.getInstance("USD"));
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompany() {
        return company;
    }

    /**
     * Returns the current sales price (most recent price in history).
     *
     * @return the latest price
     */
    public BigDecimal getSalesPrice() {
        return prices.getLast();
    }

    public Currency getCurrency() {
        return currency;
    }

    /**
     * Adds a new sales price to the price history.
     *
     * @param price the new price to add
     * @throws NullPointerException     if the price is null
     * @throws IllegalArgumentException if the price is negative
     */
    public void addNewSalesPrice(BigDecimal price) {
        Objects.requireNonNull(price, "Price cannot be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        this.prices.add(price);
    }

    /**
     * Returns a mutable defensive copy of all historical prices, from the initial price to the most recent.
     *
     * @return mutable copy of all recorded prices, oldest first
     */
    public List<BigDecimal> getHistoricalPrices() {
        return new ArrayList<>(prices);
    }

    /**
     * Returns the highest price registered for this stock.
     *
     * @return the highest recorded price
     */
    public BigDecimal getHighestPrice() {
        return prices.stream()
                .max(BigDecimal::compareTo)
                .orElseThrow();
    }

    /**
     * Returns the lowest price recorded for this stock.
     *
     * @return the lowest recorded price
     */
    public BigDecimal getLowestPrice() {
        return prices.stream()
                .min(BigDecimal::compareTo)
                .orElseThrow();
    }

    /**
     * Returns the price change between the two most recent registered prices.
     * Returns zero if only one price has been registered.
     *
     * @return the difference between the latest and second-to-latest price or zero
     * if only one price exists.
     */
    public BigDecimal getLatestPriceChange() {
        if (prices.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal latest = prices.getLast();
        BigDecimal previous = prices.get(prices.size() - 2);
        return latest.subtract(previous);
    }

    /**
     * Returns whether this stock matches the given search term.
     * Checks for a case-insensitive substring match against the symbol or company name.
     * Returns {@code false} for null or blank terms.
     *
     * @param term the search term to test against
     * @return {@code true} if the symbol or company name contains the term
     */
    public boolean matches(String term) {
        if (term == null || term.isBlank()) return false;
        String normalized = term.toLowerCase();
        return symbol.toLowerCase().contains(normalized)
                || company.toLowerCase().contains(normalized);
    }

    /**
     * Returns the most recent {@code weeks} prices from the price history.
     * If fewer entries exist than requested, all prices are returned.
     *
     * @param weeks the maximum number of recent prices to return
     * @return mutable defensive copy of the most recent prices, oldest first
     * @throws IllegalArgumentException if weeks is not greater than zero
     */
    public List<BigDecimal> getRecentPrices(int weeks) {
        if (weeks <= 0) throw new IllegalArgumentException("weeks must be greater than zero");
        return new ArrayList<>(prices.subList(Math.max(0, prices.size() - weeks), prices.size()));
    }

    /**
     * Returns the lowest price among the most recent {@code weeks} prices.
     *
     * @param weeks the number of recent weeks to consider
     * @return the lowest price in the recent window
     * @throws IllegalArgumentException if {@code weeks} is not greater than zero
     */
    public BigDecimal getRecentLow(int weeks) {
        return getRecentPrices(weeks).stream().min(BigDecimal::compareTo).orElseThrow();
    }

    /**
     * Returns the highest price among the most recent {@code weeks} prices.
     *
     * @param weeks the number of recent weeks to consider
     * @return the highest price in the recent window
     * @throws IllegalArgumentException if {@code weeks} is not greater than zero
     */
    public BigDecimal getRecentHigh(int weeks) {
        return getRecentPrices(weeks).stream().max(BigDecimal::compareTo).orElseThrow();
    }

    /**
     * Returns the price change this week as a percentage of the previous price.
     * Returns zero if there is no previous price to compare against.
     *
     * @return the weekly change as a percentage, e.g. 3.50 means +3.50%
     */
    public BigDecimal getWeeklyChangePercent() {
        BigDecimal change = getLatestPriceChange();
        BigDecimal previous = getSalesPrice().subtract(change);
        if (previous.signum() == 0) return BigDecimal.ZERO;
        return change.multiply(BigDecimal.valueOf(100))
                .divide(previous, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Returns the week-over-week price change as a decimal fraction.
     * Returns zero if there is no previous price to compare against.
     *
     * @return e.g. 0.12 for +12%, -0.10 for -10%
     */
    public BigDecimal getLatestPercentChange() {
        BigDecimal change = getLatestPriceChange();
        BigDecimal previous = getSalesPrice().subtract(change);
        if (previous.signum() == 0) return BigDecimal.ZERO;
        return change.divide(previous, 4, java.math.RoundingMode.HALF_UP);
    }
}