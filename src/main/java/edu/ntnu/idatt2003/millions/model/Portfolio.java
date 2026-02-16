package edu.ntnu.idatt2003.millions.model;

import java.util.ArrayList;
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
     */
    public boolean addShare(Share share) {
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
     */
    public boolean removeShare(Share share) {
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
     * Gets all shares of a specific stock by symbol.
     *
     * @param symbol the stock symbol to filter by
     * @return a list of shares matching the symbol
     */
    public List<Share> getShares(String symbol) {
        return shares.stream()
                .filter(share -> Objects.equals(share.getStock().getSymbol(), symbol))
                .toList();
    }

    /**
     * Checks if the portfolio contains a specific share.
     *
     * @param share the share to check for
     * @return true if the share exists in the portfolio
     */
    public boolean contains(Share share) {
        return shares.contains(share);
    }
}