package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntry;
import edu.ntnu.idatt2003.millions.model.loan.LoanLedgerEntryType;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.LoanLedgerSort;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.LedgerTypeFilter;
import edu.ntnu.idatt2003.millions.view.component.WeekRangeFilter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Body component for the "Lån og rente" sub-tab on the Transactions tab.
 * Shows a searchable, sortable, paginated table of every loan-related money
 * movement — disbursements, weekly interest deductions, and repayments.
 *
 * <p>Extends {@link SortableTableCard} for shared search, metadata row,
 * sort state, pagination, and the common refresh Template Method. The filter
 * row includes a {@link LedgerTypeFilter} scoped to {@link LoanLedgerEntryType}
 * and a {@link WeekRangeFilter}, matching the visual style of
 * {@link TransactionsCard}.</p>
 *
 * <p>The outer card chrome and title are owned by
 * {@code TransactionsView}, so this class acts as a body-only component.</p>
 */
public class LoanLedgerCard extends SortableTableCard<LoanLedgerEntry, LoanLedgerSort.SortColumn> {

    private static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;
    private static final double ROW_HEIGHT = 34.0;

    private final GameService gameService;
    private final LoanLedgerSort sort;
    private final LedgerTypeFilter<LoanLedgerEntryType> typeFilter;
    private final WeekRangeFilter weekRangeFilter;
    private SearchBar ownSearchBar;

    /**
     * The latest loan-label map computed from the full ledger.
     * Re-populated on every {@link #fetchAll()} call so the LOAN-column
     * comparator in {@link LoanLedgerSort} always sees labels consistent
     * with what the user sees in the table.
     */
    private Map<Loan, String> loanLabels = new LinkedHashMap<>();

    /**
     * Constructs a new LoanLedgerCard.
     *
     * @param gameService the game service supplying the active player's loan ledger
     */
    public LoanLedgerCard(GameService gameService) {
        super(gameService, PAGE_SIZE, "transactions.loans.status", "transactions.loans.empty");
        getStyleClass().remove("card");
        this.gameService = gameService;

        int currentWeek = Math.max(gameService.getExchange().getWeek(), 1);
        this.weekRangeFilter = new WeekRangeFilter(1, currentWeek);
        this.sort = new LoanLedgerSort(entry -> loanLabels.getOrDefault(entry.loan(), "?"));
        this.sortProvider = sort;
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        table.setMinHeight(PAGE_SIZE * ROW_HEIGHT);
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);

