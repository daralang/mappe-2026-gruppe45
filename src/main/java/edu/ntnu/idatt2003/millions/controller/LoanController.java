package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.loan.ExcessiveDebtException;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.LoanPreviewService;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.dialog.LoanApplicationDialog;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.dialog.RepayLoanDialog;
import edu.ntnu.idatt2003.millions.view.dialog.LoanDetailsModal;

import java.math.BigDecimal;

/**
 * Controller for loan-related actions.
 * Opens the loan application dialog and delegates confirmation to {@link GameService}.
 */
public class LoanController {

    private final GameService gameService;
    private final LoanPreviewService previewService = new LoanPreviewService();

    public LoanController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Opens the loan application dialog for the given offer.
     * The dialog is modal and blocks until the player confirms or cancels.
     *
     * @param offer the loan offer the player wants to apply for
     */
    public void openLoanDialog(LoanOffer offer) {
        Player player = gameService.getPlayer();
        CurrencyConverter converter = gameService.getCurrencyConverter();
        BigDecimal availableLoanCapacity = player.getAvailableLoanCapacity(converter);

        LoanApplicationDialog[] ref = new LoanApplicationDialog[1];
        ref[0] = new LoanApplicationDialog(
                offer,
                availableLoanCapacity,
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
        ref[0] = new RepayLoanDialog(loan, loanIndex, player, currentWeek);
        ref[0].setOnConfirm(l -> handleRepayConfirm(ref[0], l));
        ref[0].show();
    }

    /**
     * Returns the current game week. Used by modals that need temporal context.
     *
     * @return the current exchange week
     */
    public int getCurrentWeek() {
        return gameService.getExchange().getWeek();
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

    private void handleConfirm(LoanApplicationDialog dialog, LoanOffer offer, BigDecimal amount) {
        try {
            gameService.takeLoan(offer, amount);
            dialog.close();
        } catch (ExcessiveDebtException e) {
            dialog.showError(e.getMessage());
        } catch (Exception e) {
            dialog.showError(e.getMessage());
        }
    }

    private void handleRepayConfirm(RepayLoanDialog dialog, Loan loan) {
        try {
            gameService.repayLoan(loan);
            dialog.close();
        } catch (Exception e) {
            dialog.showError(e.getMessage());
        }
    }
}
