package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;
import java.util.List;

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
     * @param symbol the stock's trading symbol (e.g., "AAPL")
     * @param company the company name (e.g., "Apple Inc.")
     * @param prices the list of historical prices
     * @throws IllegalArgumentException if the symbol is null or blank
     * @throws IllegalArgumentException if the company is null or blank
     * @throws IllegalArgumentException if the list of prices is null
     * @throws IllegalArgumentException if the list of prices is empty
     */
    public Stock(String symbol, String company, List<BigDecimal> prices) {
        if (symbol == null || symbol.isBlank()) throw new IllegalArgumentException("Symbol cannot be null or blank");
        if (company == null || company.isBlank()) throw new IllegalArgumentException("Company cannot be null or blank");
        if (prices == null) throw new IllegalArgumentException("Prices cannot be null");
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
     * @throws IllegalArgumentException if the price is null
     */
    public void addNewSalesPrice(BigDecimal price) {
        if (price == null) throw new IllegalArgumentException("Price cannot be null");
        if (price.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.prices.add(price);
    }
}
