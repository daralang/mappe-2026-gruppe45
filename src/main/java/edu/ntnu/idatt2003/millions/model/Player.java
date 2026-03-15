package edu.ntnu.idatt2003.millions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a player in the game.
 * A player has a name, a starting balance, and owns a {@link Portfolio} of shares
 * and a {@link TransactionArchive} of committed transactions.
 */
public class Player {
    private final String name;
    private final BigDecimal startingMoney;
    private BigDecimal money;

    private final Portfolio portfolio;
    private final TransactionArchive transactionArchive;

    /**
     * Constructs a new Player with the specified name and starting balance.
     *
     * @param name          the player's name
     * @param startingMoney the amount of money the player starts with
     * @throws NullPointerException     if the name or starting balance is null
     * @throws IllegalArgumentException if the name is blank or the starting balance is negative
     */
    public Player(String name, BigDecimal startingMoney) {
        Objects.requireNonNull(name, "Player name cannot be null");
        Objects.requireNonNull(startingMoney, "Starting money cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Player name cannot be blank");
        if (startingMoney.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Starting money cannot be negative");

        this.name = name;
        this.startingMoney = startingMoney;
        this.money = startingMoney;

        this.portfolio = new Portfolio();
        this.transactionArchive = new TransactionArchive();
    }

    /**
     * Gets the player's name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the amount of money the player started with.
     *
     * @return the starting balance
     */
    public BigDecimal getStartingMoney() {
        return startingMoney;
    }

    /**
     * Gets the player's current balance.
     *
     * @return the current balance
     */
    public BigDecimal getMoney() {
        return money;
    }

    /**
     * Gets the player's portfolio of shares.
     *
     * @return the portfolio
     */
    public Portfolio getPortfolio() {
        return portfolio;
    }

    /**
     * Gets the player's transaction archive.
     *
     * @return the transaction archive
     */
    public TransactionArchive getTransactionArchive() {
        return transactionArchive;
    }

    /**
     * Adds the specified amount to the player's balance.
     *
     * @param amount the amount to add
     * @throws IllegalArgumentException if the amount is negative
     */
    public void addMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative");
        this.money = this.money.add(amount);
    }

    /**
     * Withdraws the specified amount from the player's balance.
     *
     * @param amount the amount to withdraw
     * @throws IllegalArgumentException if the amount is negative or the player does not have sufficient funds
     */
    public void withdrawMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative");
        if (this.money.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Cannot withdraw more money than the current balance");
        }
        this.money = this.money.subtract(amount);
    }
}
