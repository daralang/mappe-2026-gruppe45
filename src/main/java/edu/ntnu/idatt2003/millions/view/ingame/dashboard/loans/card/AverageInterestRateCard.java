package edu.ntnu.idatt2003.millions.view.ingame.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.view.ingame.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Widget card showing the weighted average annual interest rate across active loans,
 * weighted by outstanding principal. Displays "–" when there are no active loans.
 */
public class AverageInterestRateCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    public AverageInterestRateCard(GameService gameService) {
        super(gameService, "dashboard.loans.averageRate");
        this.gameService = gameService;
        InfoTooltip tooltip = new InfoTooltip("tooltip.loans.averageRate");
        HBox titleRow = new HBox(5, titleLabel, tooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        tooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        List<Loan> loans = gameService.getPlayer().getActiveLoans();
        if (loans.isEmpty()) {
            valueLabel.setText("–");
            return;
        }
        // Weighted average weekly rate: sum(principal_i * weeklyRate_i) / sum(principal_i)
        BigDecimal totalPrincipal = BigDecimal.ZERO;
        BigDecimal weightedSum = BigDecimal.ZERO;
        for (Loan loan : loans) {
            weightedSum = weightedSum.add(
                    loan.principal().multiply(loan.offer().weeklyInterestRate()));
            totalPrincipal = totalPrincipal.add(loan.principal());
        }
        BigDecimal avgWeeklyRate = weightedSum
                .divide(totalPrincipal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        valueLabel.setText(MoneyFormatter.format(avgWeeklyRate) + " %");
    }
}
