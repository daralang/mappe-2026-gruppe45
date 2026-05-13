package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * Read-only details modal for a single active loan.
 *
 * <p>Shows the loan's terms, current time-based status, and a full cost
 * breakdown. A primary "Innfri lån" button closes this modal and immediately
 * opens the {@code RepayLoanDialog} for the same loan — mirroring the
 * ShareDetailsModal pattern of close-then-open.</p>
 *
 * <p>All business logic is delegated to the controller. This class is pure
 * view: it reads from the {@link Loan} record and renders.</p>
 */
public class LoanDetailsModal extends Modal {

    private final Loan loan;
    private final int loanIndex;
    private final LoanController controller;
    private final int weeksElapsed;
    private final int weeksRemaining;

    /**
     * @param loan       the active loan to display
     * @param loanIndex  per-type 1-based display index (e.g. 2 → "Standard loan #2")
     * @param controller the loan controller; used to open RepayLoanDialog on confirm
     */
    public LoanDetailsModal(Loan loan, int loanIndex, LoanController controller) {
        this.loan = loan;
        this.loanIndex = loanIndex;
        this.controller = controller;
        int currentWeek = controller.getCurrentWeek();
        this.weeksElapsed = currentWeek - loan.takenAtWeek();
        this.weeksRemaining = Math.max(0, loan.offer().termWeeks() - weeksElapsed);
    }

    @Override
    protected Region buildContent() {
        String offerName = LanguageManager.get("loans.offer." + loan.offer().id() + ".name");
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeaderWithSubtitle(
                        LanguageManager.get("loans.details.title"),
                        offerName + " #" + loanIndex),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(
                buildTermsBox(),
                buildStatusBox(),
                buildCostBox(),
                buildActions()
        );
        return body;
    }

    private SummaryBox buildTermsBox() {
        BigDecimal weeklyRate = loan.offer().weeklyInterestRate()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal annualRate = loan.offer().weeklyInterestRate()
                .multiply(BigDecimal.valueOf(52))
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        String annualRateValue = TableCells.NUMBER_FORMAT.format(annualRate) + " %";
        String termValue = MessageFormat.format(
                LanguageManager.get("loans.details.terms.termValue"),
                loan.offer().termWeeks());

        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("loans.details.section.terms"));
        box.addRow(LanguageManager.get("loans.details.terms.principal"),
                CurrencyFormatter.format(loan.principal()));
        box.addRow(LanguageManager.get("loans.details.terms.rate"),
                TableCells.NUMBER_FORMAT.format(weeklyRate) + " %");
        box.addRow(LanguageManager.get("loans.details.terms.annualRate"), annualRateValue);
        box.addRow(LanguageManager.get("loans.details.terms.term"), termValue);
        box.addRow(LanguageManager.get("loans.details.terms.takenInWeek"),
                String.valueOf(loan.takenAtWeek()));
        return box;
    }

    private SummaryBox buildStatusBox() {
        BigDecimal weeklyInterestAmount = loan.principal()
                .multiply(loan.offer().weeklyInterestRate())
                .setScale(2, RoundingMode.HALF_UP);
        int dueWeek = loan.takenAtWeek() + loan.offer().termWeeks();

        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("loans.details.section.status"));
        box.addRow(LanguageManager.get("loans.details.status.weeksElapsed"),
                weeksElapsed + " / " + loan.offer().termWeeks());
        box.addRow(LanguageManager.get("loans.details.status.weeksRemaining"),
                String.valueOf(weeksRemaining));
        box.addRow(LanguageManager.get("loans.details.status.weeklyInterestNow"),
                CurrencyFormatter.format(weeklyInterestAmount));
        box.addRow(LanguageManager.get("loans.details.status.dueWeek"),
                String.valueOf(dueWeek));
        return box;
    }

    private SummaryBox buildCostBox() {
        BigDecimal weeklyInterest = loan.principal()
                .multiply(loan.offer().weeklyInterestRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal paid = weeklyInterest
                .multiply(BigDecimal.valueOf(weeksElapsed))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = weeklyInterest
                .multiply(BigDecimal.valueOf(weeksRemaining))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = weeklyInterest
                .multiply(BigDecimal.valueOf(loan.offer().termWeeks()))
                .setScale(2, RoundingMode.HALF_UP);

        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("loans.details.section.cost"));
        box.addRow(LanguageManager.get("loans.details.cost.paid"),
                CurrencyFormatter.format(paid));
        box.addRow(LanguageManager.get("loans.details.cost.remaining"),
                CurrencyFormatter.format(remaining));
        box.addTotal(LanguageManager.get("loans.details.cost.total"),
                CurrencyFormatter.format(total));
        return box;
    }

    private Region buildActions() {
        Button closeBtn = new Button(LanguageManager.get("receipt.button.close"));
        closeBtn.getStyleClass().add("modal-button");
        closeBtn.setOnAction(e -> close());

        Button repayBtn = new Button(LanguageManager.get("loans.details.button.repay"));
        repayBtn.getStyleClass().addAll("modal-button", "modal-button-primary");
        repayBtn.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openRepayDialog(loan, loanIndex));
        });

        return ModalActions.row(closeBtn, repayBtn);
    }
}
