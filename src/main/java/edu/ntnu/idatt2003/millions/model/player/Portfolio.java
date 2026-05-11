package edu.ntnu.idatt2003.millions.model.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * Represents a portfolio that holds a player's share holdings.
 * A portfolio can contain multiple shares of the same stock purchased at different times.
 */
public class Portfolio {
    private final List<Share> shares;

    /**
     * Constructs a new empty portfolio.
     */
    public Portfolio() {
        shares = new ArrayList<>();
    }

    /**
     * Adds a share to the portfolio.
     *
     * @param share the share to add
     * @return true if the share was added, false if it already exists
     * @throws NullPointerException if the share is null
     */
    public boolean addShare(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        if (!shares.contains(share)) {
            shares.add(share);
            return true;
        }
        return false;
    }

    /**
     * Removes a share from the portfolio.
     *
     * @param share the share to remove
     * @return true if the share was removed, false if it wasn't found
     * @throws NullPointerException if the share is null
     */
    public boolean removeShare(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        return shares.remove(share);
    }

    /**
     * Gets all shares in the portfolio.
     *
     * @return a list of all shares
     */
    public List<Share> getShares() {
        return new ArrayList<>(shares);
    }

    /**
     * Gets all shares of a specific stock by symbol. The method will return
     * an empty list if the symbol is null.
     *
     * @param symbol the stock symbol to filter by
     * @return a list of shares matching the symbol
     * @throws NullPointerException if the symbol is null
     */
    public List<Share> getShares(String symbol) {
        if (symbol == null) return List.of();

        return shares.stream()
                .filter(share -> Objects.equals(share.getStock().getSymbol(), symbol))
                .toList();
    }

    /**
     * Replaces all shares in the portfolio with the given list of shares.
     * Used after deserialization in JsonGameFileHandler to relink shares to the correct
     * stock references from the exchange.
     *
     * @param shares the new list of shares to set
     * @throws NullPointerException if the list or any share in the list is null
     */
    public void setShares(List<Share> shares) {
        Objects.requireNonNull(shares, "Shares cannot be null");
        shares.forEach(share -> Objects.requireNonNull(share, "Share cannot be null"));
        this.shares.clear();
        this.shares.addAll(shares);
    }

    /**
     * Checks if the portfolio contains a specific share.
     *
     * @param share the share to check for
     * @return true if the share exists in the portfolio
     * @throws NullPointerException if the share is null
     */
    public boolean contains(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        return shares.contains(share);
    }

    /**
     * Returns the total market value of the portfolio in NOK.
     *
     * <p>For each share, the current market value ({@link edu.ntnu.idatt2003.millions.model.stock.Share#getCurrentValue()},
     * i.e. {@code salesPrice × quantity}) is converted to NOK via the given
     * {@link CurrencyConverter}. The converted values are summed.
     * Sale commission and tax are NOT deducted — those are transaction-level
     * concerns handled by {@link edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator}
     * at the point of sale.
     *
     * @param converter the currency converter used to translate each share's market value to NOK
     * @return the total market value of all positions in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getNetWorth(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        Currency nok = Currency.getInstance("NOK");
        return shares.stream()
                .map(share -> converter.convert(
                        share.getCurrentValue(), share.getStock().getCurrency(), nok))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total current market value of all positions in the portfolio
     * in each stock's native currency. Only meaningful for single-currency portfolios.
     *
     * @return sum of {@link Share#getCurrentValue()} for all shares
     */
    public BigDecimal getTotalValueNative() {
        return shares.stream()
                .map(Share::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total absolute return across all positions in each stock's
     * native currency. Only meaningful for single-currency portfolios.
     *
     * @return sum of {@link Share#getReturnNative()} for all shares
     */
    public BigDecimal getTotalReturnNative() {
        return shares.stream()
                .map(Share::getReturnNative)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total absolute return across all positions in NOK.
     *
     * @param converter the currency converter used to translate each return to NOK
     * @return total return in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getTotalReturnInNok(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        Currency nok = Currency.getInstance("NOK");
        return shares.stream()
                .map(share -> converter.convert(
                        share.getReturnNative(), share.getStock().getCurrency(), nok))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total return as a percentage of total cost across all positions,
     * with all values converted to NOK before computing the ratio.
     * Returns zero if the total cost basis is zero.
     *
     * @param converter the currency converter used to translate costs and returns to NOK
     * @return total return as a percentage
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getTotalReturnPercent(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        BigDecimal totalCostInNok = getTotalCostInNok(converter);
        if (totalCostInNok.signum() == 0) return BigDecimal.ZERO;
        return getTotalReturnInNok(converter)
                .multiply(BigDecimal.valueOf(100))
                .divide(totalCostInNok, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal getTotalCostInNok(CurrencyConverter converter) {
        Currency nok = Currency.getInstance("NOK");
        return shares.stream()
                .map(share -> converter.convert(
                        share.getCost(), share.getStock().getCurrency(), nok))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}