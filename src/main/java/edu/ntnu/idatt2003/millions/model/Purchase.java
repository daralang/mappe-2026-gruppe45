package edu.ntnu.idatt2003.millions.model;

public class Purchase extends Transaction {

    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    @Override
    public void commit(Player player) {
        // TO BE IMPLEMENTED
    }
}