package edu.ntnu.idatt2003.millions.model.player;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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

    private BigDecimal previousNetWorth;
    private List<BigDecimal> netWorthHistory;

    /**
     * Constructs a new Player with the specified name and starting balance.
     *
     * @param name          the player's name
     * @param startingMoney the amount of money the player starts with
     * @throws NullPointerException     if the name or starting balance is null
     * @throws IllegalArgumentException if the name is blank or the starting balance
     *                                  is not strictly greater than zero
     */
    public Player(String name, BigDecimal startingMoney) {
        Objects.requireNonNull(name, "Player name cannot be null");
        Objects.requireNonNull(startingMoney, "Starting money cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Player name cannot be blank");
        if (startingMoney.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Starting money must be greater than zero");

        this.name = name;
        this.startingMoney = startingMoney;
        this.money = startingMoney;

        this.portfolio = new Portfolio();
        this.transactionArchive = new TransactionArchive();

        this.netWorthHistory = new ArrayList<>();
        netWorthHistory.add(startingMoney);
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

    /**
     * Returns the player's total net worth in NOK: cash balance plus the market
     * value of all portfolio positions.
     *
     * <p>Market value is {@code salesPrice × quantity} per share, converted to
     * NOK via the given {@link CurrencyConverter}. Sale commission and tax are
     * NOT deducted — this is the gross market value, not a liquidation estimate.
     *
     * @param converter the currency converter used to translate share values to NOK
     * @return cash plus market value of holdings, in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getNetWorth(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        return money.add(portfolio.getNetWorth(converter));
    }

    /**
     * Records the player's current net worth in the history.
     * Called by {@link edu.ntnu.idatt2003.millions.service.GameService}
     * before advancing the week.
     *
     * @param converter the currency converter used to compute the net worth
     * @throws NullPointerException if converter is null
     */
    public void recordNetWorth(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        if (netWorthHistory == null) netWorthHistory = new ArrayList<>();
        netWorthHistory.add(getNetWorth(converter));
    }

    /**
     * Returns a list of all recorded net worth values over time.
     * Each entry corresponds to the net worth at the end of a week.
     * Returns an empty list if no history has been recorded yet.
     *
     * @return a copy of the net worth history
     */
    public List<BigDecimal> getNetWorthHistory() {
        if (netWorthHistory == null) return List.of();
        return new ArrayList<>(netWorthHistory);
    }

    /**
     * Returns the player's net worth from before the last week advance.
     * Returns null if no week has been advanced yet.
     *
     * @return the previous net worth, or null if not yet available
     */
    public BigDecimal getPreviousNetWorth() {
        return previousNetWorth;
    }

    /**
     * Sets the player's previous net worth.
     * Called by {@link edu.ntnu.idatt2003.millions.service.GameService}
     * before advancing the week.
     *
     * @param previousNetWorth the net worth to store
     */
    public void setPreviousNetWorth(BigDecimal previousNetWorth) {
        this.previousNetWorth = previousNetWorth;
    }

    /**
     * Returns the absolute change in net worth since the start of the game.
     *
     * @param converter the currency converter used to compute the current net worth
     * @return current net worth minus starting money, in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getNetWorthChangeSinceStart(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        return getNetWorth(converter).subtract(startingMoney);
    }

    /**
     * Returns the percentage change in net worth since the start of the game.
     * Returns zero if the starting money was zero.
     *
     * @param converter the currency converter used to compute the current net worth
     * @return percent change since start, e.g. 12.5 means +12.5%
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getNetWorthChangePercentSinceStart(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        if (startingMoney.signum() == 0) return BigDecimal.ZERO;
        return getNetWorthChangeSinceStart(converter)
                .divide(startingMoney, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Returns the absolute change in net worth since the previous week.
     * Returns null if no week has been advanced yet.
     *
     * @param converter the currency converter used to compute the current net worth
     * @return current net worth minus previous net worth, or null if not available
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getWeeklyNetWorthChange(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        if (previousNetWorth == null) return null;
        return getNetWorth(converter).subtract(previousNetWorth);
    }

    /**
     * Returns the percentage change in net worth since the previous week.
     * Returns null if no week has been advanced yet, or zero if the previous
     * net worth was zero.
     *
     * @param converter the currency converter used to compute the current net worth
     * @return percent change since last week, or null if not available
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getWeeklyNetWorthChangePercent(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        if (previousNetWorth == null) return null;
        if (previousNetWorth.signum() == 0) return BigDecimal.ZERO;
        return getWeeklyNetWorthChange(converter)
                .divide(previousNetWorth, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /***
     * Returns the players current status based on net worth growth and number of weeks with active
     * trading.
     * <ul>
     *     <li>{@link PlayerStatusLevel#NOVICE}: basic start level, all other cases</li>
     *     <li>{@link PlayerStatusLevel#INVESTOR}: players who have been trading for minimum 10 weeks and increased their
     *          net worth with at least 20%.</li>
     *     <li> {@link PlayerStatusLevel#SPECULATOR}: players who have been trading for minimum 20 weeks and have
     *          minimum doubled their net worth.</li>
     * </ul>
     *
     * @param converter the currency converter used to compute the player's net worth in NOK
     * @return the players status level {@link PlayerStatusLevel}
     * @throws NullPointerException if converter is null
     */
    public PlayerStatusLevel getStatus(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        int weeksTraded = transactionArchive.countDistinctWeeks();
        BigDecimal netWorth = getNetWorth(converter);
        BigDecimal twentyPercentGrowth = startingMoney.multiply(new BigDecimal("1.20"));
        BigDecimal doubleGrowth = startingMoney.multiply(new BigDecimal("2.00"));

        if (weeksTraded >= 20 && netWorth.compareTo(doubleGrowth) >= 0) {
            return PlayerStatusLevel.SPECULATOR;
        } else if (weeksTraded >= 10 && netWorth.compareTo(twentyPercentGrowth) >= 0) {
            return PlayerStatusLevel.INVESTOR;
        } else {
            return PlayerStatusLevel.NOVICE;
        }
    }
}