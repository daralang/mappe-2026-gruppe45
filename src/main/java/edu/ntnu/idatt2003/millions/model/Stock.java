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
     */
    public Stock(String symbol, String company, List<BigDecimal> prices) {
        this.symbol = symbol;       // NOTAT: skal være unikt, dette må sørges for et sted (kom tilbake til)
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
     */
    public void addNewSalesPrice(BigDecimal price) {
        this.prices.add(price);
    }
}
