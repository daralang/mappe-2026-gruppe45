package edu.ntnu.idatt2003.millions.view.dashboard.loans.dialog;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Modal dialog for repaying a loan in full.
 *
 * <p>Shows which loan is being repaid (via header subtitle), its temporal
 * context (taken-at week, weeks remaining, weekly rate), a breakdown of
 * what repaying now costs vs. what it saves in future interest, and the
 * resulting balance change. The confirm button is disabled when the player
 * lacks sufficient funds.</p>
 *
 * <p>Business logic lives entirely in the callback supplied by the controller.
 * The dialog never touches {@code GameService} directly.</p>
 */
public class RepayLoanDialog extends Modal {

    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private final Loan loan;
    private final int loanIndex;
    private final Player player;
    private final int weeksElapsed;
    private final int weeksRemaining;

    private final Label errorLabel = new Label();
    private final Button confirmButton = new Button();
    private Consumer<Loan> confirmCallback;

    /**
     * @param loan        the loan the player wants to repay
     * @param loanIndex   per-type display index (e.g. 2 → "Standard loan #2")
     * @param player      the current player, used for balance checks
     * @param currentWeek the current game week, used to compute elapsed / remaining weeks
     */
    public RepayLoanDialog(Loan loan, int loanIndex, Player player, int currentWeek) {
        this.loan = loan;
        this.loanIndex = loanIndex;
        this.player = player;
        this.weeksElapsed = currentWeek - loan.takenAtWeek();
        this.weeksRemaining = Math.max(0, loan.offer().termWeeks() - weeksElapsed);
    }

    /**
     * Sets the callback invoked when the player confirms repayment.
     *
     * @param callback receives the loan being repaid
     */
    public void setOnConfirm(Consumer<Loan> callback) {
        this.confirmCallback = callback;
    }

    @Override
    protected Region buildContent() {
        String offerName = LanguageManager.get("loans.offer." + loan.offer().id() + ".name");
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeaderWithSubtitle(
                        LanguageManager.get("loans.repay.title"),
                        offerName + " #" + loanIndex),
                buildBody()
        );
        return content;
    }

    @Override
    protected void onBeforeShow() {
        updateConfirmState();
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }

    // -------------------------------------------------------------------------

    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");

        errorLabel.getStyleClass().add("modal-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        body.getChildren().addAll(
                buildInfoStrip(),
                buildSummary(),
                buildBalanceSection(),
                errorLabel,
                buildActions()
        );
        return body;
    }

    private Region buildInfoStrip() {
        BigDecimal weeklyRate = loan.offer().weeklyInterestRate()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        String weeksLeftText = MessageFormat.format(
                LanguageManager.get("loans.repay.info.weeksLeftValue"),
                weeksRemaining, loan.offer().termWeeks());

        VBox col1 = buildInfoColumn(
                LanguageManager.get("loans.repay.info.takenInWeek"),
                String.valueOf(loan.takenAtWeek()));
        VBox col2 = buildInfoColumn(
                LanguageManager.get("loans.repay.info.weeksLeft"),
                weeksLeftText);
        VBox col3 = buildInfoColumn(
                LanguageManager.get("loans.repay.info.weeklyInterest"),
                NUMBER_FORMAT.format(weeklyRate) + "%");

        HBox strip = new HBox(col1, col2, col3);
        strip.getStyleClass().add("modal-summary");
        for (Node child : strip.getChildren()) {
            HBox.setHgrow(child, Priority.ALWAYS);
            ((Region) child).setMaxWidth(Double.MAX_VALUE);
        }
        return strip;
    }

    private VBox buildInfoColumn(String label, String value) {
        return new VBox(4, StyledText.detailLabel(label), StyledText.detailValue(value));
    }

    private SummaryBox buildSummary() {
        BigDecimal weeklyInterest = loan.principal()
                .multiply(loan.offer().weeklyInterestRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal interestPaid = weeklyInterest
                .multiply(BigDecimal.valueOf(weeksElapsed))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal interestSaved = weeklyInterest
                .multiply(BigDecimal.valueOf(weeksRemaining))
                .setScale(2, RoundingMode.HALF_UP);

        SummaryBox summary = new SummaryBox();
        summary.addRow(
                LanguageManager.get("loans.repay.summary.remaining"),
                NUMBER_FORMAT.format(loan.principal()) + " NOK");
        summary.addRow(
                LanguageManager.get("loans.repay.summary.interestPaid"),
                NUMBER_FORMAT.format(interestPaid) + " NOK");
        summary.addRow(
                LanguageManager.get("loans.repay.summary.interestSaved"),
                NUMBER_FORMAT.format(interestSaved) + " NOK", "positive");
        summary.addTotal(
                LanguageManager.get("loans.repay.summary.toPay"),
                NUMBER_FORMAT.format(loan.principal()) + " NOK");
        return summary;
    }

    private SummaryBox buildBalanceSection() {
        BigDecimal available = player.getMoney();
        BigDecimal afterRepay = available.subtract(loan.principal());

        SummaryBox balance = new SummaryBox();
        balance.addRow(
                LanguageManager.get("loans.repay.balance.available"),
                NUMBER_FORMAT.format(available) + " NOK");
        balance.addTotal(
                LanguageManager.get("loans.repay.balance.after"),
                NUMBER_FORMAT.format(afterRepay) + " NOK");
        return balance;
    }

    private VBox buildActions() {
        Button cancel = new Button(LanguageManager.get("dialog.button.cancel"));
        cancel.getStyleClass().add("modal-button");
        cancel.setOnAction(e -> close());

        confirmButton.setText(LanguageManager.get("loans.repay.button.confirm"));
        confirmButton.getStyleClass().addAll("modal-button", "modal-button-primary");
        confirmButton.setOnAction(e -> onConfirm());

        return new VBox(ModalActions.row(cancel, confirmButton));
    }

    private void updateConfirmState() {
        boolean canAfford = player.getMoney().compareTo(loan.principal()) >= 0;
        confirmButton.setDisable(!canAfford);
        if (!canAfford) {
            errorLabel.setText(LanguageManager.get("loans.repay.error.insufficientFunds"));
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Displays an error message from the controller if domain validation fails.
     *
     * @param message the error text to display
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        confirmButton.setDisable(true);
        sizeToContent();
    }

    private void onConfirm() {
        if (confirmCallback != null) {
            confirmCallback.accept(loan);
        }
    }
}
