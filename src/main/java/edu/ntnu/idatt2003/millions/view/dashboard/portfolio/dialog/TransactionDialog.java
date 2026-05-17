package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StockHeader;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

    protected final TradeController controller;
    protected final Stock stock;

    protected final TextField quantityInput = new TextField();
    protected final SummaryBox summaryBox = new SummaryBox();
    protected final StyledText balanceAfterValue = StyledText.detailValue();
    protected final Label errorLabel = new Label();
    protected final Button confirmButton = new Button();
    private final VBox transactionInfoBox = new VBox(2);

    /**
     * Constructs a new TransactionDialog.
     *
     * @param stock      the stock involved in the transaction
     * @param controller the controller used for previews and balance lookup
     */
    protected TransactionDialog(Stock stock, TradeController controller) {
        this.stock = stock;
        this.controller = controller;
    }

    @Override
    protected javafx.scene.Node firstFocusTarget() {
        return quantityInput;
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
                transactionInfoBox,
                buildBalanceSection(),
                errorLabel,
                buildActions()
        );

        transactionInfoBox.getStyleClass().add("modal-info");
        transactionInfoBox.setVisible(false);
        transactionInfoBox.setManaged(false);

        errorLabel.getStyleClass().add("modal-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        return body;
    }

    private StockHeader buildStockSection() {
        return new StockHeader(stock, getStockHint());
    }

    /**
     * Builds the quantity input section. Subclasses can override to add
     * extras like a "Du eier"-info or a "Selg alt"-shortcut.
     *
     * <p>Pressing {@code Enter} in the input field fires {@code onConfirm()}
     * when the confirm button is enabled, so the user does not need to Tab
     * to the button.</p>
     */
    protected VBox buildQuantitySection() {
        StyledText label = StyledText.detailLabel(LanguageManager.get("dialog.quantity.label"));
        quantityInput.setText(getInitialQuantity().toPlainString());
        quantityInput.textProperty().addListener((obs, oldVal, newVal) -> updateSummary());
        quantityInput.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !confirmButton.isDisable()) {
                onConfirm();
                event.consume();
            }
        });
        return new VBox(6, label, buildStepperRow());
    }

    /**
     * Returns the [−] [input] [+] stepper group. Sets up CSS and grow
     * constraints on {@code quantityInput}; callers are responsible for
     * setting the initial text and change listener before or after.
     */
    protected HBox buildStepperRow() {
        quantityInput.getStyleClass().addAll("modal-input", "quantity-stepper-input");
        HBox.setHgrow(quantityInput, Priority.ALWAYS);

        Button dec = new Button("−");
        dec.getStyleClass().addAll("quantity-stepper-btn", "quantity-stepper-btn-dec");
        dec.setOnAction(e -> stepQuantity(-1));

        Button inc = new Button("+");
        inc.getStyleClass().addAll("quantity-stepper-btn", "quantity-stepper-btn-inc");
        inc.setOnAction(e -> stepQuantity(1));

        HBox group = new HBox(1, dec, quantityInput, inc);
        group.getStyleClass().add("quantity-stepper-group");
        return group;
    }

    private void stepQuantity(int delta) {
        BigDecimal current = getQuantity();
        BigDecimal next = (current == null ? BigDecimal.ZERO : current)
                .add(BigDecimal.valueOf(delta));
        if (next.compareTo(BigDecimal.ONE) < 0) {
            next = BigDecimal.ONE;
        }
        quantityInput.setText(next.stripTrailingZeros().toPlainString());
    }

    private VBox buildBalanceSection() {
        StyledText availableLabel = StyledText.detailLabel(LanguageManager.get("dialog.balance.available"));
        StyledText availableValue = StyledText.detailValue(
                NUMBER_FORMAT.format(controller.getCurrentBalance()) + " NOK");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox availableRow = new HBox(availableLabel, spacer1, availableValue);
        availableRow.getStyleClass().add("modal-balance-row");

        StyledText afterLabel = StyledText.detailLabel(getBalanceAfterLabel());
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

        return ModalActions.row(cancel, confirmButton);
    }

    /**
     * Returns the ISO currency code for the stock in this dialog (e.g. "USD", "EUR").
     * Used to label summary values in their native trading currency.
     */
    protected String currencyCode() {
        return stock.getCurrency().getCurrencyCode();
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
        sizeToContent();
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
        sizeToContent();
    }

    protected void setConfirmEnabled(boolean enabled) {
        confirmButton.setDisable(!enabled);
    }

    /**
     * Shows a profit/loss info box with label on the left and value(s) right-aligned.
     * Pass null for label to hide the box.
     *
     * @param label          leading text (left-aligned), or null to hide the box
     * @param primaryValue   main right-aligned value (e.g. "−22,28 USD (-8.0%)")
     * @param secondaryValue optional smaller right-aligned line (e.g. "= −205,23 NOK"); null omits it
     * @param positive       true for green coloring, false for red
     */
    protected void setTransactionInfo(String label, String primaryValue, String secondaryValue, boolean positive) {
        if (label == null) {
            transactionInfoBox.setVisible(false);
            transactionInfoBox.setManaged(false);
            sizeToContent();
            return;
        }
        String colorClass = positive ? "modal-info-positive" : "modal-info-negative";
        String valueClass = positive ? "positive" : "negative";

        transactionInfoBox.getStyleClass().removeAll("modal-info-positive", "modal-info-negative");
        transactionInfoBox.getStyleClass().add(colorClass);
        transactionInfoBox.getChildren().clear();

        Label labelNode = new Label(label);
        Label primaryNode = new Label(primaryValue);
        primaryNode.getStyleClass().add(valueClass);
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        transactionInfoBox.getChildren().add(new HBox(labelNode, spacer1, primaryNode));

        if (secondaryValue != null && !secondaryValue.isEmpty()) {
            Label secondaryNode = new Label(secondaryValue);
            secondaryNode.getStyleClass().addAll("modal-summary-conversion-text", valueClass);
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            transactionInfoBox.getChildren().add(new HBox(spacer2, secondaryNode));
        }

        transactionInfoBox.setVisible(true);
        transactionInfoBox.setManaged(true);
        sizeToContent();
    }

    /**
     * Recomputes summary rows and balance based on current quantity.
     * Subclasses must rebuild summaryBox contents here.
     */
    protected abstract void updateSummary();

    protected abstract String getTitle();

    protected abstract String getStockHint();

    /**
     * Returns the quantity to pre-fill in the input field when the dialog opens.
     * Defaults to zero so the user must enter a deliberate amount before Confirm
     * activates. Subclasses may override when a different default makes sense.
     *
     * @return the initial quantity; zero by default
     */
    protected BigDecimal getInitialQuantity() {
        return BigDecimal.ZERO;
    }

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