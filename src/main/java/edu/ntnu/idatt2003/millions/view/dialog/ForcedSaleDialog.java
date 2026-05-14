package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.model.calculator.SalesCalculator;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.ColourChange;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.SummaryBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Modal dialog shown when the player cannot cover weekly loan interest from cash.
 * Displays the interest summary, the player's holdings sorted worst-first, and
 * lets the player select which shares to sell. Cannot be dismissed without confirming.
 */
public class ForcedSaleDialog extends Modal {

    private final List<Share> shares;
    private final BigDecimal interestDue;
    private final BigDecimal maturityDue;
    private final BigDecimal totalObligations;
    private final BigDecimal availableCash;
    private final CurrencyConverter converter;
    private final int currentWeek;
    private final Consumer<List<Share>> onConfirm;

    private final Map<Share, BigDecimal> netNokByShare = new LinkedHashMap<>();
    private final List<Share> selectedShares = new ArrayList<>();

    private Label selectedTotalLabel;
    private Label statusLabel;
    private Button confirmBtn;

    public ForcedSaleDialog(List<Share> shares,
                            BigDecimal interestDue,
                            BigDecimal maturityDue,
                            BigDecimal availableCash,
                            CurrencyConverter converter,
                            int currentWeek,
                            Consumer<List<Share>> onConfirm) {
        this.shares = shares;
        this.interestDue = interestDue;
        this.maturityDue = maturityDue;
        this.totalObligations = interestDue.add(maturityDue);
        this.availableCash = availableCash;
        this.converter = converter;
        this.currentWeek = currentWeek;
        this.onConfirm = onConfirm;

        for (Share share : shares) {
            netNokByShare.put(share, SalesCalculator.calculateNetNok(share, converter));
        }
    }

    /**
     * Blocked — this dialog cannot be closed by the user. Call {@link #closeOnConfirm()} instead.
     */
    @Override
    public void close() {}

    public void closeOnConfirm() {
        stage.close();
    }

