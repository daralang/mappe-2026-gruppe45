package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Widget card showing the fraction of loan capacity in use, with a
 * colour-coded progress bar: green below 60%, amber 60–85%, red above 85%.
 */
public class DebtRatioCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();
    private final ProgressBar progressBar = new ProgressBar(0);

    public DebtRatioCard(GameService gameService) {
        super(gameService, "dashboard.loans.debtRatio");
        this.gameService = gameService;
        InfoTooltip tooltip = new InfoTooltip("tooltip.loans.debtRatio");
        HBox titleRow = new HBox(5, titleLabel, tooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        tooltip.attachToParent(titleRow);
        progressBar.getStyleClass().add("debt-ratio-bar");
        progressBar.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(titleRow, valueLabel, progressBar);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        Player player = gameService.getPlayer();
        BigDecimal capacity = player.getLoanCapacity(gameService.getCurrencyConverter());
        BigDecimal debt = player.getTotalDebt();

        if (capacity.compareTo(BigDecimal.ZERO) == 0 || debt.compareTo(BigDecimal.ZERO) == 0) {
            valueLabel.setText("0 %");
            progressBar.setProgress(0);
            applyBarColour(0.0);
            return;
        }

        double ratio = debt.divide(capacity, 4, RoundingMode.HALF_UP).doubleValue();
        int pct = (int) Math.round(ratio * 100);
        valueLabel.setText(pct + " %");
        progressBar.setProgress(Math.min(1.0, ratio));
        applyBarColour(ratio);
    }

    private void applyBarColour(double ratio) {
        progressBar.getStyleClass().removeAll("debt-bar-ok", "debt-bar-warning", "debt-bar-danger");
        if (ratio < 0.60) {
            progressBar.getStyleClass().add("debt-bar-ok");
        } else if (ratio < 0.85) {
            progressBar.getStyleClass().add("debt-bar-warning");
        } else {
            progressBar.getStyleClass().add("debt-bar-danger");
        }
    }
}
