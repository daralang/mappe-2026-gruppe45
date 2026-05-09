package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Abstract base class for transaction dialogs (buy, sell, sell all).
 * Builds the input form, summary, and confirm/cancel actions on top of
 * the {@link Modal} infrastructure. Subclasses define the title, summary
 * rows, and confirmation logic.
 */
public abstract class TransactionDialog extends Modal {

    protected static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    protected final PortfolioController controller;
    protected final Stock stock;

    protected final TextField quantityInput = new TextField();
    protected final SummaryBox summaryBox = new SummaryBox();
    protected final Label balanceAfterValue = new Label();
    protected final Label errorLabel = new Label();
    protected final Button confirmButton = new Button();
    protected final Label transactionInfoLabel = new Label();

    /**
     * Constructs a new TransactionDialog.
     *
     * @param stock      the stock involved in the transaction
     * @param controller the controller used for previews and balance lookup
     */
    protected TransactionDialog(Stock stock, PortfolioController controller) {
        this.stock = stock;
        this.controller = controller;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(getTitle()),
                buildBody()
        );
        return content;
    }

    @Override
    protected void onBeforeShow() {
        updateSummary();
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }

    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");

        body.getChildren().addAll(
                buildStockSection(),
                buildQuantitySection(),
                summaryBox,
                transactionInfoLabel,
                buildBalanceSection(),
                errorLabel,
                buildActions()
        );

        transactionInfoLabel.getStyleClass().add("modal-info");
        transactionInfoLabel.setVisible(false);
        transactionInfoLabel.setManaged(false);

        errorLabel.getStyleClass().add("modal-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        return body;
    }

    private VBox buildStockSection() {
        Label label = new Label(LanguageManager.get("dialog.stock.label"));
        label.getStyleClass().add("modal-section-label");

        Label value = new Label(stock.getSymbol() + ", " + stock.getCompany());
        value.getStyleClass().add("modal-section-value");

        Label hint = new Label(getStockHint());
        hint.getStyleClass().add("modal-section-hint");

        return new VBox(4, label, value, hint);
    }

    /**
     * Builds the quantity input section. Subclasses can override to add
     * extras like a "Du eier"-info or a "Selg alt"-shortcut.
     */
    protected VBox buildQuantitySection() {
        Label label = new Label(LanguageManager.get("dialog.quantity.label"));
        label.getStyleClass().add("modal-section-label");

        quantityInput.getStyleClass().add("modal-input");
        quantityInput.setText(getInitialQuantity().toPlainString());
        quantityInput.textProperty().addListener((obs, oldVal, newVal) -> updateSummary());

        return new VBox(6, label, quantityInput);
    }

    private VBox buildBalanceSection() {
        Label availableLabel = new Label(LanguageManager.get("dialog.balance.available"));
        availableLabel.getStyleClass().add("modal-balance-label");
        Label availableValue = new Label(
                NUMBER_FORMAT.format(controller.getCurrentBalance()) + " NOK");
        availableValue.getStyleClass().add("modal-balance-value");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox availableRow = new HBox(availableLabel, spacer1, availableValue);
        availableRow.getStyleClass().add("modal-balance-row");

        Label afterLabel = new Label(getBalanceAfterLabel());
        afterLabel.getStyleClass().add("modal-balance-label");
        balanceAfterValue.getStyleClass().addAll("modal-balance-value", "positive");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        HBox afterRow = new HBox(afterLabel, spacer2, balanceAfterValue);
        afterRow.getStyleClass().add("modal-balance-row");

        return new VBox(4, availableRow, afterRow);
    }

    private HBox buildActions() {
        Button cancel = new Button(LanguageManager.get("dialog.button.cancel"));
        cancel.getStyleClass().add("modal-button");
        cancel.setOnAction(e -> close());

        confirmButton.setText(getConfirmButtonText());
        confirmButton.getStyleClass().addAll("modal-button", getConfirmButtonStyleClass());
        confirmButton.setOnAction(e -> onConfirm());

        HBox.setHgrow(cancel, Priority.ALWAYS);
        HBox.setHgrow(confirmButton, Priority.ALWAYS);
        cancel.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setMaxWidth(Double.MAX_VALUE);

        HBox actions = new HBox(cancel, confirmButton);
        actions.getStyleClass().add("modal-actions");
        return actions;
    }

    /**
     * Parses the current quantity input. Returns null on invalid input.
     */
    protected BigDecimal getQuantity() {
        try {
            String text = quantityInput.getText().trim().replace(',', '.');
            if (text.isEmpty()) {
                return null;
            }
            BigDecimal value = new BigDecimal(text);
            if (value.signum() <= 0) {
                return null;
            }
            return value;
        } catch (NumberFormatException _) {
            return null;
        }
    }

    protected void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Displays an error message in the dialog. Called by the controller
     * when a transaction fails, or by the dialog itself for UI validation.
     *
     * @param message the error message to display
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    protected void setConfirmEnabled(boolean enabled) {
        confirmButton.setDisable(!enabled);
    }

    /**
     * Sets an informational message shown between the summary box and
     * the balance section. Pass null or empty to hide.
     *
     * @param message  the message text, or null/empty to hide
     * @param positive true for green styling, false for red
     */
    protected void setTransactionInfo(String message, boolean positive) {
        if (message == null || message.isEmpty()) {
            transactionInfoLabel.setVisible(false);
            transactionInfoLabel.setManaged(false);
            return;
        }
        transactionInfoLabel.setText(message);
        transactionInfoLabel.getStyleClass().removeAll(
                "modal-info-positive", "modal-info-negative"
        );
        transactionInfoLabel.getStyleClass().add(
                positive ? "modal-info-positive" : "modal-info-negative"
        );
        transactionInfoLabel.setVisible(true);
        transactionInfoLabel.setManaged(true);
    }

    /**
     * Recomputes summary rows and balance based on current quantity.
     * Subclasses must rebuild summaryBox contents here.
     */
    protected abstract void updateSummary();

    protected abstract String getTitle();

    protected abstract String getStockHint();

    protected abstract BigDecimal getInitialQuantity();

    protected abstract String getBalanceAfterLabel();

    protected abstract String getConfirmButtonText();

    protected abstract String getConfirmButtonStyleClass();

    /**
     * Invoked when the user clicks the confirm button. Subclasses
     * should call their registered callback. Errors from the model
     * are surfaced via {@link #showError(String)} by the controller.
     */
    protected abstract void onConfirm();
}