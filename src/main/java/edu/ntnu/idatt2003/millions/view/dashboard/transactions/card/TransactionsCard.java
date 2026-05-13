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
import edu.ntnu.idatt2003.millions.view.component.card.Card;
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
 * Dashboard card for the transactions tab. Renders the section title,
 * a filter row, and a table of every committed transaction within the
 * current filter selection, sorted oldest first.
 *
 * <p>The {@link WeekRangeFilter} is supplied by {@code TransactionsView}
 * and shared with the summary and activity cards on the tab, so all
 * three show data for the same period. This card owns the visible
 * spinner widget; the others only echo the current range as a label.
 * The type filter is local to this card - filtering the summary by
 * type would zero out one of its two comparison rows.</p>
 *
 * <p>Shares structure and CSS with {@code HoldingsCard} via
 * {@link TableCells} and {@link ChangeFormatter}, so both dashboard
 * tables look like part of the same family.</p>
 */
public class TransactionsCard extends Card {

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
    private final StyledText title;
    private final TransactionTypeFilter typeFilter;
    private final WeekRangeFilter weekRangeFilter;
    private final GridPane grid = new GridPane();

    /**
     * @param gameService     game manager containing player and exchange
     * @param weekRangeFilter shared filter that scopes both this card's
     *                        table and the summary/activity card totals
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

        setSpacing(16);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.title"));

        typeFilter = new TransactionTypeFilter();
        typeFilter.selectedTypeProperty().addListener((_, _, _) -> refresh());

        weekRangeFilter.fromWeekProperty().addListener((_, _, _) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((_, _, _) -> refresh());

        HBox filterRow = buildFilterRow();

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(title, filterRow, grid);
        refresh();
    }

    /**
     * Builds the filter row between the title and the table. Filters
     * sit together on the left; a flexible spacer leaves room for
     * additional controls to be inserted later.
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
     * Extends the filter's upper bound to the new current week, then
     * refreshes so the player can include the new week in the filter.
     */
    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.title"));
        refresh();
    }

    /**
     * Rebuilds the table: header row, then either an empty-state
     * message or one row per transaction that passes the filters.
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

    /** Localized column headers, freshly resolved on every call. */
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
     * Gathers transactions in the inclusive week range and sorts them
     * oldest to newest so the table reads chronologically top to bottom.
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
     * Renders one transaction as a row. Value computation is delegated
     * to {@link TransactionStatsService}; this method only handles
     * cell placement and formatting.
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
     * Pill-shaped type badge: light-blue for buys, light-green for
     * sales. Kept as a private helper for now; will be extracted into
     * a reusable {@code TransactionTypeBadge} component once a second
     * caller needs it.
     */
    private Label typeBadge(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String key = isPurchase ? "transactions.type.buy" : "transactions.type.sell";
        Label badge = new Label(LanguageManager.get(key));
        badge.getStyleClass().add("transaction-type-badge");
        badge.getStyleClass().add(isPurchase ? "badge-buy" : "badge-sell");
        return badge;
    }

    /** Tax cell — purchases have no tax, so an en-dash is shown instead of "0,00 NOK". */
    private Label taxCell(TransactionStatsService.TransactionStats stats) {
        if (stats.taxNok().signum() == 0) {
            return TableCells.data("–");
        }
        return TableCells.data(TableCells.NUMBER_FORMAT.format(stats.taxNok()) + " NOK");
    }
}