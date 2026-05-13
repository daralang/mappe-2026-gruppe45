package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.LoanPreviewService;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.dialog.LoanApplicationDialog;

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
        BigDecimal netWorth = gameService.getPlayer()
                .getNetWorth(gameService.getCurrencyConverter());

        LoanApplicationDialog[] ref = new LoanApplicationDialog[1];
        ref[0] = new LoanApplicationDialog(
                offer,
                netWorth,
                amount -> previewService.preview(offer, amount),
                amount -> handleConfirm(ref[0], offer, amount)
        );
        ref[0].show();
    }

    private void handleConfirm(LoanApplicationDialog dialog, LoanOffer offer, BigDecimal amount) {
        try {
            gameService.takeLoan(offer, amount);
            dialog.close();
        } catch (Exception e) {
            dialog.showError(e.getMessage());
        }
    }
}
