package edu.ntnu.idatt2003.millions;

import java.math.BigDecimal;
import java.util.List;

public class Stock {
    private final String symbol;
    private final String company;
    private final List<BigDecimal> prices;

    public Stock(String symbol, String company, List<BigDecimal> prices) {
        this.symbol = symbol;       // NOTAT: skal være unikt, dette må sørges for et sted (kom tilbake til)
        this.company = company;
        this.prices = prices;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompany() {
        return company;
    }

    public BigDecimal getSalesPrice() {
        return prices.getLast();
    }

    public void addNewSalesPrice(BigDecimal price) {
        this.prices.add(price);
    }
}
