// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.transaction;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.stock.Share;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a purchase transaction for a given share.
 * When committed, withdraws the total cost from the player's balance in NOK
 * and adds the share to the player's portfolio.
 */
public class Purchase extends Transaction {
    private final BigDecimal settlementAmount;

    /**
     * Constructs a new Purchase for the specified share and week.
     * The settlement amount defaults to the total cost of the acquired share.
     *
     * @param share the share being purchased
     * @param week  the week in which the purchase takes place
     */
    public Purchase(Share share, int week) {
        super(share, week, new PurchaseCalculator(share));
        this.settlementAmount = getCalculator().calculateTotal();
    }

    /**
     * Constructs a new Purchase for the specified share, week, and settlement amount.
     *
     * <p>The settlement amount represents the amount that will be withdrawn from
     * the {@link Player} when {@link #commit(Player)} is called.</p>
     *
     * @param share            the share being purchased
     * @param week             the week in which the purchase takes place
     * @param settlementAmount the amount to withdraw from the player during commit
     * @throws NullPointerException     if the settlement amount is null
     * @throws IllegalArgumentException if the settlement amount is negative
     */
    @SuppressWarnings("java:S8433") // validation after super() is safe; super has no side effects beyond field assignment
    public Purchase(Share share, int week, BigDecimal settlementAmount) {
        super(share, week, new PurchaseCalculator(share));
        Objects.requireNonNull(settlementAmount, "Settlement amount cannot be null");
        if (settlementAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Settlement amount cannot be negative");
        }
        this.settlementAmount = settlementAmount;
    }

    @Override
    public BigDecimal getCommissionNative() {
        return new PurchaseCalculator(getShare()).calculateCommission();
    }

    /** Purchases are not taxed; always returns zero. */
    @Override
    public BigDecimal getTaxNative() {
        return BigDecimal.ZERO;
    }

    /** Returns the total cost as a negative value — a cash outflow from the buyer's perspective. */
    @Override
    public BigDecimal getSignedTotalNative() {
        return new PurchaseCalculator(getShare()).calculateTotal().negate();
    }

    @Override
    public BigDecimal getPricePerShare() {
        return getShare().getPurchasePrice();
    }

    /**
     * Commits this purchase for the given player.
     * Withdraws the settlement amount from the player's balance, adds the share
     * to the player's portfolio, and records the transaction in the archive.
     *
     * @param player the player executing the purchase
     * @throws NullPointerException  if the player is null
     * @throws IllegalArgumentException if the player has insufficient funds
     * @throws IllegalStateException if the transaction has already been committed
     */
    @Override
    public void commit(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        if (isCommitted()) throw new IllegalStateException("Transaction is already committed");

        player.withdrawMoney(settlementAmount);
        player.getPortfolio().addShare(getShare());
        player.getTransactionArchive().add(this);

        markAsCommitted();
    }
}
