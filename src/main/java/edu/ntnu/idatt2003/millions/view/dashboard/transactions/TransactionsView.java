package edu.ntnu.idatt2003.millions.view.dashboard.transactions;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsCard;
import javafx.scene.layout.VBox;

/**
 * The transactions tab view shown under the dashboard.
 *
 * <p>Acts as a thin container for the cards on this tab — currently a
 * single {@link TransactionsCard}, with room to add more cards (summary
 * widgets, charts, etc.) below it without restructuring the view. This
 * mirrors {@code PortfolioView}, which lays out a stack of cards the
 * same way.</p>
 *
 * <p>The view itself owns no state and no observers: each card it
 * contains is self-sufficient and reacts to game and language changes
 * through its own observer registration.</p>
 */
public class TransactionsView extends VBox {

    /**
     * Constructs a new TransactionsView and adds its cards.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TransactionsView(GameService gameService) {
        setSpacing(16);

        TransactionsCard transactionsCard = new TransactionsCard(gameService);

        getChildren().add(transactionsCard);
    }
}