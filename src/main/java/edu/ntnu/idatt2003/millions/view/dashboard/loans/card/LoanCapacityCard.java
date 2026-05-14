package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.InfoTooltip;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.WidgetCard;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

/**
 * Widget card showing the player's total loan capacity (50% of net worth).
 *
 * <p>The subtitle text ("Maks 50% av nettoverdi") is editorial copy that
 * intentionally mirrors {@code Player.MAX_DEBT_RATIO}. If that constant
 * changes, update {@code dashboard.loans.capacity.subtitle} in both i18n files.
 */
public class LoanCapacityCard extends WidgetCard {

    private final GameService gameService;
    private final StyledText valueLabel = StyledText.widgetValue();
    private final StyledText subtitleLabel = StyledText.widgetSubtitle();

    public LoanCapacityCard(GameService gameService) {
        super(gameService, "dashboard.loans.capacity");
        this.gameService = gameService;
        InfoTooltip tooltip = new InfoTooltip("tooltip.loans.capacity");
        HBox titleRow = new HBox(5, titleLabel, tooltip);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        tooltip.attachToParent(titleRow);
        getChildren().addAll(titleRow, valueLabel, subtitleLabel);
        refreshDisplay();
    }

    @Override
    protected void refreshDisplay() {
        valueLabel.setText(CurrencyFormatter.format(
                gameService.getPlayer().getLoanCapacity(gameService.getCurrencyConverter())));
        subtitleLabel.setText(LanguageManager.get("dashboard.loans.capacity.subtitle"));
    }
}
