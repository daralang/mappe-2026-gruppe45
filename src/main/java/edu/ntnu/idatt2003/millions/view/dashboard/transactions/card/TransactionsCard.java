package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;

/**
 * Dashboard card for the transactions tab.
 *
 * <p>Currently only renders the section title; filter controls, the table
 * and per-row interactions will be layered on in follow-up changes. The
 * card already registers itself as a game and language observer via the
 * {@link Card} base class, so the scaffolding is in place even though
 * {@link #onGameUpdated()} has nothing to update yet.</p>
 */
public class TransactionsCard extends Card {

    private final StyledText title;

    /**
     * Constructs a new TransactionsCard.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TransactionsCard(GameService gameService) {
        super(gameService);

        setSpacing(16);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));

        getChildren().add(title);
    }

    /**
     * Will be used to re-render the transaction list when buys, sales or
     * week advances change the underlying model. No state to refresh yet.
     */
    @Override
    public void onGameUpdated() {
        // No-op until the table is added.
    }

    /**
     * Refreshes the section title when the active language changes.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.title"));
    }
}