        typeFilter = new LedgerTypeFilter<>(List.of(
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.all",          null),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.disbursement",  LoanLedgerEntryType.DISBURSEMENT),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.interest",      LoanLedgerEntryType.INTEREST),
                new LedgerTypeFilter.TypeOption<>("loans.ledger.type.repayment",     LoanLedgerEntryType.REPAYMENT)
        ));
        typeFilter.selectedValueProperty().addListener((_, _, _) -> resetPageAndRefresh());
        weekRangeFilter.fromWeekProperty().addListener((_, _, _) -> resetPageAndRefresh());
        weekRangeFilter.toWeekProperty().addListener((_, _, _) -> resetPageAndRefresh());

        setSpacing(16);
        getChildren().addAll(buildSearchRow(clearSortButton), table.asNode(), pagination);
        refresh();
    }

    /**
     * Overrides the default search row to include {@link LedgerTypeFilter}
     * and {@link WeekRangeFilter} alongside the search bar and clear-sort button.
     *
     * @param clearSortButton the button from
     *        {@link SortColumnTable#createClearSortButton}
     * @return the configured filter row
     */
    @Override
    protected HBox buildSearchRow(Button clearSortButton) {
        ownSearchBar = createSearchBar(
                "loans.search.placeholder",
                "search.button",
                metadataRow);
        HBox row = new HBox(16, ownSearchBar, typeFilter, weekRangeFilter, clearSortButton);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

    /**
     * Focuses the loan ledger search field.
     */
    @Override
    public void focusSearch() {
        if (ownSearchBar != null) {
            ownSearchBar.focus();
        }
    }

    /**
     * Returns loan-ledger entries scoped to the active week range and type filter.
     * Also rebuilds {@link #loanLabels} from the full ledger so that the LOAN
     * sort column stays consistent with the rendered labels.
     *
     * @return mutable list of pre-filtered entries
     */
    @Override
    protected List<LoanLedgerEntry> fetchAll() {
        if (gameService.getPlayer() == null) {
            return new ArrayList<>();
        }
        List<LoanLedgerEntry> allLedger = gameService.getPlayer().getLoanLedger();
        loanLabels = buildLoanLabels(allLedger);

        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek   = weekRangeFilter.getToWeek();
        LoanLedgerEntryType selectedType = typeFilter.getSelectedValue();

        return new ArrayList<>(allLedger.stream()
                .filter(e -> e.week() >= fromWeek && e.week() <= toWeek)
                .filter(e -> selectedType == null || e.type() == selectedType)
                .sorted(Comparator.comparingInt(LoanLedgerEntry::week)
                        .thenComparingInt(e -> typePriority(e.type()))
                        .reversed())
                .toList());
    }

    /**
     * Filters entries by loan label matching the search term.
     *
     * @param all  the pre-filtered entry list from {@link #fetchAll()}
     * @param term the active search term; may be blank
     * @return a new mutable list of matching entries
     */
    @Override
    protected List<LoanLedgerEntry> applySearch(List<LoanLedgerEntry> all, String term) {
        if (term.isBlank()) {
            return new ArrayList<>(all);
        }
        String lower = term.toLowerCase();
        return new ArrayList<>(all.stream()
                .filter(e -> loanLabels.getOrDefault(e.loan(), "").toLowerCase().contains(lower))
                .toList());
    }

    /**
     * Builds the column-keyed cells for one loan-ledger entry. The loan label is
     * looked up from {@link #loanLabels} (keyed by {@link Loan}), which is rebuilt
     * on every {@link #fetchAll()} call.
     *
     * @param item     the entry to render
     * @param rowIndex the one-based grid row this entry occupies
     * @return the column-keyed cells for this entry
     */
    @Override
    protected RowCells<LoanLedgerSort.SortColumn> buildRowCells(LoanLedgerEntry item, int rowIndex) {
        String loanLabel = loanLabels.getOrDefault(item.loan(), "?");
        return RowCells.<LoanLedgerSort.SortColumn>builder()
                .put(LoanLedgerSort.SortColumn.WEEK, TableCells.data(String.valueOf(item.week())))
                .put(LoanLedgerSort.SortColumn.LOAN, TableCells.data(loanLabel))
                .put(LoanLedgerSort.SortColumn.TYPE, typeBadge(item.type()))
                .put(LoanLedgerSort.SortColumn.AMOUNT,
                        ChangeFormatter.styledAmount(item.amount(), "table-cell"));
    }

    /**
     * Returns a localised empty-state message that includes the search term when active.
     *
     * @param term the active search term; may be blank
     * @return the localised empty-state message
     */
    @Override
    protected String emptyStateMessage(String term) {
        if (!term.isBlank()) {
            return MessageFormat.format(
                    LanguageManager.get("transactions.loans.empty.search"), term);
        }
        return LanguageManager.get("transactions.loans.empty");
    }

    /**
     * Extends the week range filter's upper bound and refreshes whenever the model changes.
     */
    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

    /**
     * Numbers each distinct loan per offer type in the order its DISBURSEMENT
     * entry first appears, producing labels like "Standardlån #1", "Hurtiglån #1".
     *
     * @param ledger the full loan ledger to build labels from
     * @return an ordered map from {@link Loan} to its human-readable label
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

    /**
     * Maps a loan-ledger entry type to its chronological tiebreak priority,
     * so entries within the same week order interest, then disbursement, then repayment.
     *
     * @param type the entry type to prioritise
     * @return the sort priority for the given type
     */
    private static int typePriority(LoanLedgerEntryType type) {
        return switch (type) {
            case INTEREST     -> 0;
            case DISBURSEMENT -> 1;
            case REPAYMENT    -> 2;
        };
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
        Label badge = new Label(LanguageManager.get(labelKey).toUpperCase());
        badge.getStyleClass().addAll("transaction-type-badge", cssClass);
        return badge;
    }
}
