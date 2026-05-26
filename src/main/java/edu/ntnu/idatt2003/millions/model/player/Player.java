// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.model.player;

import edu.ntnu.idatt2003.millions.model.transaction.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.model.watchlist.WatchlistEntry;
import edu.ntnu.idatt2003.millions.service.game.GameService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a player in the game.
 * A player has a name, a starting balance, and owns a {@link Portfolio} of shares,
 * a {@link TransactionArchive} of committed transactions, and a watchlist of
 * {@link WatchlistEntry} objects for stocks the player wants to monitor without owning.
 */
public class Player {

    /**
     * Maximum ratio of total outstanding loan debt to current net worth.
     * A player may not borrow more than this fraction of their net worth in total.
     */
    public static final BigDecimal MAX_DEBT_RATIO = new BigDecimal("0.50");

    private final String name;
    private final BigDecimal startingMoney;
    private final String sessionId;
    private BigDecimal cash;

    private final Portfolio portfolio;
    private final TransactionArchive transactionArchive;
    private List<Loan> activeLoans;

    private BigDecimal previousNetWorth;
    private List<BigDecimal> netWorthHistory;
    private List<BigDecimal> totalDebtHistory = new ArrayList<>();
    private List<LoanLedgerEntry> loanLedger = new ArrayList<>();

    private List<WatchlistEntry> watchlist = new ArrayList<>();

    private List<Notification> notifications = new ArrayList<>();
    private int nextNotificationId = 1;
    private boolean wasAboveDebtThreshold = false;
    private boolean wasLowOnCash = false;
    private PlayerStatusLevel previousStatus = PlayerStatusLevel.NOVICE;

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
        this.sessionId = UUID.randomUUID().toString();
        this.cash = startingMoney;

        this.portfolio = new Portfolio();
        this.transactionArchive = new TransactionArchive();
        this.activeLoans = new ArrayList<>();

