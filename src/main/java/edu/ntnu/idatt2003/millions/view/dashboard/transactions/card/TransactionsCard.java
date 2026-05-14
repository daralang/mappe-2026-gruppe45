package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.transaction.Purchase;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
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
    private final WeekRangeFilter weekRangeFilter;
    private final GridPane grid = new GridPane();

    /**
     * @param gameService     game manager containing player and exchange
     * @param weekRangeFilter shared filter that scopes both this body's
     *                        table and the summary/activity card totals
     */
    public TransactionsCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

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

        HBox row = new HBox(36, typeFilter, weekRangeFilter, spacer);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("transactions-filter-row");
        return row;
    }

    @Override
    public void onGameUpdated() {
        weekRangeFilter.setMaxWeek(Math.max(gameService.getExchange().getWeek(), 1));
        refresh();
    }

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

    private List<Transaction> collectRange(TransactionArchive archive, int fromWeek, int toWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = fromWeek; week <= toWeek; week++) {
            list.addAll(archive.getTransactions(week));
        }
        list.sort(Comparator.comparingInt(Transaction::getWeek));
        return list;
    }

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

    private Label typeBadge(Transaction transaction) {
        boolean isPurchase = transaction instanceof Purchase;
        String key = isPurchase ? "transactions.type.buy" : "transactions.type.sell";
        Label badge = new Label(LanguageManager.get(key));
        badge.getStyleClass().add("transaction-type-badge");
        badge.getStyleClass().add(isPurchase ? "badge-buy" : "badge-sell");
        return badge;
    }

    private Label taxCell(TransactionStatsService.TransactionStats stats) {
        if (stats.taxNok().signum() == 0) {
            return TableCells.data("–");
        }
        return TableCells.data(TableCells.NUMBER_FORMAT.format(stats.taxNok()) + " NOK");
    }
}
