// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.controller.loan;

import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.loan.LoanPreviewService;
import edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.dialog.LoanApplicationDialog;
import edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.dialog.LoanDetailsModal;
import edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.dialog.LoanRepaymentReceipt;
import edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.dialog.RepayLoanDialog;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for loan-related actions: applying for loans, repaying loans, and viewing loan details.
 */
public class LoanController {

    private static final Logger LOGGER = Logger.getLogger(LoanController.class.getName());

    private final GameService gameService;
    private final LoanPreviewService previewService = new LoanPreviewService();

    /**
     * Constructs a new LoanController backed by the given game service.
     *
     * @param gameService the game service used to apply and repay loans
     */
    public LoanController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Opens the loan application dialog for the given offer.
     *
     * @param offer the loan offer the player wants to apply for
     */
    public void openLoanDialog(LoanOffer offer) {
        LoanApplicationDialog[] ref = new LoanApplicationDialog[1];
        ref[0] = new LoanApplicationDialog(
                offer,
                this::validateLoanAmount,
                amount -> previewService.preview(offer, amount),
                amount -> handleConfirm(ref[0], offer, amount)
        );
        ref[0].show();
    }

    /**
     * Opens the repay dialog for the given loan.
     *
     * @param loan      the loan to repay
     * @param loanIndex per-type index used for display (e.g. 2 → "Standard loan #2")
     */
    public void openRepayDialog(Loan loan, int loanIndex) {
        Player player = gameService.getPlayer();
        int currentWeek = gameService.getExchange().getWeek();
        RepayLoanDialog[] ref = new RepayLoanDialog[1];
        ref[0] = new RepayLoanDialog(loan, loanIndex, player.getCash(), currentWeek,
                validateRepay(loan));
        ref[0].setOnConfirm(l -> handleRepayConfirm(ref[0], l, loanIndex));
        ref[0].show();
    }

    /**
     * Returns the current game week.
     *
     * @return the current exchange week
     */
    public int getCurrentWeek() {
        return gameService.getExchange().getWeek();
    }

    /**
     * Returns whether the current game has ended.
     *
     * @return {@code true} if the game is over
     */
    public boolean isGameOver() {
        return gameService.isGameOver();
    }

    /**
     * Opens the read-only loan details modal for the given loan.
     *
     * @param loan      the loan to inspect
     * @param loanIndex per-type display index (e.g. 2 → "Standard loan #2")
     */
    public void openLoanDetailsModal(Loan loan, int loanIndex) {
        new LoanDetailsModal(loan, loanIndex, this).show();
    }

    /**
     * Validates whether the player may borrow {@code amount} right now.
     *
     * @param amount the requested loan amount
     * @return empty if allowed; present with an i18n error key if the amount exceeds the player's capacity
     */
    private Optional<String> validateLoanAmount(BigDecimal amount) {
        BigDecimal capacity = gameService.getPlayer()
                .getAvailableLoanCapacity(gameService.getCurrencyConverter());
        if (amount.compareTo(capacity) > 0) {
            return Optional.of("loans.dialog.error.exceedsCapacity");
        }
        return Optional.empty();
    }

    /**
     * Validates whether the player can afford to repay {@code loan} right now.
     *
     * @param loan the loan the player wants to repay
     * @return empty if the player has sufficient cash; present with an i18n error key if not
     */
    private Optional<String> validateRepay(Loan loan) {
        if (gameService.getPlayer().getCash().compareTo(loan.principal()) < 0) {
            return Optional.of("loans.repay.error.insufficientFunds");
        }
        return Optional.empty();
    }

    private void handleConfirm(LoanApplicationDialog dialog, LoanOffer offer, BigDecimal amount) {
        try {
            gameService.takeLoan(offer, amount);
            dialog.close();
        } catch (ExcessiveDebtException e) {
            dialog.showError(e.getMessage());
        } catch (IllegalStateException e) {
            dialog.showError(LanguageManager.get("error.gameOver"));
            LOGGER.log(Level.INFO, "Take loan blocked — game is over", e);
        }
    }

    private void handleRepayConfirm(RepayLoanDialog dialog, Loan loan, int loanIndex) {
        try {
            BigDecimal balanceBefore = gameService.getPlayer().getCash();
            int week = gameService.getExchange().getWeek();
            gameService.repayLoan(loan);
            dialog.close();
            BigDecimal balanceAfter = gameService.getPlayer().getCash();
            Platform.runLater(() ->
                    new LoanRepaymentReceipt(loan, loanIndex, balanceBefore, balanceAfter, week).show());
        } catch (IllegalArgumentException e) {
            dialog.showError(e.getMessage());
        } catch (IllegalStateException e) {
            dialog.showError(LanguageManager.get("error.gameOver"));
            LOGGER.log(Level.INFO, "Repay loan blocked — game is over", e);
        }
    }
}
