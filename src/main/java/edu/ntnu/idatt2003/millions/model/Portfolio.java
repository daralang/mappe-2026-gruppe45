package edu.ntnu.idatt2003.millions.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portfolio {
    private final List<Share> shares;

    public Portfolio() {
        shares = new ArrayList<>();
    }

    public boolean addShare(Share share) {
        if (!shares.contains(share)) {
            shares.add(share);
            return true;
        }
        return false;
    }

    public boolean removeShare(Share share) {
        return shares.remove(share);
    }

    public List<Share> getShares() {
        return new ArrayList<>(shares);
    }

    public List<Share> getShares(String symbol) {
        return shares.stream()
                .filter(share -> Objects.equals(share.getStock().getSymbol(), symbol))
                .toList();
    }

    public boolean contains(Share share) {
        return shares.contains(share);
    }
}