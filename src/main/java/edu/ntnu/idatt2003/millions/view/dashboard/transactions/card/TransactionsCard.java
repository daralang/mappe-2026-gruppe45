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
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard card for the transactions tab.
 *
 * <p>Renders the section title and a table of every committed transaction
 * from week 1 up to and including the current game week, sorted oldest
 * first. Filter controls (search, type, week range) are deliberately not
 * yet wired up — they will be layered on top in a follow-up change and
 * will only narrow which rows are rendered, not how a row is rendered.</p>
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
    private static final double[] COLUMN_WIDTHS = {8, 28, 6, 7, 12, 12, 13, 14};

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
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new TransactionsCard.
     *
     * @param gameService the game manager containing player and exchange
     */
    public TransactionsCard(GameService gameService) {
        super(gameService);
        this.gameService = gameService;

        setSpacing(16);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(title, grid);
        refresh();
    }

    /**
     * Picks up new transactions and the new current week whenever the
     * model changes.
     */
    @Override
    public void onGameUpdated() {
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
     * row per transaction.
     */
    private void refresh() {
        grid.getChildren().clear();
        TableCells.addHeaderRow(grid, headerTexts());

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int currentWeek = gameService.getExchange().getWeek();
        List<Transaction> transactions = collectAll(archive, currentWeek);

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
     * Gathers every transaction in the archive from week 1 up to and
     * including the current week, then sorts them oldest first so the
     * table reads chronologically from top to bottom.
     *
     * @param archive     the archive to read transactions from
     * @param currentWeek the current game week (inclusive upper bound)
     * @return a chronologically sorted list of all transactions
     */
    private List<Transaction> collectAll(TransactionArchive archive, int currentWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = 1; week <= currentWeek; week++) {
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