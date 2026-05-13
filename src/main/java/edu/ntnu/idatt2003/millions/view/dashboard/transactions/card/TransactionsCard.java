package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.TransactionsSort;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.TransactionTypeFilter;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard card for the transactions tab.
 *
 * <p>Renders the section title, a search and filter row, and a sortable
 * paginated table of every committed transaction within the current filter selection. The default order
 * is chronological (oldest first); clicking a column header activates
 * ascending sort on that column, toggling to descending on a second click.</p>
 *
 * <p>The filter row holds a {@link SearchBar}, a {@link TransactionTypeFilter}
 * (all / buy / sell), and a {@link WeekRangeFilter}. The {@link WeekRangeFilter}
 * is shared with {@code TransactionsSummaryCard} so both cards show data for the same period.</p>
 *
 * <p>Column structure, sort state and header rendering are owned by
 * {@link SortColumnTable}. Domain-specific sort logic is delegated to
 * {@link TransactionsSort}. This card is responsible for data fetching,
 * filtering, sort orchestration and cell construction only.</p>
 */
public class TransactionsCard extends Card {

    private static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;
    private static final double ROW_HEIGHT = 44.0;

    private final GameService gameService;
    private final TransactionStatsService statsService = new TransactionStatsService();
    private final TransactionsSort sort;
    private final SortColumnTable<TransactionsSort.SortColumn> table;
    private final StyledText title;
    private final TransactionTypeFilter typeFilter;
    private final WeekRangeFilter weekRangeFilter;
    private final Pagination pagination;

    private int currentPage = 0;
    private String currentSearchTerm = "";

    /**
     * Constructs a new TransactionsCard.
     *
     * <p>Column definitions, widths and alignments are read from
     * {@link TransactionsSort#getColumnDefs()} via a method reference so that
     * {@link SortColumnTable} can resolve fresh i18n labels on every header refresh.</p>
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes both this card's
     *                        table and the summary card's totals
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;
        this.sort = new TransactionsSort(statsService, gameService.getCurrencyConverter());
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        table.setMinHeight(PAGE_SIZE * ROW_HEIGHT);

        setSpacing(16);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));

        typeFilter = new TransactionTypeFilter();
        typeFilter.selectedTypeProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());

        weekRangeFilter.fromWeekProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());
        weekRangeFilter.toWeekProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());

        getChildren().addAll(title, buildFilterRow(), table.asNode(), pagination);
        refresh();
    }

    /**
     * Builds the filter row that sits between the title and the table.
     *
     * <p>The search field sits first, followed by the type and week filters.
     * A flexible spacer takes up remaining width so future controls can be
     * inserted without restructuring.</p>
     *
     * @return the configured filter row
     */
    private HBox buildFilterRow() {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                term -> {
                    currentSearchTerm = term == null ? "" : term;
                    resetPageAndRefresh();
                });
        searchBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(16, searchBar, typeFilter, weekRangeFilter, spacer);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

    /**
     * Picks up new transactions and extends the week range filter's upper bound
     * whenever the model changes.
     */
    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

    /**
     * Refreshes the section title and rebuilds the table so column headers,
     * badge labels and the empty-state message follow the active language.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.title"));
        refresh();
    }

    /**
     * Rebuilds the table: clears, rebuilds the header, collects and filters
     * transactions, optionally sorts them, then renders the current page or an empty state.
     *
     * <p>When no sort is active the default chronological order from
     * {@link #collectRange} is preserved.</p>
     */
    private void refresh() {
        table.clearRows();
        if (gameService.getPlayer() == null) {
            pagination.update(0, 0);
            return;
        }
        table.refreshHeader(this::refresh);

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();
        Class<? extends Transaction> selectedType = typeFilter.getSelectedType();

        List<Transaction> transactions = new ArrayList<>(
                collectRange(archive, fromWeek, toWeek).stream()
                        .filter(t -> selectedType == null || selectedType.isInstance(t))
                        .filter(t -> currentSearchTerm.isBlank() || t.getShare().getStock().matches(currentSearchTerm))
                        .toList());

        if (table.isSortActive()) {
            sort.applySort(transactions, table.getSortState());
        }

        if (transactions.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("transactions.empty"));
            pagination.update(0, 0);
            return;
        }

