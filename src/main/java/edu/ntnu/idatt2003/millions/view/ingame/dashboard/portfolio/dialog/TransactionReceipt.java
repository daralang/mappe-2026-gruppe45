package edu.ntnu.idatt2003.millions.view.ingame.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.SummaryBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import edu.ntnu.idatt2003.millions.util.MoneyFormatter;

import java.math.BigDecimal;

/**
 * Abstract base class for transaction receipts (buy, sell).
 * Shows a confirmation receipt after a successful transaction with the
 * transaction details, computed values, and the resulting balance.
 *
 * <p>Built on top of the {@link Modal} infrastructure. Subclasses define
 * the title, the price label, the summary rows specific to the
 * transaction type, and any extra content (e.g. profit or loss for sales).</p>
 */
public abstract class TransactionReceipt extends Modal {

    protected final Transaction transaction;
    protected final BigDecimal balanceBefore;
    protected final BigDecimal balanceAfter;

    protected final SummaryBox summaryBox = new SummaryBox();

    /**
     * Constructs a new TransactionReceipt.
     *
     * @param transaction   the completed transaction to display
     * @param balanceBefore the player's balance before the transaction
     * @param balanceAfter  the player's balance after the transaction
     */
    protected TransactionReceipt(
            Transaction transaction,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        this.transaction = transaction;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(buildHeader(), buildBody());
        return content;
    }

    @Override
    protected void onBeforeShow() {
        renderSummary();
    }

    private HBox buildHeader() {
        Label icon = new Label();
        icon.setGraphic(new FontIcon("fth-check"));
        icon.getStyleClass().add("modal-success-icon");

        Label title = new Label(getTitle());
        title.getStyleClass().add("modal-title");

        HBox titleGroup = new HBox(12, icon, title);
        titleGroup.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("\u2715");
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
                buildStockSection(),
                buildMetaSection(),
                summaryBox,
                buildExtraContent(),
                buildBalanceSection(),
                buildActions()
        );

        return body;
    }

    private VBox buildStockSection() {
        StyledText label = StyledText.detailLabel(LanguageManager.get("receipt.stock.label"));

        Label value = new Label(
                transaction.getShare().getStock().getSymbol() + ", "
                        + transaction.getShare().getStock().getCompany());
        value.getStyleClass().add("modal-section-value");

        return new VBox(4, label, value);
    }

    /**
     * Builds the meta info row (quantity, price, week) shown
     * directly below the stock section.
     */
    private HBox buildMetaSection() {
        HBox row = new HBox(24);
        row.getStyleClass().add("modal-meta-row");
        row.getChildren().addAll(
                buildMetaCell(LanguageManager.get("receipt.meta.quantity"),
                        MoneyFormatter.format(transaction.getShare().getQuantity())),
                buildMetaCell(getPriceLabel(),
                        MoneyFormatter.format(getPrice()) + " " + currencyCode()),
                buildMetaCell(LanguageManager.get("receipt.meta.week"),
                        String.valueOf(transaction.getWeek()))
        );
        return row;
    }

    private VBox buildMetaCell(String label, String value) {
        StyledText labelNode = StyledText.detailLabel(label);
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("modal-section-value");
        return new VBox(4, labelNode, valueNode);
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

    protected String currencyCode() {
        return transaction.getShare().getStock().getCurrency().getCurrencyCode();
    }

    // ---- Subclass hooks ----

    protected abstract String getTitle();

    protected abstract String getPriceLabel();

    protected abstract BigDecimal getPrice();

    protected abstract void renderSummary();

    /**
     * Optional extra content shown between the summary and the balance
     * section. Used by {@code SellReceipt} for the profit/loss box.
     * Default returns an empty container.
     */
    protected VBox buildExtraContent() {
        return new VBox();
    }
}