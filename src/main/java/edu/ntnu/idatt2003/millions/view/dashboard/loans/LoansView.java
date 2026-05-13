package edu.ntnu.idatt2003.millions.view.dashboard.loans;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.card.AvailableLoansCard;
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

    /**
     * Constructs a new LoansView.
     *
     * @param gameService the game manager containing player and exchange
     */
    public LoansView(GameService gameService) {
        setSpacing(VERTICAL_SPACING);

        availableLoansCard = new AvailableLoansCard(gameService);

        getChildren().add(availableLoansCard);
    }

    /**
     * Returns the available loans card, so the controller can wire
     * up the apply callback after construction.
     */
    public AvailableLoansCard getAvailableLoansCard() {
        return availableLoansCard;
    }
}
