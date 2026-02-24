package edu.ntnu.idatt2003.millions.model;

public class Sale extends Transaction {

    public Sale(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
    }

    @Override
    public void commit(Player player) {
        // TO BE IMPLEMENTED
    }
}
