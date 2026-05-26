// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.service.notification;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.model.notification.Notification;
import edu.ntnu.idatt2003.millions.model.notification.Notification.Severity;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.PlayerStatusLevel;
import edu.ntnu.idatt2003.millions.model.stock.Stock;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Evaluates game events and pushes the resulting {@link Notification} objects to the player.
 * Called after each week advances and after a loan is taken.
 */
public class NotificationService {

    /** Debt-to-capacity ratio at or above which a high-debt warning notification is generated. */
    public static final BigDecimal DEBT_RATIO_WARNING_THRESHOLD = new BigDecimal("0.85");

    /** Minimum absolute price change (as a fraction) that triggers a stock-movement notification. */
    public static final BigDecimal STOCK_MOVEMENT_THRESHOLD = new BigDecimal("0.09");

    /** Number of weeks before loan maturity at which a near-maturity warning is generated. */
    public static final int LOAN_NEAR_MATURITY_WEEKS = 3;

    /**
     * Runs all notification checks for the current week and pushes any triggered
     * notifications to {@code player}.
     *
     * @param player    the player to check and push notifications to
     * @param exchange  the exchange providing the current week number and stock data
     * @param converter currency converter used for debt-ratio and status checks
     */
    public void onWeekAdvanced(Player player, Exchange exchange, CurrencyConverter converter) {
        int currentWeek = exchange.getWeek();
        checkLoanRepayments(player, currentWeek);
        checkLoanMaturities(player, currentWeek);
        checkDebtRatio(player, currentWeek, converter);
        checkLowCash(player, currentWeek);
        checkStockMovements(player, exchange, currentWeek);
        checkStatusChange(player, currentWeek, converter);
    }

    /**
     * Re-evaluates the debt-ratio check immediately after a loan is taken and pushes
     * a warning to {@code player} if the threshold is exceeded.
     *
     * @param player      the player who took the loan
     * @param currentWeek the week in which the loan was taken
     * @param converter   currency converter used to compute loan capacity
     */
    public void onLoanTaken(Player player, int currentWeek, CurrencyConverter converter) {
        checkDebtRatio(player, currentWeek, converter);
    }

    private void checkLoanRepayments(Player player, int currentWeek) {
        List<LoanLedgerEntry> repaymentsThisWeek = player.getLoanLedger().stream()
                .filter(e -> e.week() == currentWeek)
                .filter(e -> e.type() == LoanLedgerEntryType.REPAYMENT)
                .toList();

        for (LoanLedgerEntry entry : repaymentsThisWeek) {
            push(player, Severity.INFO,
                    "notification.loanRepaid.title",
                    "notification.loanRepaid.body",
                    List.of("@loans.offer." + entry.loan().offer().id() + ".name",
                            entry.amount().abs().toPlainString()),
                    currentWeek);
        }
    }

    private void checkLoanMaturities(Player player, int currentWeek) {
        for (Loan loan : player.getActiveLoans()) {
            int weeksLeft = loan.weeksRemaining(currentWeek);
            if (weeksLeft == 1) {
                push(player, Severity.SEVERE,
                        "notification.loanMaturity.dueNextWeek.title",
                        "notification.loanMaturity.dueNextWeek.body",
                        List.of("@loans.offer." + loan.offer().id() + ".name",
                                loan.principal().toPlainString()),
                        currentWeek);
            } else if (weeksLeft >= 2 && weeksLeft <= LOAN_NEAR_MATURITY_WEEKS) {
                push(player, Severity.WARNING,
                        "notification.loanMaturity.soon.title",
                        "notification.loanMaturity.soon.body",
                        List.of("@loans.offer." + loan.offer().id() + ".name",
                                String.valueOf(weeksLeft),
                                loan.principal().toPlainString()),
                        currentWeek);
            }
        }
    }

