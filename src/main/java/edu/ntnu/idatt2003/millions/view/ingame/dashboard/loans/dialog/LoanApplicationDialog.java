package edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.dialog;

import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanPreview;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.Modal;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.SummaryBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import edu.ntnu.idatt2003.millions.util.currency.MoneyFormatter;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Extends Modal directly rather than TransactionDialog.
 * TransactionDialog is built around a Stock, a PortfolioController, and a
 * shares-quantity stepper — five abstract methods have no loan equivalent.
 * Composing from Modal directly with the same shared components (SummaryBox,
 * ModalActions, buildStandardHeader) is more honest than stubbing them out.
 */
/**
 * Modal dialog for applying for a loan against a {@link LoanOffer}.
 *
 * <p>The dialog shows the offer's static parameters at the top, lets the
 * player choose a principal via a slider and text field, and updates the
 * repayment summary live. Validation (max amount, eligibility) is enforced
 * in real time; the confirm button is disabled until all checks pass.</p>
 *
 * <p>Business logic lives entirely in the callbacks supplied by the controller.
 * The dialog never touches {@code GameService} directly.</p>
 */
public class LoanApplicationDialog extends Modal {

    private final LoanOffer offer;
    private final Function<BigDecimal, Optional<String>> validateCallback;
    private final Function<BigDecimal, LoanPreview> previewCallback;
    private final Consumer<BigDecimal> confirmCallback;

    private final SummaryBox summaryBox = new SummaryBox();
    private final Label errorLabel = new Label();
    private final Button confirmButton = new Button();
    private final Slider amountSlider = new Slider();
    private final TextField amountField = new TextField();
    private boolean updatingAmount = false;

    /**
     * @param offer            the loan product the player is applying for
     * @param validateCallback game-policy validation: returns empty if allowed,
     *                         or an i18n error key if the amount is not permitted
     * @param previewCallback  pure calculation callback: principal → {@link LoanPreview}
     * @param confirmCallback  callback invoked with the chosen principal when confirmed
     */
    public LoanApplicationDialog(LoanOffer offer,
                                 Function<BigDecimal, Optional<String>> validateCallback,
                                 Function<BigDecimal, LoanPreview> previewCallback,
                                 Consumer<BigDecimal> confirmCallback) {
        this.offer = offer;
        this.validateCallback = validateCallback;
        this.previewCallback = previewCallback;
        this.confirmCallback = confirmCallback;
    }

    @Override
    protected javafx.scene.Node firstFocusTarget() {
        return amountField;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("loans.dialog.title")),
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