        this.netWorthHistory = new ArrayList<>();
        netWorthHistory.add(startingMoney);
    }

    public String getName() {
        return name;
    }

    public BigDecimal getStartingMoney() {
        return startingMoney;
    }

    /**
     * Returns the stable session identifier for this player, generated at
     * construction time and preserved across save and load via Gson.
     *
     * <p>Used by the leaderboard to keep a single entry per game session,
     * regardless of how many times the player saves.</p>
     *
     * @return the session UUID as a string
     */
    public String getSessionId() {
        return sessionId;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

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
        this.cash = this.cash.add(amount);
    }

    /**
     * Withdraws the specified amount from the player's balance.
     *
     * @param amount the amount to withdraw
     * @throws IllegalArgumentException if the amount is negative or the player does not have sufficient funds
     */
    public void withdrawMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount cannot be negative");
        if (this.cash.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Cannot withdraw more money than the current balance");
        }
        this.cash = this.cash.subtract(amount);
    }

    /**
     * Returns the player's total net worth in NOK: cash balance plus the market
     * value of all portfolio positions, minus outstanding debt.
     *
     * <p>Market value is {@code salesPrice × quantity} per share, converted to
     * NOK via the given {@link CurrencyConverter}. Sale commission and tax are
     * NOT deducted — this is the gross market value, not a liquidation estimate.
     *
     * @param converter the currency converter used to translate share values to NOK
     * @return cash plus market value of holdings minus outstanding debt, in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getNetWorth(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        return cash.add(portfolio.getNetWorth(converter)).subtract(getTotalDebt());
    }

    /**
     * Appends the player's current net worth to the history list.
     * Called by {@link GameService}
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
     * Called by {@link GameService}
     * before advancing the week.
     *
     * @param previousNetWorth the net worth to store
     */
    public void setPreviousNetWorth(BigDecimal previousNetWorth) {
        this.previousNetWorth = previousNetWorth;
    }

    private List<Loan> activeLoansInternal() {
        if (activeLoans == null) activeLoans = new ArrayList<>();
        return activeLoans;
    }

    /**
     * Returns a defensive copy of the player's active loans.
     *
     * @return mutable snapshot of active loans; empty if none have been taken
     */
    public List<Loan> getActiveLoans() {
        return new ArrayList<>(activeLoansInternal());
    }

    /**
     * Returns the sum of all outstanding loan principals.
     *
     * @return total debt in NOK; zero if no active loans
     */
    public BigDecimal getTotalDebt() {
        return activeLoansInternal().stream()
                .map(Loan::principal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Records the player's current total debt in the history.
     * Called by {@link GameService}
     * before advancing the week.
     */
    public void recordTotalDebt() {
        if (totalDebtHistory == null) totalDebtHistory = new ArrayList<>();
        totalDebtHistory.add(getTotalDebt());
    }

    /**
     * Returns a list of all recorded total-debt snapshots over time.
     * Each entry corresponds to the debt at the end of a week.
     *
     * @return a defensive copy of the debt history
     */
    public List<BigDecimal> getTotalDebtHistory() {
        if (totalDebtHistory == null) return List.of();
        return new ArrayList<>(totalDebtHistory);
    }

    /**
     * Returns a defensive copy of all loan-related ledger entries.
     * Each entry records a disbursement, interest deduction, or repayment.
     *
     * @return copy of the loan ledger; empty if no loan activity has occurred
     */
    public List<LoanLedgerEntry> getLoanLedger() {
        if (loanLedger == null) return List.of();
        return new ArrayList<>(loanLedger);
    }

    /**
     * Returns the maximum total debt the player may carry, equal to
     * {@link #MAX_DEBT_RATIO} of their current net worth.
     *
     * @param converter the currency converter used to compute net worth
     * @return loan capacity in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getLoanCapacity(CurrencyConverter converter) {
        return getNetWorth(converter)
                .multiply(MAX_DEBT_RATIO)
                .setScale(2, RoundingMode.HALF_UP)
                .max(BigDecimal.ZERO);
    }

    /**
     * Returns how much additional debt the player may take on right now.
     * Equal to {@link #getLoanCapacity} minus {@link #getTotalDebt}, floored at zero.
     *
     * @param converter the currency converter used to compute net worth
     * @return available borrowing capacity in NOK; never negative
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getAvailableLoanCapacity(CurrencyConverter converter) {
        return getLoanCapacity(converter).subtract(getTotalDebt()).max(BigDecimal.ZERO);
    }

    /**
     * Disburses a loan to the player: credits their balance with the principal
     * and records the loan as an active debt.
     *
     * <p>The combined total of existing debt plus this loan's principal must not
     * exceed {@link #MAX_DEBT_RATIO} of the player's current net worth. If it
     * would, an {@link ExcessiveDebtException} is thrown and no state is mutated.
     *
     * @param loan      the loan to take; must not be null
     * @param converter the currency converter used to evaluate net worth
     * @throws NullPointerException    if loan or converter is null
     * @throws ExcessiveDebtException  if taking the loan would breach the debt ratio
     */
    public void takeLoan(Loan loan, CurrencyConverter converter) throws ExcessiveDebtException {
        Objects.requireNonNull(loan, "Loan cannot be null");
        Objects.requireNonNull(converter, "Converter cannot be null");
        BigDecimal newTotalDebt = getTotalDebt().add(loan.principal());
        BigDecimal capacity = getLoanCapacity(converter);
        if (newTotalDebt.compareTo(capacity) > 0) {
            throw new ExcessiveDebtException(newTotalDebt, capacity);
        }
        addMoney(loan.principal());
        activeLoansInternal().add(loan);
        if (loanLedger == null) loanLedger = new ArrayList<>();
        loanLedger.add(new LoanLedgerEntry(
                Math.max(loan.takenAtWeek(), 1), loan,
                LoanLedgerEntryType.DISBURSEMENT, loan.principal()));
    }

    /**
     * Repays a loan in full: withdraws the principal from the player's cash
     * balance and removes the loan from the active list.
     *
     * @param loan        the loan to repay; must be an active loan owned by this player
     * @param currentWeek the game week in which the repayment occurs; used for the ledger entry
     * @throws NullPointerException     if loan is null
     * @throws IllegalArgumentException if the player cannot afford the repayment
     * @throws IllegalArgumentException if the loan is not in the active list
     */
    public void repayLoan(Loan loan, int currentWeek) {
        Objects.requireNonNull(loan, "Loan cannot be null");
        if (cash.compareTo(loan.principal()) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds to repay loan of " + loan.principal() + " NOK");
        }
        if (!activeLoansInternal().remove(loan)) {
            throw new IllegalArgumentException("Loan is not an active loan for this player");
        }
        cash = cash.subtract(loan.principal());
        if (loanLedger == null) loanLedger = new ArrayList<>();
        loanLedger.add(new LoanLedgerEntry(
                Math.max(currentWeek, 1), loan,
                LoanLedgerEntryType.REPAYMENT, loan.principal().negate()));
    }

    /**
     * Deducts one week's interest for every active loan from the player's
     * cash balance and records an INTEREST ledger entry for each loan.
     *
     * <p>This method assumes the caller has already verified the player can
     * cover the obligation — see
     * {@link #canCoverObligationsThisWeek(int)} and the dispatch logic in
     * the controller layer. When the player cannot cover obligations from
     * cash alone, the forced-sale flow runs instead via
     * {@link GameService#executeForcedSale}.
     *
     * @param week the game week in which interest is collected;
     *             used for ledger entries
     */
    public void collectWeeklyInterest(int week) {
        List<Loan> loans = activeLoansInternal();
        BigDecimal total = BigDecimal.ZERO;
        if (loanLedger == null) loanLedger = new ArrayList<>();
        for (Loan loan : loans) {
            BigDecimal interest = loan.weeklyInterest();
            total = total.add(interest);
            if (interest.signum() > 0) {
                loanLedger.add(new LoanLedgerEntry(
                        Math.max(week, 1), loan,
                        LoanLedgerEntryType.INTEREST, interest.negate()));
            }
        }
        if (total.signum() == 0) return;
        BigDecimal paid = total.min(cash);
        cash = cash.subtract(paid);
    }

    /**
     * Returns the total weekly interest owed across all active loans this week.
     *
     * @return sum of each active loan's weekly interest; zero when no loans are active
     */
    public BigDecimal getWeeklyInterestDue() {
        return activeLoansInternal().stream()
                .map(Loan::weeklyInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns {@code true} iff the player's current cash balance is at least
     * equal to {@link #getWeeklyInterestDue()}.
     *
     * @return true if the player can cover this week's interest without selling shares
     */
    public boolean canCoverInterestThisWeek() {
        return cash.compareTo(getWeeklyInterestDue()) >= 0;
    }

    /**
     * Returns loans whose principal becomes due at {@code currentWeek}
     * (i.e., {@link Loan#isDueThisWeek(int)} is true for each).
     *
     * @param currentWeek the game week to evaluate
     * @return immutable list of maturing loans; empty if none are due
     */
    public List<Loan> getLoansDueThisWeek(int currentWeek) {
        return activeLoansInternal().stream()
                .filter(loan -> loan.isDueThisWeek(currentWeek))
                .toList();
    }

    /**
     * Returns the sum of principals for all loans maturing at {@code currentWeek}.
     *
     * @param currentWeek the game week to evaluate
     * @return total maturity principal in NOK; zero if no loans are due
     */
    public BigDecimal getMaturityDueThisWeek(int currentWeek) {
        return getLoansDueThisWeek(currentWeek).stream()
                .map(Loan::principal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the total amount the player must pay this week: weekly interest
     * across all active loans plus the principal of any maturing loans.
     *
     * @param currentWeek the game week to evaluate
     * @return interest + maturity principal in NOK; zero if no loans are active
     */
    public BigDecimal getTotalObligationsThisWeek(int currentWeek) {
        return getWeeklyInterestDue().add(getMaturityDueThisWeek(currentWeek));
    }

    /**
     * Returns {@code true} iff the player's current cash covers
     * {@link #getTotalObligationsThisWeek(int)}.
     *
     * @param currentWeek the game week to evaluate
     * @return true if no forced share sale is required
     */
    public boolean canCoverObligationsThisWeek(int currentWeek) {
        return cash.compareTo(getTotalObligationsThisWeek(currentWeek)) >= 0;
    }

    /**
     * Removes a matured loan from the active list and writes a REPAYMENT
     * ledger entry, without touching the cash balance.
     *
     * <p>Used by the forced-sale path where the total obligation has already
     * been withdrawn from cash in a single {@link #withdrawMoney} call.</p>
     *
     * @param loan the loan to close; must be an active loan owned by this player
     * @param week the game week in which settlement occurs; used for the ledger entry
     * @throws NullPointerException     if loan is null
     * @throws IllegalArgumentException if the loan is not in the active list
     */
    public void settleMatureLoan(Loan loan, int week) {
        Objects.requireNonNull(loan, "Loan cannot be null");
        if (!activeLoansInternal().remove(loan)) {
            throw new IllegalArgumentException("Loan is not an active loan for this player");
        }
        if (loanLedger == null) loanLedger = new ArrayList<>();
        loanLedger.add(new LoanLedgerEntry(
                Math.max(week, 1), loan,
                LoanLedgerEntryType.REPAYMENT, loan.principal().negate()));
    }

    /**
     * Appends an INTEREST {@link LoanLedgerEntry} for every active loan without
     * touching the cash balance. Called by the forced-sale path after the player's
     * cash has already been adjusted separately via {@link #withdrawMoney}.
     *
     * @param week the game week in which interest is recorded; must be &gt;= 1
     */
    public void writeInterestLedgerEntries(int week) {
        if (loanLedger == null) loanLedger = new ArrayList<>();
        for (Loan loan : activeLoansInternal()) {
            BigDecimal interest = loan.weeklyInterest();
            if (interest.signum() > 0) {
                loanLedger.add(new LoanLedgerEntry(
                        Math.max(week, 1), loan,
                        LoanLedgerEntryType.INTEREST, interest.negate()));
            }
        }
    }

    /**
     * Returns the total liquidation value of all portfolio positions plus cash, in NOK.
     * Liquidation value per share is the net payout after commission and tax,
     * converted to NOK via the given converter.
     *
     * @param converter the currency converter used to translate share values to NOK
     * @return cash plus net liquidation value of all holdings, in NOK
     * @throws NullPointerException if converter is null
     */
    public BigDecimal getTotalLiquidationValue(CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        BigDecimal portfolioLiquidation = portfolio.getShares().stream()
                .map(s -> SalesCalculator.calculateNetNok(s, converter))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return cash.add(portfolioLiquidation);
    }

    /**
     * Returns {@code true} iff the player's total liquidation value
     * (cash plus net sale proceeds from all holdings) is enough to cover
     * {@link #getTotalObligationsThisWeek(int)}.
     *
     * <p>When this returns {@code false} and
     * {@link #canCoverObligationsThisWeek(int)} is also {@code false}, a
     * forced sale cannot save the player — the game is over.</p>
     *
     * @param currentWeek the game week to evaluate
     * @param converter   the currency converter used to compute liquidation value
     * @return true if forced-sale is viable; false if game over
     * @throws NullPointerException if converter is null
     */
    public boolean canCoverWithFullLiquidation(int currentWeek, CurrencyConverter converter) {
        Objects.requireNonNull(converter, "Converter cannot be null");
        return getTotalLiquidationValue(converter)
                .compareTo(getTotalObligationsThisWeek(currentWeek)) >= 0;
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

    /**
     * Returns an unmodifiable view of the player's notifications, oldest first.
     *
     * @return unmodifiable list of notifications; empty if none have been added
     */
    public List<Notification> getNotifications() {
        if (notifications == null) notifications = new ArrayList<>();
        return Collections.unmodifiableList(notifications);
    }

    /**
     * Returns the number of notifications the player has not yet read.
     *
     * @return count of unread notifications; zero if all have been read or none exist
     */
    public int getUnreadNotificationCount() {
        if (notifications == null) return 0;
        return (int) notifications.stream().filter(n -> !n.read()).count();
    }

    /**
     * Returns the debt-threshold flag recorded during the most recent weekly advance.
     *
     * @return {@code true} if the player was above the debt threshold when last checked
     */
    public boolean wasAboveDebtThreshold() {
        return wasAboveDebtThreshold;
    }

    /**
     * Returns the low-cash flag recorded during the most recent weekly advance.
     *
     * @return {@code true} if the player was considered low on cash when last checked
     */
    public boolean wasLowOnCash() {
        return wasLowOnCash;
    }

    /**
     * Returns the next notification ID and advances the internal counter.
     * Each call returns a unique, strictly increasing integer starting at 1.
     *
     * @return the next available notification ID
     */
    public int nextNotificationId() {
        if (nextNotificationId <= 0) nextNotificationId = 1;
        return nextNotificationId++;
    }

    /**
     * Appends a notification to the player's notification list.
     *
     * @param notification the notification to append
     */
    public void addNotification(Notification notification) {
        if (notifications == null) notifications = new ArrayList<>();
        notifications.add(notification);
    }

    /**
     * Marks every unread notification as read.
     */
    public void markAllNotificationsAsRead() {
        if (notifications == null) return;
        for (int i = 0; i < notifications.size(); i++) {
            if (!notifications.get(i).read()) {
                notifications.set(i, notifications.get(i).markAsRead());
            }
        }
    }

    /**
     * Removes all notifications from the player's notification list.
     */
    public void clearAllNotifications() {
        if (notifications == null) notifications = new ArrayList<>();
        notifications.clear();
    }

    public void setWasAboveDebtThreshold(boolean value) {
        this.wasAboveDebtThreshold = value;
    }

    public void setWasLowOnCash(boolean value) {
        this.wasLowOnCash = value;
    }

    /**
     * Returns the player's status level recorded at the end of the previous week.
     * Returns {@link PlayerStatusLevel#NOVICE} if no status has been recorded yet.
     *
     * @return the previously recorded status level; never {@code null}
     */
    public PlayerStatusLevel getPreviousStatus() {
        return previousStatus == null ? PlayerStatusLevel.NOVICE : previousStatus;
    }

    public void setPreviousStatus(PlayerStatusLevel status) {
        this.previousStatus = status;
    }

    /**
     * Returns a defensive copy of all entries currently on the player's watchlist.
     *
     * @return immutable snapshot of the watchlist; empty if no entries have been added
     */
    public List<WatchlistEntry> getWatchlist() {
        if (watchlist == null) watchlist = new ArrayList<>();
        return new ArrayList<>(watchlist);
    }

    /**
     * Returns {@code true} if the given stock symbol is currently on the watchlist.
     *
     * @param symbol the ticker symbol to look up; must not be null
     * @return {@code true} if an entry with that symbol exists
     * @throws NullPointerException if symbol is null
     */
    public boolean isOnWatchlist(String symbol) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (watchlist == null) watchlist = new ArrayList<>();
        return watchlist.stream().anyMatch(e -> e.symbol().equals(symbol));
    }

    /**
     * Adds the given entry to the watchlist.
     * If an entry with the same symbol already exists, this call is a no-op.
     *
     * @param entry the entry to add; must not be null
     * @throws NullPointerException if entry is null
     */
    public void addToWatchlist(WatchlistEntry entry) {
        Objects.requireNonNull(entry, "WatchlistEntry cannot be null");
        if (watchlist == null) watchlist = new ArrayList<>();
        if (!isOnWatchlist(entry.symbol())) {
            watchlist.add(entry);
        }
    }

    /**
     * Removes the entry with the given symbol from the watchlist.
     * If no such entry exists, this call is a no-op.
     *
     * @param symbol the ticker symbol of the entry to remove; must not be null
     * @throws NullPointerException if symbol is null
     */
    public void removeFromWatchlist(String symbol) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (watchlist == null) watchlist = new ArrayList<>();
        watchlist.removeIf(e -> e.symbol().equals(symbol));
    }

    /**
     * Replaces the note on the watchlist entry identified by {@code symbol}.
     * Uses {@link WatchlistEntry} to produce an updated copy.
     * If no entry with that symbol exists, this call is a no-op.
     *
     * @param symbol  the ticker symbol of the entry to update; must not be null
     * @param newNote the new note text; {@code null} is normalised to {@code ""}
     * @throws NullPointerException if symbol is null
     */
    public void updateWatchlistNote(String symbol, String newNote) {
        Objects.requireNonNull(symbol, "Symbol cannot be null");
        if (watchlist == null) watchlist = new ArrayList<>();
        for (int i = 0; i < watchlist.size(); i++) {
            if (watchlist.get(i).symbol().equals(symbol)) {
                watchlist.set(i, watchlist.get(i).withNote(newNote));
                return;
            }
        }
    }

    /**
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