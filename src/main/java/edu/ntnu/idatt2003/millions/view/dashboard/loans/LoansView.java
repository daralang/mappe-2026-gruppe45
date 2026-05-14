package edu.ntnu.idatt2003.millions.view.dashboard.loans;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.ActiveLoansCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.AverageInterestRateCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.AvailableLoansCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.DebtRatioCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.LoanCapacityCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.TotalDebtCard;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.WeeklyInterestCostCard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The loans tab view shown under the dashboard.
 *
 * <p>Acts as a thin container for the cards on this tab.
 * Currently displays {@link AvailableLoansCard} with the three
 * standard loan offers the player can apply for.</p>
 *
 * <p>The view itself owns no observers: each card it contains is
 * self-sufficient and reacts to game and language changes through
 * its own observer registration.</p>
 */
public class LoansView extends VBox {

    private static final int VERTICAL_SPACING = 16;

    private final AvailableLoansCard availableLoansCard;
    private final ActiveLoansCard activeLoansCard;

    /**
     * Constructs a new LoansView.
     *
     * @param gameService    the game manager containing player and exchange
     * @param loanController the controller for loan actions
     */
    public LoansView(GameService gameService, LoanController loanController) {
        setSpacing(VERTICAL_SPACING);

        availableLoansCard = new AvailableLoansCard(gameService);
        activeLoansCard = new ActiveLoansCard(gameService, loanController);

        HBox topRow = buildTopRow(gameService);

        getChildren().addAll(availableLoansCard, topRow, activeLoansCard);
    }

    private HBox buildTopRow(GameService gameService) {
        HBox row = new HBox(VERTICAL_SPACING);

        TotalDebtCard totalDebtCard = new TotalDebtCard(gameService);
        HBox.setHgrow(totalDebtCard, Priority.ALWAYS);

        VBox rightCards = new VBox(12,
                new WeeklyInterestCostCard(gameService),
                new AverageInterestRateCard(gameService),
                new LoanCapacityCard(gameService),
                new DebtRatioCard(gameService)
        );
        rightCards.setMinWidth(220);
        rightCards.setMaxWidth(260);

        totalDebtCard.prefHeightProperty().bind(rightCards.heightProperty());
        totalDebtCard.maxHeightProperty().bind(rightCards.heightProperty());

        row.getChildren().addAll(totalDebtCard, rightCards);
        return row;
    }

    /**
     * Returns the available loans card, so the controller can wire
     * up the apply callback after construction.
     */
    public AvailableLoansCard getAvailableLoansCard() {
        return availableLoansCard;
    }
}