    private void checkDebtRatio(Player player, int currentWeek, CurrencyConverter converter) {
        BigDecimal capacity = player.getLoanCapacity(converter);
        boolean isAbove;
        int percentDisplay;

        if (capacity.compareTo(BigDecimal.ZERO) == 0) {
            isAbove = player.getTotalDebt().compareTo(BigDecimal.ZERO) > 0;
            percentDisplay = isAbove ? 100 : 0;
        } else {
            BigDecimal ratio = player.getTotalDebt()
                    .divide(capacity, 4, RoundingMode.HALF_UP);
            isAbove = ratio.compareTo(DEBT_RATIO_WARNING_THRESHOLD) >= 0;
            percentDisplay = ratio.multiply(BigDecimal.valueOf(100)).intValue();
        }

        if (isAbove && !player.wasAboveDebtThreshold()) {
            push(player, Severity.WARNING,
                    "notification.debtRatio.high.title",
                    "notification.debtRatio.high.body",
                    List.of(String.valueOf(percentDisplay)),
                    currentWeek);
        }
        player.setWasAboveDebtThreshold(isAbove);
    }

    private void checkLowCash(Player player, int currentWeek) {
        int nextWeek = currentWeek + 1;
        BigDecimal nextObligation = player.getTotalObligationsThisWeek(nextWeek);

        if (nextObligation.compareTo(BigDecimal.ZERO) == 0) {
            player.setWasLowOnCash(false);
            return;
        }

        boolean isLow = player.getCash().compareTo(nextObligation) < 0;

        if (isLow && !player.wasLowOnCash()) {
            BigDecimal shortfall = nextObligation.subtract(player.getCash());
            push(player, Severity.WARNING,
                    "notification.lowCash.title",
                    "notification.lowCash.body",
                    List.of(shortfall.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                            nextObligation.setScale(2, RoundingMode.HALF_UP).toPlainString()),
                    currentWeek);
        }
        player.setWasLowOnCash(isLow);
    }

    private void checkStockMovements(Player player, Exchange exchange, int currentWeek) {
        Set<String> ownedSymbols = player.getPortfolio().getShares().stream()
                .map(s -> s.getStock().getSymbol())
                .collect(Collectors.toSet());

        for (String symbol : ownedSymbols) {
            Stock stock = exchange.getStock(symbol);
            BigDecimal pct = stock.getLatestPercentChange();
            BigDecimal displayPct = pct.abs()
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

            if (pct.compareTo(STOCK_MOVEMENT_THRESHOLD) >= 0) {
                push(player, Severity.INFO,
                        "notification.stockMovement.gain.title",
                        "notification.stockMovement.gain.body",
                        List.of(stock.getSymbol(), displayPct.toPlainString()),
                        currentWeek);
            } else if (pct.compareTo(STOCK_MOVEMENT_THRESHOLD.negate()) <= 0) {
                push(player, Severity.INFO,
                        "notification.stockMovement.drop.title",
                        "notification.stockMovement.drop.body",
                        List.of(stock.getSymbol(), displayPct.toPlainString()),
                        currentWeek);
            }
        }
    }

    private void checkStatusChange(Player player, int currentWeek, CurrencyConverter converter) {
        PlayerStatusLevel current = player.getStatus(converter);
        PlayerStatusLevel previous = player.getPreviousStatus();

        if (current.ordinal() > previous.ordinal()) {
            push(player, Severity.MILESTONE,
                    "notification.statusUpgrade.title",
                    "notification.statusUpgrade.body",
                    List.of(current.name()),
                    currentWeek);
        } else if (current.ordinal() < previous.ordinal()) {
            push(player, Severity.INFO,
                    "notification.statusDowngrade.title",
                    "notification.statusDowngrade.body",
                    List.of(current.name()),
                    currentWeek);
        }

        player.setPreviousStatus(current);
    }

    private void push(Player player, Severity severity, String titleKey,
                      String bodyKey, List<String> args, int week) {
        Notification n = new Notification(
                player.nextNotificationId(), severity, titleKey, bodyKey, args, week, false
        );
        player.addNotification(n);
    }
}
