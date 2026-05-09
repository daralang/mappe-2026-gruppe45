package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.dialog;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

/**
 * Abstract base class for transaction dialogs (buy, sell, sell all).
 * Handles the modal structure, quantity input, summary updates,
 * and confirm/cancel actions. Subclasses define dialog-specific
 * details like title, summary rows, and confirmation logic.
 */
public abstract class TransactionDialog {

    protected static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    protected final PortfolioController controller;
    protected final Stock stock;
    protected final Stage stage = new Stage();

    protected final TextField quantityInput = new TextField();
    protected final VBox summaryBox = new VBox();
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

    /**
     * Builds and shows the dialog. Subclasses don't override this;
     * they override the abstract methods called from here.
     */
    public void show() {
        VBox card = new VBox();
        card.getStyleClass().add("modal-card");
        card.getChildren().addAll(buildHeader(), buildBody());

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(20));  // gir plass til drop-shadow

        Scene scene = new Scene(root);
        scene.setFill(null);  // gjør scenen transparent
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.setResizable(false);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                stage.close();
            }
        });

        updateSummary();
        stage.showAndWait();
    }

    /**
     * Closes the dialog. Called by the controller after a successful
     * transaction.
     */
    public void close() {
        stage.close();
    }

    private HBox buildHeader() {
        Label title = new Label(getTitle());
        title.getStyleClass().add("modal-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().add("modal-close");
        close.setOnAction(e -> stage.close());

        HBox header = new HBox(title, spacer, close);
        header.getStyleClass().add("modal-header");
        return header;
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
        summaryBox.getStyleClass().add("modal-summary");

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
        cancel.setOnAction(e -> stage.close());

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
     * Adds a row to the summary box. Use addSummaryTotal for the bold
     * bottom row with the divider.
     */
    protected void addSummaryRow(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("modal-summary-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("modal-summary-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(labelNode, spacer, valueNode);
        row.getStyleClass().add("modal-summary-row");
        summaryBox.getChildren().add(row);
    }

    protected void addSummaryTotal(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("modal-summary-total-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("modal-summary-total-value");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(labelNode, spacer, valueNode);
        row.getStyleClass().addAll("modal-summary-row", "modal-summary-total");
        summaryBox.getChildren().add(row);
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