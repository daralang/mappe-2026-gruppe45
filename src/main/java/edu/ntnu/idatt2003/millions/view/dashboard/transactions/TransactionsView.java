package edu.ntnu.idatt2003.millions.view.dashboard.transactions;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsSummaryCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.scene.layout.VBox;

/**
 * The transactions tab view shown under the dashboard.
 *
 * <p>Acts as a thin container for the cards on this tab — a
 * {@link TransactionsCard} with the history table, followed by a
 * {@link TransactionsSummaryCard} with aggregated totals for the
 * same week range. A trading-activity counter card will be placed
 * next to the summary card in a horizontal row once it lands; until
 * then the summary card spans the full width. This mirrors how
 * {@code PortfolioView} stacks its cards.</p>
 *
 * <p>The view owns a single {@link WeekRangeFilter} instance and
 * hands it to both cards. The summary always reflects what the
 * user sees in the table, which is the natural reading of
 * "summary of transactions" — picking a separate range for the
 * summary would be a confusing UX where the two cards on the same
 * tab show data for different periods. The filter widget itself
 * is rendered inside {@code TransactionsCard}, in its filter row;
 * the summary card only shows the current range as a read-only
 * "Uke {from}-{to}" label, so the same filter visibly drives both
 * cards without being duplicated on screen.</p>
 *
 * <p>The view itself owns no observers: each card it contains is
 * self-sufficient and reacts to game and language changes through
 * its own observer registration. The view forwards game updates to
 * the filter so the spinner's upper bound advances with the game.</p>
 */
public class TransactionsView extends VBox {

    /** Spacing between the two cards on the tab. */
    private static final int CARD_SPACING = 16;

    /**
     * Constructs a new TransactionsView and wires the shared filter
     * into both cards.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TransactionsView(GameService gameService) {
        setSpacing(CARD_SPACING);

        int currentWeek = Math.max(gameService.getExchange().getWeek(), 1);
        WeekRangeFilter sharedFilter = new WeekRangeFilter(1, currentWeek);

        TransactionsCard transactionsCard = new TransactionsCard(gameService, sharedFilter);
        TransactionsSummaryCard summaryCard = new TransactionsSummaryCard(gameService, sharedFilter);

        getChildren().addAll(transactionsCard, summaryCard);
    }
}