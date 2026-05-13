package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.model.loan.LoanCatalog;
import edu.ntnu.idatt2003.millions.model.loan.LoanOffer;
import edu.ntnu.idatt2003.millions.model.loan.LoanRiskLevel;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Card displaying all available loan offers from {@link LoanCatalog}.
 * Three offer nodes are shown side-by-side in equal-width columns.
 *
 * <p>The selected offer is highlighted with a filled blue apply button and
 * a blue border around its box. LEFT/RIGHT arrow keys move the selection;
 * ENTER fires the selected offer's apply action.</p>
 *
 * <p>Wire up the apply callback via {@link #setOnApplyClicked(Consumer)}
 * after construction; the controller hooks in there.</p>
 */
public class AvailableLoansCard extends Card {

    private static final int OFFER_COUNT = 3;

    private final StyledText title = StyledText.sectionTitle();
    private final GridPane offersGrid = new GridPane();

    private final List<VBox> offerNodes = new ArrayList<>();
    private final List<Button> applyButtons = new ArrayList<>();
    private int selectedIndex = 0;

    private Consumer<LoanOffer> onApplyClicked;

    private final EventHandler<KeyEvent> keyHandler = this::handleKeyPress;

    public AvailableLoansCard(GameService gameService) {
        super(gameService);
        setSpacing(16);

        offersGrid.setHgap(16);
        for (int i = 0; i < OFFER_COUNT; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / OFFER_COUNT);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            offersGrid.getColumnConstraints().add(col);
        }

        getChildren().addAll(title, offersGrid);
        refresh();

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            if (newScene != null) newScene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        });
    }

    /**
     * Sets the callback invoked when the player clicks "Søk om lån" on an offer.
     * The offer that was clicked is passed as the argument.
     */
    public void setOnApplyClicked(Consumer<LoanOffer> callback) {
        this.onApplyClicked = callback;
    }

    @Override
    public void onGameUpdated() {
        // Loan catalog is static — no response to game-state changes needed
    }

    @Override
    protected void onLanguageChanged() {
        refresh();
    }

    private void refresh() {
        title.setText(LanguageManager.get("dashboard.loans.available.title"));
        offersGrid.getChildren().clear();
        offerNodes.clear();
        applyButtons.clear();

        List<LoanOffer> offers = LoanCatalog.getOffers();
        for (int i = 0; i < offers.size(); i++) {
            VBox node = buildOfferNode(offers.get(i), i);
            node.setMaxWidth(Double.MAX_VALUE);
            node.setMaxHeight(Double.MAX_VALUE);
            offerNodes.add(node);
            offersGrid.add(node, i, 0);
        }

        selectedIndex = Math.min(selectedIndex, offerNodes.size() - 1);
        select(selectedIndex);
    }

    private VBox buildOfferNode(LoanOffer offer, int index) {
        VBox box = new VBox(10);
        box.getStyleClass().add("loans-offer-card");

        StyledText nameLabel = StyledText.weekLabel(
                LanguageManager.get("loans.offer." + offer.id() + ".name"));

        Region nameSpacer = new Region();
        HBox.setHgrow(nameSpacer, Priority.ALWAYS);
        HBox nameRow = new HBox(nameLabel, nameSpacer, buildBadge(offer.riskLevel()));
        nameRow.setAlignment(Pos.CENTER_LEFT);

        StyledText description = StyledText.detailValue(
                LanguageManager.get("loans.offer." + offer.id() + ".description"));
        description.getStyleClass().add("loans-offer-muted");
        description.setWrapText(true);

        VBox keyValues = new VBox(6);
        keyValues.getChildren().addAll(
                buildKeyValueRow("loans.offer.rate",
                        TableCells.NUMBER_FORMAT.format(
                                offer.weeklyInterestRate().multiply(BigDecimal.valueOf(100))) + "%"),
                buildKeyValueRow("loans.offer.term",
                        MessageFormat.format(LanguageManager.get("loans.offer.weeks"), offer.termWeeks())),
                buildKeyValueRow("loans.offer.maxAmount",
                        CurrencyFormatter.format(offer.maxPrincipal()))
        );

        Region bottomSpacer = new Region();
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        Button applyBtn = new Button(LanguageManager.get("loans.button.apply"));
        applyBtn.getStyleClass().add("modal-button");
        applyBtn.setMaxWidth(Double.MAX_VALUE);
        applyBtn.setOnAction(e -> {
            select(index);
            if (onApplyClicked != null) {
                onApplyClicked.accept(offer);
            }
        });
        applyButtons.add(applyBtn);

        box.getChildren().addAll(nameRow, description, keyValues, bottomSpacer, applyBtn);
        return box;
    }

    /**
     * Moves the selection to the given index. The selected offer gets a blue
     * border and a filled primary button; the others get an outlined button.
     */
    private void select(int index) {
        selectedIndex = index;
        for (int i = 0; i < offerNodes.size(); i++) {
            boolean isSelected = i == index;

            VBox node = offerNodes.get(i);
            node.getStyleClass().remove("loans-offer-card-selected");
            if (isSelected) node.getStyleClass().add("loans-offer-card-selected");

            Button btn = applyButtons.get(i);
            btn.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
            btn.getStyleClass().add(isSelected ? "modal-button-primary" : "modal-button-outlined");
        }
    }

    private void handleKeyPress(KeyEvent e) {
        if (offerNodes.isEmpty()) return;
        switch (e.getCode()) {
            case LEFT  -> select(Math.max(0, selectedIndex - 1));
            case RIGHT -> select(Math.min(offerNodes.size() - 1, selectedIndex + 1));
            case ENTER -> { applyButtons.get(selectedIndex).fire(); e.consume(); }
            default    -> {}
        }
    }

    private StyledText buildBadge(LoanRiskLevel riskLevel) {
        String textKey = switch (riskLevel) {
            case LOW -> "loans.risk.low";
            case MEDIUM -> "loans.risk.medium";
            case HIGH -> "loans.risk.high";
        };
        String colorClass = switch (riskLevel) {
            case LOW -> "loans-offer-badge-low";
            case MEDIUM -> "loans-offer-badge-medium";
            case HIGH -> "loans-offer-badge-high";
        };
        StyledText badge = StyledText.detailLabel(LanguageManager.get(textKey));
        badge.getStyleClass().addAll("loans-offer-badge", colorClass);
        return badge;
    }

    private HBox buildKeyValueRow(String labelKey, String value) {
        StyledText label = StyledText.detailLabel(LanguageManager.get(labelKey));
        label.getStyleClass().add("loans-offer-muted");
        StyledText valueText = StyledText.detailValue(value);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(label, spacer, valueText);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
