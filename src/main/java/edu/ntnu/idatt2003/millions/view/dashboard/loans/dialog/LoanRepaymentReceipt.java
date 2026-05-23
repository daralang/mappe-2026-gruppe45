package edu.ntnu.idatt2003.millions.view.dashboard.loans.dialog;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import edu.ntnu.idatt2003.millions.util.MoneyFormatter;

import java.math.BigDecimal;

/**
 * Receipt shown after a successful loan repayment.
 */
public class LoanRepaymentReceipt extends Modal {

    private final Loan loan;
    private final int loanIndex;
    private final BigDecimal balanceBefore;
    private final BigDecimal balanceAfter;
    private final int currentWeek;

    public LoanRepaymentReceipt(
            Loan loan,
            int loanIndex,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            int currentWeek) {
        this.loan = loan;
        this.loanIndex = loanIndex;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.currentWeek = currentWeek;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(buildHeader(), buildBody());
        return content;
    }

    private HBox buildHeader() {
        Label icon = new Label();
        icon.setGraphic(new FontIcon("fth-check"));
        icon.getStyleClass().add("modal-success-icon");

        Label title = new Label(LanguageManager.get("receipt.loan.repay.title"));
        title.getStyleClass().add("modal-title");

        HBox titleGroup = new HBox(12, icon, title);
        titleGroup.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().add("modal-close");
        close.setOnAction(e -> close());

        HBox header = new HBox(titleGroup, spacer, close);
        header.getStyleClass().addAll("modal-header", "modal-header-success");
        return header;
    }

    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(
                buildLoanSection(),
                buildSummary(),
                buildBalanceSection(),
                buildActions()
        );
        return body;
    }

    private VBox buildLoanSection() {
        StyledText label = StyledText.detailLabel(LanguageManager.get("receipt.loan.label"));

        String offerName = LanguageManager.get("loans.offer." + loan.offer().id() + ".name");
        Label value = new Label(offerName + " #" + loanIndex);
        value.getStyleClass().add("modal-section-value");

        return new VBox(4, label, value);
    }

    private SummaryBox buildSummary() {
        SummaryBox summary = new SummaryBox();
        summary.addRow(
                LanguageManager.get("receipt.loan.repay.summary.amountPaid"),
                MoneyFormatter.format(loan.principal()) + " NOK");
        summary.addRow(
                LanguageManager.get("receipt.loan.repay.summary.interestSaved"),
                MoneyFormatter.format(loan.interestSaved(currentWeek)) + " NOK",
                "positive");
        return summary;
    }

    private VBox buildBalanceSection() {
        StyledText beforeLabel = StyledText.detailLabel(LanguageManager.get("receipt.balance.before"));
        StyledText beforeValue = StyledText.detailValue(MoneyFormatter.format(balanceBefore) + " NOK");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox beforeRow = new HBox(beforeLabel, spacer1, beforeValue);
        beforeRow.getStyleClass().add("modal-balance-row");

        StyledText afterLabel = StyledText.detailLabel(LanguageManager.get("receipt.balance.after"));
        StyledText afterValue = StyledText.detailValue(MoneyFormatter.format(balanceAfter) + " NOK");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox afterRow = new HBox(afterLabel, spacer2, afterValue);
        afterRow.getStyleClass().add("modal-balance-row");

        return new VBox(4, beforeRow, afterRow);
    }

    private HBox buildActions() {
        Button closeButton = new Button(LanguageManager.get("receipt.button.close"));
        closeButton.getStyleClass().add("modal-button");
        closeButton.setOnAction(e -> close());
        closeButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(closeButton, Priority.ALWAYS);

        HBox actions = new HBox(closeButton);
        actions.getStyleClass().add("modal-actions");
        return actions;
    }
}
