package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.dashboard.transactions.component.WeekRangeFilter;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

/**
 * Dashboard card that summarises transactions over the selected week
 * range: total purchase outflow, total sale inflow, and the net.
 *
 * <p>Listens to the shared {@link WeekRangeFilter} owned by
 * {@code TransactionsCard}. The type filter is not applied here - the card exists to compare
 * purchases against sales, and filtering to one type would zero out the other row.</p>
 *
 * <p>All aggregation is delegated to
 * {@link TransactionStatsService#getSummary} so the card stays a
 * thin presentation layer over a stateless read service.</p>
 */
public class TransactionsSummaryCard extends Card {

    /** Spacing between the header row and the value grid. */
    private static final int CARD_SPACING = 16;

    /** Horizontal gap between the label and value columns. */
    private static final int VALUE_HGAP = 20;

    /** Equal-width two-column layout: label left, NOK amount right. */
    private static final double[] COLUMN_WIDTHS = {50, 50};
    private static final HPos[] COLUMN_ALIGNMENTS = {HPos.LEFT, HPos.RIGHT};

    private final GameService gameService;
    private final TransactionStatsService statsService = new TransactionStatsService();
    private final WeekRangeFilter weekRangeFilter;

    private final StyledText title;
    private final StyledText weekRangeLabel;
    private final GridPane grid = new GridPane();

    /**
     * @param gameService     game manager containing player and exchange
     * @param weekRangeFilter shared filter that scopes the table this
     *                        card summarises
     */
    public TransactionsSummaryCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

        setSpacing(CARD_SPACING);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.summary.title"));
        weekRangeLabel = StyledText.weekLabel();

        weekRangeFilter.fromWeekProperty().addListener((_, _, _) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((_, _, _) -> refresh());

        HBox header = buildHeader();

        grid.setHgap(VALUE_HGAP);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(header, grid);
        refresh();
    }

    /** Title on the left, flexible spacer, week-range label on the right. */
    private HBox buildHeader() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, weekRangeLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    @Override
    public void onGameUpdated() {
        refresh();
    }

    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.summary.title"));
        refresh();
    }

    /**
     * Rebuilds the value rows from the current filter selection and
     * updates the week-range label. The Total row uses the same
     * divider as {@code HoldingsCard} so the two cards read as a
     * visual family.
     *
     * <p>Transactions are fetched via
     * {@link TransactionArchive#getTransactionsInRange} without additional
     * sorting — ordering does not matter for the summary aggregation, so
     * the newest-first sort that {@code TransactionsCard} applies is
     * intentionally skipped here.</p>
     */
    private void refresh() {
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();

        weekRangeLabel.setText(MessageFormat.format(
                LanguageManager.get("transactions.summary.weekRange"), fromWeek, toWeek));

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        List<Transaction> transactions = archive.getTransactionsInRange(fromWeek, toWeek);
        TransactionStatsService.TransactionSummary summary =
                statsService.getSummary(transactions, gameService.getCurrencyConverter());

        grid.getChildren().clear();

        grid.add(rowLabel(LanguageManager.get("transactions.summary.purchases"), false), 0, 0);
        grid.add(rowValue(summary.purchasesNok(), false), 1, 0);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.sales"), false), 0, 1);
        grid.add(rowValue(summary.salesNok(), false), 1, 1);

        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, 2);
        grid.add(divider, 0, 2);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.total"), true), 0, 3);
        grid.add(rowValue(summary.totalNok(), true), 1, 3);
    }

    /** Left-column label; bold variant is used for the total row. */
    private Label rowLabel(String text, boolean bold) {
        Label label = TableCells.data(text);
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }

    /**
     * Signed NOK amount. Positives are rendered with an explicit
     * {@code +} so sale inflows read as "+1 234,56 NOK"; negatives
     * keep the minus produced by {@link edu.ntnu.idatt2003.millions.util.MoneyFormatter};
     * zero is unsigned. No color modifiers — the summary keeps a
     * neutral palette and only weight distinguishes the total row.
     */
    private Label rowValue(BigDecimal amount, boolean bold) {
        String sign = amount.signum() > 0 ? "+" : "";
        String text = sign + MoneyFormatter.format(amount) + " NOK";
        Label label = TableCells.data(text);
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }

}