package edu.ntnu.idatt2003.millions.view.dashboard.transactions;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.transaction.TransactionStatsService;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.LoanLedgerCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsActivityCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsCard;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.card.TransactionsSummaryCard;
import edu.ntnu.idatt2003.millions.view.component.WeekRangeFilter;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * The Transactions tab view shown under the dashboard.
 *
 * <p>Layout: one outer card containing the section title, a compact sub-tab
 * bar ("Aksjehandel" / "Lån og rente"), and a swappable content area.
 * Below the outer card, a row of summary and activity cards is shown only
 * while the Aksjehandel sub-tab is active — the loan-ledger sub-tab omits
 * them in this iteration.</p>
 *
 * <p>The {@link WeekRangeFilter} is shared across all three lower cards and
 * both sub-tab bodies, so the selected period is preserved when switching
 * between sub-tabs.</p>
 */
public class TransactionsView extends VBox implements SearchFocusProvider {

    private static final int VERTICAL_SPACING   = 16;
    private static final int HORIZONTAL_SPACING = 16;
    private static final int SUBTAB_BAR_SPACING = 24;

    private final HBox bottomRow;
    private final VBox contentArea = new VBox();
    private final TransactionsCard tradesBody;
    private final LoanLedgerCard   loanLedgerBody;
    private Button activeSubTab;
    private boolean tradesTabActive = true;

    public TransactionsView(GameService gameService) {
        setSpacing(VERTICAL_SPACING);

        int currentWeek = Math.max(gameService.getExchange().getWeek(), 1);
        WeekRangeFilter sharedFilter = new WeekRangeFilter(1, currentWeek);

        tradesBody     = new TransactionsCard(gameService, sharedFilter, new TransactionStatsService());
        loanLedgerBody = new LoanLedgerCard(gameService);

        // ── Outer card ──────────────────────────────────────────────────────
        StyledText title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));
        LanguageManager.addObserver(() ->
                title.setText(LanguageManager.get("transactions.title")));

        HBox subTabBar = buildSubTabBar();

        VBox outerCard = new VBox(16, title, subTabBar, contentArea);
        outerCard.getStyleClass().add("card");

        // ── Summary / Activity row (Aksjehandel only) ───────────────────────
        TransactionsSummaryCard  summaryCard  = new TransactionsSummaryCard(gameService, sharedFilter);
        TransactionsActivityCard activityCard = new TransactionsActivityCard(gameService, sharedFilter);
        HBox.setHgrow(summaryCard,  Priority.ALWAYS);
        HBox.setHgrow(activityCard, Priority.ALWAYS);
        summaryCard.setMinWidth(0);
        activityCard.setMinWidth(0);
        bottomRow = new HBox(HORIZONTAL_SPACING, summaryCard, activityCard);

        getChildren().addAll(outerCard, bottomRow);
        showTrades();
    }

    private HBox buildSubTabBar() {
        HBox bar = new HBox(SUBTAB_BAR_SPACING);
        bar.getStyleClass().addAll("tab-bar", "tab-bar-compact");

        Button tradesBtn = new Button(LanguageManager.get("transactions.subtab.trades"));
        tradesBtn.getStyleClass().add("tab-button");
        tradesBtn.setOnAction(e -> { setActiveSubTab(tradesBtn); showTrades(); });

        Button loansBtn = new Button(LanguageManager.get("transactions.subtab.loans"));
        loansBtn.getStyleClass().add("tab-button");
        loansBtn.setOnAction(e -> { setActiveSubTab(loansBtn); showLoanLedger(); });

        bar.getChildren().addAll(tradesBtn, loansBtn);
        setActiveSubTab(tradesBtn);

        LanguageManager.addObserver(() -> {
            tradesBtn.setText(LanguageManager.get("transactions.subtab.trades"));
            loansBtn.setText(LanguageManager.get("transactions.subtab.loans"));
        });

        return bar;
    }

    private void setActiveSubTab(Button button) {
        if (activeSubTab != null) activeSubTab.getStyleClass().remove("tab-button-active");
        activeSubTab = button;
        activeSubTab.getStyleClass().add("tab-button-active");
    }

    /**
     * Focuses the search field of the currently active sub-tab.
     * Delegates to {@link TransactionsCard} or {@link LoanLedgerCard}
     * depending on which sub-tab is shown.
     */
    @Override
    public void focusSearch() {
        if (tradesTabActive) {
            tradesBody.focusSearch();
        } else {
            loanLedgerBody.focusSearch();
        }
    }

    private void showTrades() {
        tradesTabActive = true;
        contentArea.getChildren().setAll(tradesBody);
        if (!getChildren().contains(bottomRow)) {
            getChildren().add(bottomRow);
        }
    }

    private void showLoanLedger() {
        tradesTabActive = false;
        contentArea.getChildren().setAll(loanLedgerBody);
        getChildren().remove(bottomRow);
    }
}
