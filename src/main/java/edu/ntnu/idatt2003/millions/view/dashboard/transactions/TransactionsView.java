package edu.ntnu.idatt2003.millions.view.dashboard.transactions;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsActivityCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsSummaryCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The transactions tab view shown under the dashboard.
 *
 * <p>Acts as a thin container for the cards on this tab — a
 * {@link TransactionsCard} with the history table on top, followed
 * by a row of two cards: a {@link TransactionsSummaryCard} showing
 * NOK totals on the left and a {@link TransactionsActivityCard} showing
 * transaction counts on the right. The two bottom cards share the
 * full width 50/50 and read as a pair: one tells the user how much
 * money moved, the other how many trades they made.</p>
 *
 * <p>The view owns a single {@link WeekRangeFilter} instance and
 * hands it to all three cards. The summary and activity cards
 * always reflect the period the table shows; picking three
 * different ranges on one tab would be a confusing UX. The filter
 * widget itself is rendered inside {@code TransactionsCard}'s
 * filter row, and the summary and activity cards each show the
 * current range as a read-only "UKE {from}-{to}" label.</p>
 *
 * <p>The view itself owns no observers: each card it contains is
 * self-sufficient and reacts to game and language changes through
 * its own observer registration.</p>
 */
public class TransactionsView extends VBox {

    /** Spacing between the table card and the summary row below. */
    private static final int VERTICAL_SPACING = 16;

    /** Spacing between the two cards in the bottom row. */
    private static final int HORIZONTAL_SPACING = 16;

    /**
     * Constructs a new TransactionsView and wires the shared filter
     * into all three cards.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TransactionsView(GameService gameService) {
        setSpacing(VERTICAL_SPACING);

        int currentWeek = Math.max(gameService.getExchange().getWeek(), 1);
        WeekRangeFilter sharedFilter = new WeekRangeFilter(1, currentWeek);

        TransactionsCard transactionsCard = new TransactionsCard(gameService, sharedFilter);
        TransactionsSummaryCard summaryCard = new TransactionsSummaryCard(gameService, sharedFilter);
        TransactionsActivityCard activityCard = new TransactionsActivityCard(gameService, sharedFilter);

        HBox.setHgrow(summaryCard, Priority.ALWAYS);
        HBox.setHgrow(activityCard, Priority.ALWAYS);
        // Set minWidth to 0 so the cards can shrink below their preferred
        // size on narrow viewports without pushing each other off-screen;
        // Priority.ALWAYS alone keeps them expanded, but not collapsible.
        summaryCard.setMinWidth(0);
        activityCard.setMinWidth(0);

        HBox bottomRow = new HBox(HORIZONTAL_SPACING, summaryCard, activityCard);

        getChildren().addAll(transactionsCard, bottomRow);
    }
}