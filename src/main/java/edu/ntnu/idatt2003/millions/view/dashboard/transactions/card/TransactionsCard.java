package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

<<<<<<< HEAD
=======
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
>>>>>>> origin/main
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
<<<<<<< HEAD
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.TransactionsSort;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.TransactionTypeFilter;
=======
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.LedgerTypeFilter;
>>>>>>> origin/main
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
<<<<<<< HEAD
=======
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
>>>>>>> origin/main

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
<<<<<<< HEAD
 * Dashboard card for the transactions tab. Extends {@link SortableTableCard}
 * for shared pagination, search, sort state, and the common refresh Template Method.
 *
 * <p>The default order is chronological (oldest first). The filter row holds a
 * {@link SearchBar}, a {@link TransactionTypeFilter} (all / buy / sell), and a
 * {@link WeekRangeFilter} shared with {@code TransactionsSummaryCard} so both
 * cards show data for the same period. {@link #buildSearchRow} is overridden to
 * include these extra filters.</p>
 */
public class TransactionsCard extends SortableTableCard<Transaction, TransactionsSort.SortColumn> {

    private static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;
    private static final double ROW_HEIGHT = 34.0;

    private final GameService gameService;
    private final TransactionStatsService statsService;
    private final TransactionsSort sort;
    private final StyledText title;
    private final TransactionTypeFilter typeFilter;
=======
 * Body component for the Aksjehandel sub-tab on the Transactions tab.
 * Renders a filter row and a table of every committed stock transaction
 * within the current filter selection, sorted oldest first.
 *
 * <p>This class is a plain {@link VBox} — no card chrome. The surrounding
 * {@link Card} and title are owned by {@code TransactionsView}, which wraps
 * this body alongside the loan-ledger body in a single outer card.</p>
 *
 * <p>The {@link WeekRangeFilter} is supplied by {@code TransactionsView}
 * and shared with the summary and activity cards so all three show data
 * for the same period. The type filter is local to this body.</p>
 */
public class TransactionsCard extends VBox implements GameObserver {

    /** Number of columns — used for empty-state column span. */
    private static final int COLUMN_COUNT = 8;

    /** Percentage widths for each column; sums to 100. */
    private static final double[] COLUMN_WIDTHS = {8, 26, 8, 7, 12, 12, 13, 14};

    /** Text columns align left, numeric columns right. */
    private static final HPos[] COLUMN_ALIGNMENTS = {
            HPos.LEFT, HPos.LEFT, HPos.LEFT, HPos.RIGHT,
            HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT
    };

    private final GameService gameService;
    private final TransactionStatsService statsService = new TransactionStatsService();
    private final LedgerTypeFilter<Class<? extends Transaction>> typeFilter;
>>>>>>> origin/main
    private final WeekRangeFilter weekRangeFilter;

    /**
<<<<<<< HEAD
     * Constructs a new TransactionsCard.
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes both this card's
     *                        table and the summary card's totals
     * @param statsService    the service used to compute per-transaction stats for display and sorting
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter,
                            TransactionStatsService statsService) {
        super(gameService, PAGE_SIZE, "transactions.status", "transactions.empty");
=======
     * @param gameService     game manager containing player and exchange
     * @param weekRangeFilter shared filter that scopes both this body's
     *                        table and the summary/activity card totals
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
>>>>>>> origin/main
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;
        this.statsService = statsService;
        this.sort = new TransactionsSort(statsService, gameService.getCurrencyConverter());
        this.sortProvider = sort;
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);
        table.setMinHeight(PAGE_SIZE * ROW_HEIGHT);

        gameService.addObserver(this);
        LanguageManager.addObserver(this::refresh);

        setSpacing(16);

        typeFilter = new LedgerTypeFilter<>(List.of(
                new LedgerTypeFilter.TypeOption<>("transactions.type.all", null),
                new LedgerTypeFilter.TypeOption<>("transactions.type.buy", Purchase.class),
                new LedgerTypeFilter.TypeOption<>("transactions.type.sell",
                        edu.ntnu.idatt2003.millions.model.transaction.Sale.class)
        ));
        typeFilter.selectedValueProperty().addListener((_, _, _) -> refresh());

<<<<<<< HEAD
        typeFilter = new TransactionTypeFilter();
        typeFilter.selectedTypeProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());

        weekRangeFilter.fromWeekProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());
        weekRangeFilter.toWeekProperty().addListener((obs, oldVal, newVal) -> resetPageAndRefresh());

        getChildren().addAll(title, buildSearchRow(clearSortButton), table.asNode(), pagination);
        refresh();
    }

    /**
     * Overrides the default search row to include {@link TransactionTypeFilter}
     * and {@link WeekRangeFilter} alongside the search bar.
     *
     * @param clearSortButton the button from
     *        {@link SortColumnTable#createClearSortButton}
     * @return the configured filter row
     */
    @Override
    protected HBox buildSearchRow(Button clearSortButton) {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                searchCallback(),
                metadataRow);
        searchBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);
=======
        weekRangeFilter.fromWeekProperty().addListener((_, _, _) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((_, _, _) -> refresh());

        HBox filterRow = buildFilterRow();

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(filterRow, grid);
        refresh();
    }

    private HBox buildFilterRow() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
>>>>>>> origin/main

        HBox row = new HBox(16, searchBar, typeFilter, weekRangeFilter, clearSortButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

<<<<<<< HEAD
    /**
     * Returns transactions scoped to the active week range and type filter,
     * sorted chronologically. Returns an empty list when the player is null.
     *
     * @return mutable list of pre-filtered transactions, oldest first
     */
    @Override
    protected List<Transaction> fetchAll() {
        if (gameService.getPlayer() == null) {
            return new ArrayList<>();
        }
        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();
        Class<? extends Transaction> selectedType = typeFilter.getSelectedType();

        return new ArrayList<>(collectRange(archive, fromWeek, toWeek).stream()
                .filter(t -> selectedType == null || selectedType.isInstance(t))
                .toList());
    }

    @Override
    protected List<Transaction> applySearch(List<Transaction> all, String term) {
        if (term.isBlank()) {
            return new ArrayList<>(all);
        }
        return new ArrayList<>(all.stream()
                .filter(t -> t.getShare().getStock().matches(term))
                .toList());
    }

    @Override
    protected void renderPage(List<Transaction> page) {
        int row = 1;
        for (Transaction transaction : page) {
            addDataRow(row++, transaction);
        }
    }

    /**
     * Extends the week range filter's upper bound whenever the model changes.
     */
=======
>>>>>>> origin/main
    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

<<<<<<< HEAD
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
                    LanguageManager.get("transactions.empty.search"), term);
        }
        return LanguageManager.get("transactions.empty");
    }

    /**
     * Refreshes the section title and rebuilds the table for the active language.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.title"));
        refresh();
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
=======
    private void refresh() {
        grid.getChildren().clear();
        if (gameService.getPlayer() == null) return;
        TableCells.addHeaderRow(grid, headerTexts());

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();
        Class<? extends Transaction> selectedType = typeFilter.getSelectedValue();

        List<Transaction> transactions = collectRange(archive, fromWeek, toWeek).stream()
                .filter(t -> selectedType == null || selectedType.isInstance(t))
                .toList();

        if (transactions.isEmpty()) {
            TableCells.renderEmptyState(
                    grid, LanguageManager.get("transactions.empty"), COLUMN_COUNT);
            return;
        }

        int row = 1;
        for (Transaction transaction : transactions) {
            addDataRow(row++, transaction);
        }
    }

    private String[] headerTexts() {
        return new String[] {
                LanguageManager.get("transactions.col.week"),
                LanguageManager.get("transactions.col.company"),
                LanguageManager.get("transactions.col.type"),
                LanguageManager.get("transactions.col.quantity"),
                LanguageManager.get("transactions.col.price"),
                LanguageManager.get("transactions.col.commission"),
                LanguageManager.get("transactions.col.tax"),
                LanguageManager.get("transactions.col.amount")
        };
    }

>>>>>>> origin/main
    private List<Transaction> collectRange(TransactionArchive archive, int fromWeek, int toWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = fromWeek; week <= toWeek; week++) {
            list.addAll(archive.getTransactions(week));
        }
        list.sort(Comparator.comparingInt(Transaction::getWeek));
        return list;
    }

<<<<<<< HEAD
    /**
     * Renders one transaction as a data row in the table.
     *
     * @param row         the table row index to write to
     * @param transaction the transaction to render
     */
=======
>>>>>>> origin/main
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

<<<<<<< HEAD
    /**
     * Creates the pill-shaped type badge for a transaction.
     *
     * @param transaction the transaction to label
     * @return a styled badge label
     */
=======
>>>>>>> origin/main
    private Label typeBadge(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String key = isPurchase ? "transactions.type.buy" : "transactions.type.sell";
        Label badge = new Label(LanguageManager.get(key));
        badge.getStyleClass().add("transaction-type-badge");
        badge.getStyleClass().add(isPurchase ? "badge-buy" : "badge-sell");
        return badge;
    }

<<<<<<< HEAD
    /**
     * Renders the tax cell. Purchases show an en-dash instead of "0,00 NOK".
     *
     * @param stats the row stats supplying the NOK tax amount
     * @return a styled cell label
     */
=======
>>>>>>> origin/main
    private Label taxCell(TransactionStatsService.TransactionStats stats) {
        if (stats.taxNok().signum() == 0) {
            return TableCells.data("–");
        }
        return TableCells.data(TableCells.NUMBER_FORMAT.format(stats.taxNok()) + " NOK");
    }
}