        errorLabel.getStyleClass().add("modal-error");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        body.getChildren().addAll(
                buildOfferInfo(),
                buildAmountSection(),
                summaryBox,
                buildWarningBox(),
                errorLabel,
                buildActions()
        );
        return body;
    }

    private SummaryBox buildOfferInfo() {
        SummaryBox info = new SummaryBox();
        info.setSectionTitle(LanguageManager.get("loans.offer." + offer.id() + ".name"));
        info.addRow(LanguageManager.get("loans.offer.rate"),
                MoneyFormatter.format(
                        offer.weeklyInterestRate().multiply(BigDecimal.valueOf(100))) + "%");
        info.addRow(LanguageManager.get("loans.offer.term"),
                MessageFormat.format(LanguageManager.get("loans.offer.weeks"), offer.termWeeks()));
        info.addRow(LanguageManager.get("loans.offer.maxAmount"),
                MoneyFormatter.format(offer.maxPrincipal()) + " NOK");
        return info;
    }

    /**
     * Builds the amount input section with a slider and text field kept in sync.
     *
     * <p>Pressing {@code Enter} in the text field fires {@code onConfirm()} when
     * the confirm button is enabled.</p>
     */
    private VBox buildAmountSection() {
        StyledText labelLeft = StyledText.detailLabel(LanguageManager.get("loans.dialog.amount.label"));
        StyledText labelRight = StyledText.detailLabel(
                MessageFormat.format(
                        LanguageManager.get("loans.dialog.amount.range"),
                        MoneyFormatter.format(BigDecimal.ZERO),
                        MoneyFormatter.format(offer.maxPrincipal())));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox labelRow = new HBox(labelLeft, spacer, labelRight);

        amountSlider.getStyleClass().add("loan-amount-slider");
        amountSlider.setMin(0);
        amountSlider.setMax(offer.maxPrincipal().doubleValue());
        amountSlider.setValue(0);
        amountSlider.setMajorTickUnit(1000);
        amountSlider.setMinorTickCount(0);
        amountSlider.setBlockIncrement(1000);
        amountSlider.setSnapToTicks(true);
        amountSlider.setShowTickMarks(false);
        amountSlider.setShowTickLabels(false);
        amountSlider.setMaxWidth(Double.MAX_VALUE);

        // Custom track overlay: two Regions placed behind the transparent slider track
        Region trackEmpty = new Region();
        trackEmpty.getStyleClass().add("loan-amount-slider-track-empty");
        trackEmpty.setMouseTransparent(true);
        trackEmpty.setMaxHeight(4);

        Region trackFill = new Region();
        trackFill.getStyleClass().add("loan-amount-slider-track-fill");
        trackFill.setMouseTransparent(true);
        trackFill.setMaxHeight(4);
        // USE_PREF_SIZE (-1) prevents StackPane from stretching fill beyond its bound prefWidth
        trackFill.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(trackFill, Pos.CENTER_LEFT);
        trackFill.prefWidthProperty().bind(
                amountSlider.widthProperty()
                        .multiply(amountSlider.valueProperty().subtract(amountSlider.getMin()))
                        .divide(amountSlider.getMax() - amountSlider.getMin())
        );

        amountField.getStyleClass().add("modal-input");
        amountField.setText("0");
        amountField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER && !confirmButton.isDisable()) {
                onConfirm();
                event.consume();
            }
        });

        amountSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingAmount) return;
            updatingAmount = true;
            long rounded = Math.round(newVal.doubleValue() / 1000.0) * 1000;
            amountField.setText(String.valueOf(rounded));
            updatingAmount = false;
            updateSummary();
        });

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingAmount) return;
            updatingAmount = true;
            try {
                BigDecimal val = new BigDecimal(
                        newVal.trim().replace(",", ".").replace(" ", "").replace(" ", ""));
                if (val.signum() >= 0 && val.compareTo(offer.maxPrincipal()) <= 0) {
                    amountSlider.setValue(val.doubleValue());
                }
            } catch (NumberFormatException ignored) { }
            updatingAmount = false;
            updateSummary();
        });

        StackPane sliderWrapper = new StackPane(trackEmpty, trackFill, amountSlider);
        sliderWrapper.setMaxWidth(Double.MAX_VALUE);

        return new VBox(6, labelRow, amountField, sliderWrapper);
    }

    private VBox buildWarningBox() {
        StyledText warning = StyledText.detailLabel(LanguageManager.get("loans.dialog.warning"));
        warning.setWrapText(true);
        VBox box = new VBox(warning);
        box.getStyleClass().addAll("modal-info", "modal-info-warning");
        return box;
    }

    private VBox buildActions() {
        Button cancel = new Button(LanguageManager.get("dialog.button.cancel"));
        cancel.getStyleClass().add("modal-button");
        cancel.setOnAction(e -> close());

        confirmButton.setText(LanguageManager.get("loans.dialog.button.confirm"));
        confirmButton.getStyleClass().addAll("modal-button", "modal-button-primary");
        confirmButton.setOnAction(e -> onConfirm());

        return new VBox(ModalActions.row(cancel, confirmButton));
    }

    // -------------------------------------------------------------------------

    private void updateSummary() {
        summaryBox.clear();
        hideError();

        BigDecimal amount = getAmount();
        if (amount == null) {
            renderEmptySummary();
            setConfirmEnabled(false);
            return;
        }

        if (amount.compareTo(offer.maxPrincipal()) > 0) {
            renderEmptySummary();
            showError(LanguageManager.get("loans.dialog.error.exceedsMax"));
            setConfirmEnabled(false);
            return;
        }

        Optional<String> policyError = validateCallback.apply(amount);
        if (policyError.isPresent()) {
            renderEmptySummary();
            showError(LanguageManager.get(policyError.get()));
            setConfirmEnabled(false);
            return;
        }

        LoanPreview preview = previewCallback.apply(amount);
        renderSummary(preview);
        setConfirmEnabled(true);
    }

    private void renderSummary(LoanPreview preview) {
        summaryBox.addRow(
                LanguageManager.get("loans.dialog.summary.disbursed"),
                MoneyFormatter.format(preview.principal()) + " NOK");
        summaryBox.addRow(
                LanguageManager.get("loans.dialog.summary.weeklyInterest"),
                MoneyFormatter.format(preview.weeklyInterestAmount()) + " NOK");
        summaryBox.addRow(
                MessageFormat.format(
                        LanguageManager.get("loans.dialog.summary.totalInterest"), offer.termWeeks()),
                MoneyFormatter.format(preview.totalInterest()) + " NOK");
        summaryBox.addTotal(
                LanguageManager.get("loans.dialog.summary.totalRepayment"),
                MoneyFormatter.format(preview.totalRepayment()) + " NOK");
    }

    private void renderEmptySummary() {
        String zero = MoneyFormatter.format(BigDecimal.ZERO) + " NOK";
        summaryBox.addRow(LanguageManager.get("loans.dialog.summary.disbursed"), zero);
        summaryBox.addRow(LanguageManager.get("loans.dialog.summary.weeklyInterest"), zero);
        summaryBox.addRow(
                MessageFormat.format(
                        LanguageManager.get("loans.dialog.summary.totalInterest"), offer.termWeeks()), zero);
        summaryBox.addTotal(LanguageManager.get("loans.dialog.summary.totalRepayment"), zero);
    }

    private BigDecimal getAmount() {
        try {
            String text = amountField.getText()
                    .trim().replace(",", ".").replace(" ", "").replace(" ", "");
            if (text.isEmpty()) return null;
            BigDecimal val = new BigDecimal(text);
            return val.signum() > 0 ? val : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void onConfirm() {
        BigDecimal amount = getAmount();
        if (amount == null) return;
        if (confirmCallback != null) {
            confirmCallback.accept(amount);
        }
    }

    /**
     * Displays an error message and disables the confirm button.
     * Called by the controller if the domain layer rejects the transaction.
     *
     * @param message the error text to display
     */
    public void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        sizeToContent();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        sizeToContent();
    }

    private void setConfirmEnabled(boolean enabled) {
        confirmButton.setDisable(!enabled);
    }
}
