// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a portfolio that holds a player's share holdings.
 * At most one {@link Share} per stock symbol is held at any time: adding a share
 * for a stock that is already in the portfolio merges the two positions using a
 * weighted-average purchase price (GAV = total cost / total quantity).
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
     * <p>If a share for the same stock symbol already exists, the two positions are
     * consolidated into one with a weighted-average purchase price
     * (GAV = total cost / total quantity). The merged Share replaces the existing one.
     *
     * <p>If the exact same object reference is already in the portfolio, no change is
     * made and {@code false} is returned.
     *
     * @param share the share to add
     * @return true if the share was added or merged into an existing position,
     *         false if the same object reference already existed in the portfolio
     * @throws NullPointerException if the share is null
     */
    public boolean addShare(Share share) {
        Objects.requireNonNull(share, "Share cannot be null");
        if (shares.contains(share)) {
            return false;
        }
        String symbol = share.getStock().getSymbol();
        for (int i = 0; i < shares.size(); i++) {
            if (shares.get(i).getStock().getSymbol().equals(symbol)) {
                shares.set(i, shares.get(i).mergedWith(share));
                return true;
            }
        }
        shares.add(share);
        return true;
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
     * Returns a defensive copy of all share positions in the portfolio.
     *
     * @return mutable copy of all shares; empty if the portfolio has no holdings
     */
    public List<Share> getShares() {
        return new ArrayList<>(shares);
    }

    /**
     * Returns all shares matching the given stock symbol.
     *
     * @param symbol the stock symbol to filter by; {@code null} returns an empty list
     * @return mutable defensive copy of matching shares; empty if {@code symbol} is null or no shares match
     */
    public List<Share> getShares(String symbol) {
        if (symbol == null) return List.of();

        return shares.stream()
                .filter(share -> Objects.equals(share.getStock().getSymbol(), symbol))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Replaces all current share positions with the given list.
     *
     * @param shares the new list of shares; must not be null, and must contain no null elements
     * @throws NullPointerException if the list or any element in the list is null
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
     * Returns the gross market value of all positions in the portfolio, converted to NOK.
     * Sale commission and tax are not deducted.
     *
     * @param converter the currency converter used to translate each position's market value to NOK
     * @return the total gross market value of all positions in NOK
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