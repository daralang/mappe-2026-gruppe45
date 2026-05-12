package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.TransactionTypeFilter;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
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
 * <p>Renders the section title, a filter row, and a table of every
 * committed transaction within the current filter selection, sorted
 * oldest first. The filter row holds a {@link TransactionTypeFilter}
 * (all / buy / sell) and a {@link WeekRangeFilter}; a search field can
 * be added to the same row later without restructuring the card.</p>
 *
 * <p>The {@link WeekRangeFilter} is supplied by {@code TransactionsView}
 * and shared with {@code TransactionsSummaryCard} so both cards on the
 * tab show data for the same period. This card owns the visible spinner
 * widget; the summary card only echoes the current range as a read-only
 * label. The type filter, on the other hand, is local to this card —
 * the summary's whole purpose is to compare purchases against sales, so
 * filtering it by type would zero out one of the two rows.</p>
 *
 * <p>Shares structure and CSS with {@code HoldingsCard} via
 * {@link TableCells} (column setup, header row, cell factories, empty
 * state), {@link ChangeFormatter} (coloured signed amounts) and the
 * {@code holdings-*} CSS classes, so both dashboard tables look like
 * part of the same family.</p>
 *
 * <p>The type column uses a pill-shaped badge with its own colour scheme
 * (light blue for buys, light green for sales); the helper that builds
 * the badge will be extracted into a reusable {@code TransactionTypeBadge}
 * component once we have a second caller for it.</p>
 */
public class TransactionsCard extends Card {

    /** Number of columns in the table — used for empty-state column span. */
    private static final int COLUMN_COUNT = 8;

    /** Percentage widths for each column; sums to 100. */
    private static final double[] COLUMN_WIDTHS = {8, 26, 8, 7, 12, 12, 13, 14};

    /**
     * Horizontal alignment per column: text columns (week, company, type)
     * align left so labels read naturally; numeric columns align right so
     * digits line up cleanly.
     */
    private static final HPos[] COLUMN_ALIGNMENTS = {
            HPos.LEFT, HPos.LEFT, HPos.LEFT, HPos.RIGHT,
            HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT
    };

    private final GameService gameService;
    private final TransactionStatsService statsService = new TransactionStatsService();
    private final StyledText title;
    private final TransactionTypeFilter typeFilter;
    private final WeekRangeFilter weekRangeFilter;
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new TransactionsCard.
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes both this card's
     *                        table and the summary card's totals
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

        setSpacing(16);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));

        typeFilter = new TransactionTypeFilter();
        typeFilter.selectedTypeProperty().addListener((obs, oldVal, newVal) -> refresh());

        weekRangeFilter.fromWeekProperty().addListener((obs, oldVal, newVal) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((obs, oldVal, newVal) -> refresh());

        HBox filterRow = buildFilterRow();

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(title, filterRow, grid);
        refresh();
    }

    /**
     * Builds the filter row that sits between the title and the table.
     *
     * <p>Both filters sit together on the left edge with a small gap
     * between them; a flexible spacer takes up the remaining width so
     * future filter controls can be inserted next to the existing ones
     * without disturbing the layout.</p>
     *
     * @return the configured filter row
     */
    private HBox buildFilterRow() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(36, typeFilter, weekRangeFilter, spacer);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

    /**
     * Picks up new transactions and the new current week whenever the
     * model changes. Also extends the week range filter's upper bound
     * so the player can include the new week in the filter.
     */
    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

    /**
     * Refreshes the section title and rebuilds the table so column
     * headers, badge labels and the empty-state message follow the
     * active language.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.title"));
        refresh();
    }

    /**
     * Rebuilds the table from scratch: clears the grid, adds the header
     * row, then either renders a centered empty-state message or one
     * row per transaction that passes the current filters.
     */
    private void refresh() {
        grid.getChildren().clear();
        if (gameService.getPlayer() == null) return;
        TableCells.addHeaderRow(grid, headerTexts());

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();
        Class<? extends Transaction> selectedType = typeFilter.getSelectedType();

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

    /**
     * Resolves the localized header text for each column. Returns a fresh
     * array on every call so a language switch picks up new translations.
     *
     * @return one localized string per column
     */
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

    /**
     * Gathers every transaction in the archive within the given week
     * range (inclusive on both ends), then sorts them oldest first so
     * the table reads chronologically from top to bottom.
     *
     * @param archive  the archive to read transactions from
     * @param fromWeek the first week to include (inclusive)
     * @param toWeek   the last week to include (inclusive)
     * @return a chronologically sorted list of transactions in the range
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
     * Renders one transaction as a row in the table. Delegates value
     * computation to {@link TransactionStatsService#getStats} so this method
     * only deals with cell placement and formatting.
     *
     * @param row         the grid row index to write to
     * @param transaction the transaction to render
     */
    private void addDataRow(int row, Transaction transaction) {
        Stock stock = transaction.getShare().getStock();
        TransactionStatsService.TransactionStats stats =
                statsService.getStats(transaction, gameService.getCurrencyConverter());

        grid.add(TableCells.data(MessageFormat.format(
                LanguageManager.get("transactions.weekValue"), transaction.getWeek())), 0, row);
        grid.add(TableCells.data(stock.getSymbol() + ", " + stock.getCompany()), 1, row);
        grid.add(typeBadge(transaction), 2, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(stats.quantity())), 3, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(stats.pricePerShare())
                + " " + stats.nativeCurrencyCode()), 4, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(stats.commissionNok()) + " NOK"), 5, row);
        grid.add(taxCell(stats), 6, row);
        grid.add(ChangeFormatter.styledAmount(stats.amountNok(), "holdings-cell"), 7, row);
    }

    /**
     * Creates the pill-shaped type badge for a transaction. KJØP gets
     * a light-blue colour scheme, SALG a light-green one.
     *
     * <p>Lives as a private helper for now; will be extracted into a
     * reusable {@code TransactionTypeBadge} component once a second
     * caller (receipts, details modal) needs it.</p>
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