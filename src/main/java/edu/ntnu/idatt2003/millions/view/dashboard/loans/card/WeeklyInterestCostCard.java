package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Widget card showing the total weekly interest cost across all active loans.
 */
public class WeeklyInterestCostCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();

    public WeeklyInterestCostCard(GameService gameService) {
        super(gameService, "dashboard.loans.weeklyCost");
        this.gameService = gameService;
        InfoTooltip tooltip = new InfoTooltip("tooltip.loans.weeklyCost");
        HBox titleRow = new HBox(5, titleLabel, tooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        tooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, valueLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        List<Loan> loans = gameService.getPlayer().getActiveLoans();
        BigDecimal total = loans.stream()
                .map(l -> l.principal()
                        .multiply(l.offer().weeklyInterestRate())
                        .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        valueLabel.setText(CurrencyFormatter.format(total));
    }
}
