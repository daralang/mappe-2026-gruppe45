package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a sale transaction for a given share.
 * A sale records the share being sold and the week it was sold,
 * and uses a {@link SalesCalculator} to process the transaction.
 */
public class Sale extends Transaction {

    /**
     * Constructs a new Sale for the specified share and week.
     * The sales price is fetched directly from the stock and may be in a foreign currency.
     * Use {@link #Sale(Share, int, BigDecimal)} when the price has already been converted to NOK.
     *
     * @param share the share being sold
     * @param week  the week in which the sale takes place
     */
    public Sale(Share share, int week) {
        super(share, week, new SalesCalculator(share));
    }

    /**
     * Constructs a new Sale for the specified share, week and sales price in NOK.
     * Use this constructor when selling via {@link edu.ntnu.idatt2003.millions.model.exchange.Exchange},
     * which converts the stock price to NOK before creating the transaction.
     *
     * @param share          the share being sold
     * @param week           the week in which the sale takes place
     * @param salesPriceInNok the current sales price converted to NOK
     */
    public Sale(Share share, int week, BigDecimal salesPriceInNok) {
        super(share, week, new SalesCalculator(share, salesPriceInNok));
    }

    /**
     * Commits this sale for the given player.
     * Adds the total payout to the player's balance, removes the share
     * from the player's portfolio, and records the transaction in the archive.
     *
     * @param player the player executing the sale
     * @throws NullPointerException  if the player is null
     * @throws IllegalStateException if the transaction has already been committed
     * @throws IllegalStateException if the share is not in the player's portfolio
     */
    @Override
    public void commit(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        if (isCommitted()) throw new IllegalStateException("Sale is already committed");
        if (!player.getPortfolio().contains(getShare())) throw new IllegalStateException("Sale is not in portfolio");

        BigDecimal totalValue = getCalculator().calculateTotal();
        player.addMoney(totalValue);
        player.getPortfolio().removeShare(getShare());
        player.getTransactionArchive().add(this);
        markAsCommitted();
    }
}