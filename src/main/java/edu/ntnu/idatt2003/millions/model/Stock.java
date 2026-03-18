package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stock that can be traded on an exchange.
 * A stock has a trading symbol, company name, and price history.
 * Stocks are traded through purchases and sales, with players acquiring shares in them.
 */
public class Stock {
    private final String symbol;
    private final String company;
    private final List<BigDecimal> prices;

    /**
     * Constructs a new Stock with the specified symbol, company name, and price history.
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
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        Objects.requireNonNull(company, "Company cannot be null");
        Objects.requireNonNull(prices, "Prices cannot be null");
        if (symbol.isBlank()) throw new IllegalArgumentException("Symbol cannot be blank");
        if (company.isBlank()) throw new IllegalArgumentException("Company cannot be blank");
        if (prices.isEmpty()) throw new IllegalArgumentException("Prices cannot be empty");

        this.symbol = symbol;
        this.company = company;
        this.prices = prices;
    }

    /**
     * Gets the stock's trading symbol.
     *
     * @return the trading symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the company name.
     *
     * @return the company name
     */
    public String getCompany() {
        return company;
    }

    /**
     * Gets the current sales price (most recent price in history).
     *
     * @return the latest price
     */

    public BigDecimal getSalesPrice() {
        return prices.getLast();
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
        if (price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.prices.add(price);
    }

    /**
     * Returns a list of all registered prices for this Stock.
     * The list includes every price that has ever been recorded from the initial price to the most recent.
     *
     * @return an unmodifiable list of all historical prices.
     */
    public List<BigDecimal> getHistoricalPrices() {
          return Collections.unmodifiableList(prices);
    }

  /**
   * Returns the highest price registered for this stock
   *
   * @return the highest recorded price
   */
    public BigDecimal getHighestPrice() {
      return prices.stream()
          .max(BigDecimal::compareTo)
          .orElseThrow();
    }

  /**
   * Returns the lowest price recorded for this stock
   *
   * @return the lowest price recorded
   */
  public BigDecimal getLowestPrice() {
      return prices.stream()
          .min(BigDecimal::compareTo)
          .orElseThrow();
    }

  /**
   * Returns the price change between the two most recent registered prices.
   * If only one price has been registered will this method interpreted as no change and
   * {@link BigDecimal#ZERO} is returned.
   *
   * @return the difference between the latest and second-to-latest price or zero
   *         if only one price exists.
   */
  public BigDecimal getLatestPriceChange() {
      if (prices.size()< 2) {
        return BigDecimal.ZERO;
      }

      BigDecimal latest = prices.getLast();
      BigDecimal previous = prices.get(prices.size()-2);
      return latest.subtract(previous);
    }
}