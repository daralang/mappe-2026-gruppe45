package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.LedgerTypeFilter;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Body component for the "Lån og rente" sub-tab on the Transactions tab.
 * Shows a filterable table of every loan-related money movement — disbursements,
 * weekly interest deductions, and repayments.
 *
 * <p>Plain {@link VBox} body: the surrounding card chrome and title are
 * owned by {@code TransactionsView}, matching the pattern of
 * {@link TransactionsCard}.</p>
 */
public class LoanLedgerCard extends VBox implements GameObserver {

    private static final int COLUMN_COUNT = 4;
    private static final double[] COLUMN_WIDTHS  = {10, 38, 22, 30};
    private static final HPos[]   COLUMN_ALIGNMENTS = {
            HPos.LEFT, HPos.LEFT, HPos.LEFT, HPos.RIGHT
    };

    private final GameService gameService;
    private final LedgerTypeFilter<LoanLedgerEntryType> typeFilter;
    private final WeekRangeFilter weekRangeFilter;
    private final GridPane grid = new GridPane();

    public LoanLedgerCard(GameService gameService) {
        this.gameService = gameService;

        int currentWeek = Math.max(gameService.getExchange().getWeek(), 1);
        this.weekRangeFilter = new WeekRangeFilter(1, currentWeek);

        gameService.addObserver(this);
        LanguageManager.addObserver(this::refresh);

        setSpacing(16);

        typeFilter = new LedgerTypeFilter<>(List.of(
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.all",         null),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.disbursement", LoanLedgerEntryType.DISBURSEMENT),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.interest",     LoanLedgerEntryType.INTEREST),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.repayment",    LoanLedgerEntryType.REPAYMENT)
        ));
        typeFilter.selectedValueProperty().addListener((_, _, _) -> refresh());

        weekRangeFilter.fromWeekProperty().addListener((_, _, _) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((_, _, _) -> refresh());

        HBox filterRow = new HBox(36, typeFilter, weekRangeFilter);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        filterRow.getStyleClass().add("transactions-filter-row");

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(filterRow, grid);
        refresh();
    }

    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

    private void refresh() {
        grid.getChildren().clear();
        if (gameService.getPlayer() == null) return;

        TableCells.addHeaderRow(grid, new String[]{
                LanguageManager.get("transactions.loans.col.week"),
                LanguageManager.get("transactions.loans.col.loan"),
                LanguageManager.get("transactions.loans.col.type"),
                LanguageManager.get("transactions.loans.col.amount")
        });

        List<LoanLedgerEntry> ledger = gameService.getPlayer().getLoanLedger();
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek   = weekRangeFilter.getToWeek();
        LoanLedgerEntryType selectedType = typeFilter.getSelectedValue();

        List<LoanLedgerEntry> filtered = ledger.stream()
                .filter(e -> e.week() >= fromWeek && e.week() <= toWeek)
                .filter(e -> selectedType == null || e.type() == selectedType)
                .toList();

        if (filtered.isEmpty()) {
            TableCells.renderEmptyState(
                    grid, LanguageManager.get("transactions.loans.empty"), COLUMN_COUNT);
            return;
        }

        // Build a stable label for each distinct loan in order of first disbursement.
        Map<Loan, String> loanLabels = buildLoanLabels(ledger);

        int row = 1;
        for (LoanLedgerEntry entry : filtered) {
            addDataRow(row++, entry, loanLabels.getOrDefault(entry.loan(), "?"));
        }
    }

    /**
     * Numbers each distinct loan per offer type in the order its DISBURSEMENT
     * entry first appears, producing labels like "Standardlån #1", "Hurtiglån #1".
     */
    private Map<Loan, String> buildLoanLabels(List<LoanLedgerEntry> ledger) {
        Map<Loan, String> labels = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (LoanLedgerEntry entry : ledger) {
            if (entry.type() == LoanLedgerEntryType.DISBURSEMENT
                    && !labels.containsKey(entry.loan())) {
                String offerId = entry.loan().offer().id();
                int count = counts.merge(offerId, 1, Integer::sum);
                String name = LanguageManager.get("loans.offer." + offerId + ".name");
                labels.put(entry.loan(), name + " #" + count);
            }
        }
        return labels;
    }

    private void addDataRow(int row, LoanLedgerEntry entry, String loanLabel) {
        grid.add(TableCells.data(MessageFormat.format(
                LanguageManager.get("transactions.weekValue"), entry.week())), 0, row);
        grid.add(TableCells.data(loanLabel), 1, row);
        grid.add(typeBadge(entry.type()), 2, row);
        grid.add(ChangeFormatter.styledAmount(entry.amount(), "holdings-cell"), 3, row);
    }

    private Label typeBadge(LoanLedgerEntryType type) {
        String labelKey = switch (type) {
            case DISBURSEMENT -> "loans.ledger.type.disbursement";
            case INTEREST     -> "loans.ledger.type.interest";
            case REPAYMENT    -> "loans.ledger.type.repayment";
        };
        String cssClass = switch (type) {
            case DISBURSEMENT -> "badge-disbursement";
            case INTEREST     -> "badge-interest";
            case REPAYMENT    -> "badge-repayment";
        };
        Label badge = new Label(LanguageManager.get(labelKey));
        badge.getStyleClass().addAll("transaction-type-badge", cssClass);
        return badge;
    }
}