        clampCurrentPage(transactions.size());
        pagination.update(currentPage, transactions.size());

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, transactions.size());
        List<Transaction> page = transactions.subList(fromIndex, toIndex);

        int row = 1;
        for (Transaction transaction : page) {
            addDataRow(row++, transaction);
        }
    }

    /**
     * Sets the active page and refreshes the rendered transaction rows.
     * Called by {@link Pagination} when the user navigates.
     *
     * @param page the zero-based page index to render
     */
    private void setPage(int page) {
        currentPage = page;
        refresh();
    }

    /**
     * Returns to the first page and refreshes the table after a filter change.
     */
    private void resetPageAndRefresh() {
        currentPage = 0;
        refresh();
    }

    /**
     * Keeps the current page inside the valid page range after filtering.
     *
     * @param itemCount the number of filtered transactions
     */
    private void clampCurrentPage(int itemCount) {
        int lastPage = Math.max(0, (itemCount - 1) / PAGE_SIZE);
        if (currentPage > lastPage) {
            currentPage = lastPage;
        }
    }

    /**
     * Gathers every transaction in the archive within the given week range
     * (inclusive on both ends), sorted chronologically oldest first.
     *
     * @param archive  the archive to read transactions from
     * @param fromWeek the first week to include (inclusive)
     * @param toWeek   the last week to include (inclusive)
     * @return a chronologically sorted mutable list of transactions in the range
     */
    private List<Transaction> collectRange(TransactionArchive archive, int fromWeek, int toWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = fromWeek; week <= toWeek; week++) {
            list.addAll(archive.getTransactions(week));
        }
        list.sort(Comparator.comparingInt(Transaction::getWeek));
        return list;
    }

    /**
     * Renders one transaction as a data row in the table. Delegates value
     * computation to {@link TransactionStatsService#getStats} so this method
     * only deals with cell construction and placement.
     *
     * @param row         the table row index to write to
     * @param transaction the transaction to render
     */
    private void addDataRow(int row, Transaction transaction) {
        Stock stock = transaction.getShare().getStock();
        TransactionStatsService.TransactionStats stats =
                statsService.getStats(transaction, gameService.getCurrencyConverter());

        table.addRow(row,
                TableCells.data(MessageFormat.format(
                        LanguageManager.get("transactions.weekValue"), transaction.getWeek())),
                TableCells.data(stock.getSymbol() + ", " + stock.getCompany()),
                typeBadge(transaction),
                TableCells.data(TableCells.NUMBER_FORMAT.format(stats.quantity())),
                TableCells.data(TableCells.NUMBER_FORMAT.format(stats.pricePerShare())
                        + " " + stats.nativeCurrencyCode()),
                TableCells.data(TableCells.NUMBER_FORMAT.format(stats.commissionNok()) + " NOK"),
                taxCell(stats),
                ChangeFormatter.styledAmount(stats.amountNok(), "holdings-cell")
        );
    }

    /**
     * Creates the pill-shaped type badge for a transaction.
     * Purchases get a light-blue colour scheme, sales a light-green one.
     *
     * @param transaction the transaction to label
     * @return a styled badge label
     */
    private Label typeBadge(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String key = isPurchase ? "transactions.type.buy" : "transactions.type.sell";
        Label badge = new Label(LanguageManager.get(key));
        badge.getStyleClass().add("transaction-type-badge");
        badge.getStyleClass().add(isPurchase ? "badge-buy" : "badge-sell");
        return badge;
    }

    /**
     * Renders the tax cell. Purchases have no tax, so the column shows
     * an en-dash for visual cleanliness instead of "0,00 NOK".
     *
     * @param stats the row stats supplying the NOK tax amount
     * @return a styled cell label
     */
    private Label taxCell(TransactionStatsService.TransactionStats stats) {
        if (stats.taxNok().signum() == 0) {
            return TableCells.data("–");
        }
        return TableCells.data(TableCells.NUMBER_FORMAT.format(stats.taxNok()) + " NOK");
    }
}
