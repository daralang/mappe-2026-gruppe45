package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

/**
 * Card displaying the player's realized returns — gains, losses,
 * taxes, and commissions from completed sales.
 *
 * <p>Until the player has completed their first sale, the card shows an
 * empty-state message instead of zero-filled values. Once a sale exists,
 * the card switches to a two-row, three-column grid of summary values.</p>
 */
public class RealizedReturnsCard extends Card {

    private final GameManager gameManager;

    private final StyledText title = StyledText.sectionTitle();
    private final StyledText emptyMessage = StyledText.detailLabel();
    private final VBox emptyContainer = new VBox(emptyMessage);
    private final VBox valuesContainer = new VBox(12);

    private final StyledText gainValue = StyledText.widgetValue();
    private final StyledText lossValue = StyledText.widgetValue();
    private final StyledText netValue = StyledText.widgetValue();
    private final StyledText taxValue = StyledText.widgetValue();
    private final StyledText commissionValue = StyledText.widgetValue();
    private final StyledText countValue = StyledText.widgetValue();

    private final StyledText gainLabel = StyledText.widgetLabel();
    private final StyledText lossLabel = StyledText.widgetLabel();
    private final StyledText netLabel = StyledText.widgetLabel();
    private final StyledText taxLabel = StyledText.widgetLabel();
    private final StyledText commissionLabel = StyledText.widgetLabel();
    private final StyledText countLabel = StyledText.widgetLabel();

    /**
     * Constructs a new RealizedReturnsCard.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public RealizedReturnsCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(16);

        emptyContainer.setAlignment(Pos.CENTER);
        emptyContainer.setPadding(new Insets(12, 0, 12, 0));

        valuesContainer.getChildren().addAll(
                buildRow(gainLabel, gainValue, lossLabel, lossValue, netLabel, netValue),
                buildRow(taxLabel, taxValue, commissionLabel, commissionValue, countLabel, countValue)
        );

        getChildren().addAll(title, emptyContainer, valuesContainer);

        applyLabels();
        refresh();
    }

    @Override
    protected void onLanguageChanged() {
        applyLabels();
        refresh();
    }

    @Override
    public void onGameUpdated() {
        refresh();
    }

    private void refresh() {
        boolean hasSales = gameManager.getSalesCount() > 0;

        emptyContainer.setVisible(!hasSales);
        emptyContainer.setManaged(!hasSales);
        valuesContainer.setVisible(hasSales);
        valuesContainer.setManaged(hasSales);

        if (!hasSales) return;

        BigDecimal gains = gameManager.getRealizedGainsInNok();
        BigDecimal losses = gameManager.getRealizedLossesInNok();
        BigDecimal net = gameManager.getNetRealizedInNok();
        BigDecimal tax = gameManager.getTotalTaxPaidInNok();
        BigDecimal commission = gameManager.getTotalSaleCommissionInNok();
        int count = gameManager.getSalesCount();

        String gainSign = gains.signum() > 0 ? "+" : "";
        gainValue.setText(gainSign + CurrencyFormatter.format(gains));
        applyColor(gainValue, gains.signum() > 0 ? "positive" : null);

        lossValue.setText(losses.signum() > 0
                ? "\u2212" + CurrencyFormatter.format(losses)
                : CurrencyFormatter.format(losses));
        applyColor(lossValue, losses.signum() > 0 ? "negative" : null);

        String netSign = net.signum() > 0 ? "+" : (net.signum() < 0 ? "\u2212" : "");
        netValue.setText(netSign + CurrencyFormatter.format(net.abs()));
        applyColor(netValue,
                net.signum() > 0 ? "positive" : (net.signum() < 0 ? "negative" : null));

        taxValue.setText(CurrencyFormatter.format(tax));
        applyColor(taxValue, null);

        commissionValue.setText(CurrencyFormatter.format(commission));
        applyColor(commissionValue, null);

        countValue.setText(String.valueOf(count));
        applyColor(countValue, null);
    }

    private void applyLabels() {
        title.setText(LanguageManager.get("dashboard.realized.title"));
        emptyMessage.setText(LanguageManager.get("dashboard.realized.empty"));
        gainLabel.setText(LanguageManager.get("dashboard.realized.gain"));
        lossLabel.setText(LanguageManager.get("dashboard.realized.loss"));
        netLabel.setText(LanguageManager.get("dashboard.realized.net"));
        taxLabel.setText(LanguageManager.get("dashboard.realized.tax"));
        commissionLabel.setText(LanguageManager.get("dashboard.realized.commission"));
        countLabel.setText(LanguageManager.get("dashboard.realized.count"));
    }

    private HBox buildRow(StyledText l1, StyledText v1,
                          StyledText l2, StyledText v2,
                          StyledText l3, StyledText v3) {
        HBox row = new HBox(20);
        row.getChildren().addAll(
                buildCell(l1, v1),
                buildCell(l2, v2),
                buildCell(l3, v3)
        );
        return row;
    }

    private VBox buildCell(StyledText label, StyledText value) {
        VBox cell = new VBox(4, label, value);
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMaxWidth(Double.MAX_VALUE);
        return cell;
    }

    private void applyColor(StyledText node, String modifier) {
        node.getStyleClass().removeAll("positive", "negative");
        if (modifier != null) {
            node.getStyleClass().add(modifier);
        }
    }
}