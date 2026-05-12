package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.RealizedReturnsService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
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

    private final GameService gameService;
    private final RealizedReturnsService realizedService = new RealizedReturnsService();

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
     * @param gameService the game manager containing player and exchange
     */
    public RealizedReturnsCard(GameService gameService) {
        super(gameService);
        this.gameService = gameService;
        setSpacing(16);

        emptyContainer.setAlignment(Pos.CENTER);
        emptyContainer.setPadding(new Insets(12, 0, 12, 0));

        valuesContainer.getChildren().addAll(
                buildRow(gainLabel, gainValue, "tooltip.realized.gain",
                         lossLabel, lossValue, "tooltip.realized.loss",
                         netLabel, netValue, "tooltip.realized.net"),
                buildRow(taxLabel, taxValue, "tooltip.realized.tax",
                         commissionLabel, commissionValue, "tooltip.realized.commission",
                         countLabel, countValue, null)
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
        boolean hasSales = realizedService.getSalesCount(gameService.getPlayer()) > 0;

        emptyContainer.setVisible(!hasSales);
        emptyContainer.setManaged(!hasSales);
        valuesContainer.setVisible(hasSales);
        valuesContainer.setManaged(hasSales);

        if (!hasSales) return;

        BigDecimal gains = realizedService.getGainsInNok(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal losses = realizedService.getLossesInNok(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal net = realizedService.getNetRealizedInNok(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal tax = realizedService.getTotalTaxPaidInNok(gameService.getPlayer(), gameService.getCurrencyConverter());
        BigDecimal commission = realizedService.getTotalSaleCommissionInNok(gameService.getPlayer(), gameService.getCurrencyConverter());
        int count = realizedService.getSalesCount(gameService.getPlayer());

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

    private HBox buildRow(StyledText l1, StyledText v1, String key1,
                          StyledText l2, StyledText v2, String key2,
                          StyledText l3, StyledText v3, String key3) {
        HBox row = new HBox(20);
        VBox c1 = buildCell(l1, v1, key1);
        VBox c2 = buildCell(l2, v2, key2);
        VBox c3 = buildCell(l3, v3, key3);
        row.getChildren().addAll(c1, c2, c3);
        return row;
    }

    private VBox buildCell(StyledText label, StyledText value, String tooltipKey) {
        VBox cell;
        if (tooltipKey != null) {
            InfoTooltip infoTooltip = new InfoTooltip(tooltipKey);
            HBox labelRow = new HBox(4, label, infoTooltip);
            labelRow.setAlignment(Pos.CENTER_LEFT);
            infoTooltip.attachToParent(labelRow);
            cell = new VBox(4, labelRow, value);
        } else {
            cell = new VBox(4, label, value);
        }
        HBox.setHgrow(cell, Priority.ALWAYS);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMinWidth(0);
        cell.setPrefWidth(1);
        return cell;
    }

    private void applyColor(StyledText node, String modifier) {
        node.getStyleClass().removeAll("positive", "negative");
        if (modifier != null) {
            node.getStyleClass().add(modifier);
        }
    }
}