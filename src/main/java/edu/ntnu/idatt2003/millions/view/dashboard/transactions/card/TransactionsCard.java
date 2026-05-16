package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.TransactionsSort;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.LedgerTypeFilter;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard card for the transactions tab. Extends {@link SortableTableCard}
 * for shared pagination, search, sort state, and the common refresh Template Method.
 *
 * <p>The default order is chronological (oldest first). The filter row holds a
 * {@link SearchBar}, a {@link LedgerTypeFilter} (all / buy / sell), and a
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
    private final LedgerTypeFilter<Class<? extends Transaction>> typeFilter;
    private final WeekRangeFilter weekRangeFilter;

    /**
     * Constructs a new TransactionsCard.
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes both this card's
     *                        table and the summary card's totals
     * @param statsService    the service used to compute per-transaction stats
     *                        for display and sorting
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter,
                            TransactionStatsService statsService) {
        super(gameService, PAGE_SIZE, "transactions.status", "transactions.empty");
        getStyleClass().remove("card");
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

        typeFilter = new LedgerTypeFilter<>(List.of(
                new LedgerTypeFilter.TypeOption<>("transactions.type.all", null),
                new LedgerTypeFilter.TypeOption<>("transactions.type.buy", Purchase.class),
                new LedgerTypeFilter.TypeOption<>("transactions.type.sell",
                        edu.ntnu.idatt2003.millions.model.transaction.Sale.class)
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
     * and {@link WeekRangeFilter} alongside the search bar.
     *
     * @param clearSortButton the button from
     *        {@link SortColumnTable#createClearSortButton}
     * @return the configured filter row
     */
    @Override
    protected HBox buildSearchRow(Button clearSortButton) {
        SearchBar searchBar = new SearchBar(
                "transactions.search.placeholder",
                "search.button",
                searchCallback(),
                metadataRow);
        HBox row = new HBox(36, searchBar, typeFilter, weekRangeFilter, clearSortButton);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

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
        Class<? extends Transaction> selectedType = typeFilter.getSelectedValue();

        return new ArrayList<>(collectRange(archive, fromWeek, toWeek).stream()
                .filter(t -> selectedType == null || selectedType.isInstance(t))
                .toList());
    }

    /**
     * Filters transactions by ticker or company name matching the search term.
     *
     * @param all  the full transaction list from {@link #fetchAll()}
     * @param term the active search term; may be blank
     * @return a new mutable list of matching transactions
     */
    @Override
    protected List<Transaction> applySearch(List<Transaction> all, String term) {
        if (term.isBlank()) {
            return new ArrayList<>(all);
        }
        return new ArrayList<>(all.stream()
                .filter(t -> t.getShare().getStock().matches(term))
                .toList());
    }

    /**
     * Renders one page of transactions into the table.
     *
     * @param page the sub-list of transactions for the current page
     */
    @Override
    protected void renderPage(List<Transaction> page) {
        int row = 1;
        for (Transaction transaction : page) {
            addDataRow(row++, transaction);
        }
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
                    LanguageManager.get("transactions.empty.search"), term);
        }
        return LanguageManager.get("transactions.empty");
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
     * Gathers every transaction in the archive within the given week range
     * (inclusive on both ends), sorted chronologically oldest first.
     *
     * @param archive  the archive to read transactions from
     * @param fromWeek the first week to include (inclusive)
     * @param toWeek   the last week to include (inclusive)
     * @return a reverse-chronologically sorted mutable list of transactions in the range
     */
    private List<Transaction> collectRange(TransactionArchive archive, int fromWeek, int toWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = fromWeek; week <= toWeek; week++) {
            list.addAll(archive.getTransactions(week));
        }
        list.sort(Comparator.comparingInt(Transaction::getWeek).reversed());
        return list;
    }

    /**
     * Renders one transaction as a data row in the table.
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
                TableCells.data(MoneyFormatter.format(stats.quantity())),
                TableCells.data(MoneyFormatter.format(stats.pricePerShare())
                        + " " + CurrencyFormatter.symbol(stats.nativeCurrencyCode())),
                TableCells.data(MoneyFormatter.format(stats.commissionNok())
                        + " " + CurrencyFormatter.symbol("NOK")),
                taxCell(stats),
                amountCell(stats.amountNok())
        );
    }

    /**
     * Creates the pill-shaped type badge for a transaction.
     *
     * @param transaction the transaction to label
     * @return a styled badge label
     */
    private Label typeBadge(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String key = isPurchase ? "transactions.type.buy" : "transactions.type.sell";
        Label badge = new Label(LanguageManager.get(key).toUpperCase());
        badge.getStyleClass().add("transaction-type-badge");
        badge.getStyleClass().add(isPurchase ? "badge-buy" : "badge-sell");
        return badge;
    }

    /**
     * Creates a coloured signed amount label for the Beløp column,
     * with the NOK currency symbol appended.
     *
     * @param amountNok the NOK amount to format
     * @return a styled label with currency symbol
     */
    private Label amountCell(java.math.BigDecimal amountNok) {
        Label label = ChangeFormatter.styledAmount(amountNok, "holdings-cell");
        label.setText(label.getText() + " " + CurrencyFormatter.symbol("NOK"));
        return label;
    }

    /**
     * Renders the tax cell. Purchases show an en-dash instead of "0,00 NOK".
     *
     * @param stats the row stats supplying the NOK tax amount
     * @return a styled cell label
     */
    private Label taxCell(TransactionStatsService.TransactionStats stats) {
        if (stats.taxNok().signum() == 0) {
            return TableCells.data("–");
        }
        return TableCells.data(MoneyFormatter.format(stats.taxNok())
                + " " + CurrencyFormatter.symbol("NOK"));
    }
}