    public void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("positive", "negative");
        statusLabel.getStyleClass().add("negative");
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(buildHeader(), buildBody());
        return content;
    }

    private VBox buildHeader() {
        Label iconLabel = new Label("⚠");
        iconLabel.getStyleClass().add("forced-sale-warning-icon");

        Label titleLabel = new Label(LanguageManager.get("forcedSale.title"));
        titleLabel.getStyleClass().add("modal-title");

        HBox titleRow = new HBox(10, iconLabel, titleLabel);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        String subtitleText = MessageFormat.format(
                LanguageManager.get("forcedSale.subtitle"), currentWeek);
        Label subtitleLabel = new Label(subtitleText);
        subtitleLabel.getStyleClass().add("detail-label");

        VBox header = new VBox(4, titleRow, subtitleLabel);
        header.getStyleClass().add("forced-sale-header");
        return header;
    }

    private VBox buildBody() {
        VBox body = new VBox(16);
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(
                buildObligationsSummaryBox(),
                buildHoldingsSection(),
                buildStatusSection()
        );
        return body;
    }

    private SummaryBox buildObligationsSummaryBox() {
        SummaryBox box = new SummaryBox();
        box.setSectionTitle(LanguageManager.get("forcedSale.obligations.header"));

        if (interestDue.signum() > 0) {
            box.addRow(LanguageManager.get("forcedSale.obligations.interest"),
                    CurrencyFormatter.format(interestDue));
        }
        if (maturityDue.signum() > 0) {
            box.addRow(LanguageManager.get("forcedSale.obligations.maturity"),
                    CurrencyFormatter.format(maturityDue));
        }
        box.addRow(LanguageManager.get("forcedSale.obligations.total"),
                CurrencyFormatter.format(totalObligations));
        box.addRow(LanguageManager.get("forcedSale.obligations.availableCash"),
                CurrencyFormatter.format(availableCash));

        BigDecimal shortfall = totalObligations.subtract(availableCash).max(BigDecimal.ZERO);
        box.addRow(LanguageManager.get("forcedSale.obligations.toBeCovered"),
                CurrencyFormatter.format(shortfall), "negative");

        return box;
    }

    private VBox buildHoldingsSection() {
        Label holdingsHeader = StyledText.detailLabel(LanguageManager.get("forcedSale.holdings.header"));
        holdingsHeader.getStyleClass().add("modal-summary-section-title");

        VBox rows = new VBox(0);
        for (Share share : shares) {
            rows.getChildren().add(buildHoldingRow(share));
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.getStyleClass().add("content-scroll");
        scroll.setMaxHeight(240);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return new VBox(12, holdingsHeader, scroll);
    }

    private HBox buildHoldingRow(Share share) {
        BigDecimal netNok = netNokByShare.get(share);
        BigDecimal price = share.getStock().getSalesPrice();
        BigDecimal returnPct = share.getReturnPercent();
        BigDecimal weekChangePct = share.getStock().getWeeklyChangePercent();
        String currencyCode = share.getStock().getCurrency().getCurrencyCode();

        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("forced-sale-checkbox");
        checkbox.setFocusTraversable(false);

        // Left column: stock-specific data
        Label symbolLine = StyledText.detailValue(
                share.getStock().getSymbol() + " — " + share.getStock().getCompany());

        String priceText = MessageFormat.format(
                LanguageManager.get("forcedSale.holdings.pricePerShare"),
                ChangeFormatter.formatPlain(price) + " " + currencyCode);
        Label priceLine = StyledText.detailLabel(priceText);

        String arrow = weekChangePct.compareTo(BigDecimal.ZERO) >= 0 ? "↑ " : "↓ ";
        String weekText = MessageFormat.format(
                LanguageManager.get("forcedSale.holdings.weekChange"),
                arrow + ChangeFormatter.formatSignedPercent(weekChangePct));
        Label weekLine = StyledText.detailLabel(weekText);
        ColourChange.applyChangeStyle(weekLine, weekChangePct);

        VBox leftCol = new VBox(2, symbolLine, priceLine, weekLine);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        // Right column: player's position data, right-aligned
        String qtyAndValue = MessageFormat.format(
                LanguageManager.get("forcedSale.holdings.quantityAndValue"),
                ChangeFormatter.formatPlain(share.getQuantity()),
                CurrencyFormatter.format(netNok));
        Label qtyValueLine = StyledText.detailValue(qtyAndValue);

        Label returnHeaderLine = StyledText.detailLabel(
                LanguageManager.get("forcedSale.holdings.yourReturn"));

        Label returnPctLine = StyledText.detailValue(
                ChangeFormatter.formatSignedPercent(returnPct));
        ColourChange.applyChangeStyle(returnPctLine, returnPct);

        VBox rightCol = new VBox(2, qtyValueLine, returnHeaderLine, returnPctLine);
        rightCol.setAlignment(Pos.TOP_RIGHT);

        HBox row = new HBox(10, checkbox, leftCol, rightCol);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("forced-sale-holding-row");

        Runnable toggle = () -> {
            if (selectedShares.contains(share)) {
                selectedShares.remove(share);
                checkbox.setSelected(false);
                row.getStyleClass().remove("selected");
            } else {
                selectedShares.add(share);
                checkbox.setSelected(true);
                if (!row.getStyleClass().contains("selected")) {
                    row.getStyleClass().add("selected");
                }
            }
            refreshStatus();
        };

        row.setOnMouseClicked(e -> toggle.run());
        checkbox.setOnAction(e -> toggle.run());

        return row;
    }

    private VBox buildStatusSection() {
        selectedTotalLabel = StyledText.detailValue(
                LanguageManager.get("forcedSale.selected") + ": " + CurrencyFormatter.format(BigDecimal.ZERO));

        statusLabel = StyledText.detailLabel();

        confirmBtn = new Button(LanguageManager.get("forcedSale.confirm"));
        confirmBtn.getStyleClass().addAll("modal-button", "modal-button-primary");
        confirmBtn.setDisable(true);
        confirmBtn.setMaxWidth(Double.MAX_VALUE);
        confirmBtn.setOnAction(e -> onConfirm.accept(new ArrayList<>(selectedShares)));

        return new VBox(8, selectedTotalLabel, statusLabel, confirmBtn);
    }

    private void refreshStatus() {
        BigDecimal selectedTotal = selectedShares.stream()
                .map(netNokByShare::get)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        selectedTotalLabel.setText(
                LanguageManager.get("forcedSale.selected") + ": " + CurrencyFormatter.format(selectedTotal));

        int cmp = selectedTotal.compareTo(totalObligations);
        if (cmp < 0) {
            BigDecimal missing = totalObligations.subtract(selectedTotal).setScale(2, RoundingMode.HALF_UP);
            statusLabel.setText(MessageFormat.format(
                    LanguageManager.get("forcedSale.status.short"),
                    ChangeFormatter.formatPlain(missing)));
            statusLabel.getStyleClass().removeAll("positive", "negative");
            statusLabel.getStyleClass().add("negative");
            confirmBtn.setDisable(true);
        } else if (cmp == 0) {
            statusLabel.setText(LanguageManager.get("forcedSale.status.exact"));
            statusLabel.getStyleClass().removeAll("positive", "negative");
            statusLabel.getStyleClass().add("positive");
            confirmBtn.setDisable(false);
        } else {
            BigDecimal surplus = selectedTotal.subtract(totalObligations).setScale(2, RoundingMode.HALF_UP);
            statusLabel.setText(MessageFormat.format(
                    LanguageManager.get("forcedSale.status.over"),
                    ChangeFormatter.formatPlain(surplus)));
            statusLabel.getStyleClass().removeAll("positive", "negative");
            statusLabel.getStyleClass().add("positive");
            confirmBtn.setDisable(false);
        }
    }
}
