package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.TransactionStatsService;
import edu.ntnu.idatt2003.millions.model.transaction.Transaction;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard card that summarises the user's transactions over the
 * week range selected in {@code TransactionsCard}: total purchase
 * outflow, total sale inflow, and the net of the two.
 *
 * <p>Layout: a header row with the section title on the left and a
 * read-only "Uke {from}–{to}" label on the right, followed by three
 * label/value rows (Kjøp, Salg, Total). The Total row is separated
 * from the others by the same divider {@code HoldingsCard} uses for
 * its total row, so the two cards read as part of the same visual
 * family.</p>
 *
 * <p>The card does not own a filter widget. It listens to the
 * {@link WeekRangeFilter} that {@code TransactionsView} created and
 * handed to both cards on the tab, so the summary always reflects
 * the same period the table shows. The spinner widget itself lives
 * inside {@code TransactionsCard}'s filter row; rendering it a
 * second time here would duplicate a control without adding any
 * functionality, and letting the summary have its own range would
 * make two cards on the same tab show data for different periods —
 * a confusing UX. The card shows the range as a plain "Uke
 * {from}–{to}" label that updates the moment the user changes the
 * spinner in the table card above.</p>
 *
 * <p>The type filter from the transactions table is intentionally
 * not applied here either: the whole point of the summary is to
 * compare purchases against sales, so filtering to one type would
 * zero out the other row and remove the comparison this card exists
 * to show.</p>
 *
 * <p>All aggregation is delegated to
 * {@link TransactionStatsService#getSummary} so the card stays a thin
 * presentation layer over a stateless read service.</p>
 */
public class TransactionsSummaryCard extends Card {

    /** Spacing between the header row and the value grid. */
    private static final int CARD_SPACING = 16;

    /** Horizontal gap between label and value columns in the value grid. */
    private static final int VALUE_HGAP = 20;

    /**
     * Column widths for the two-column value grid. Left column carries
     * the label ("Kjøp" / "Salg" / "Total"), right column the NOK amount
     * right-aligned to keep digits aligned across the three rows.
     */
    private static final double[] COLUMN_WIDTHS = {50, 50};

    /** Left-aligned labels, right-aligned values. */
    private static final HPos[] COLUMN_ALIGNMENTS = {HPos.LEFT, HPos.RIGHT};

    private final GameService gameService;
    private final TransactionStatsService statsService = new TransactionStatsService();
    private final WeekRangeFilter weekRangeFilter;

    private final StyledText title;
    private final StyledText weekRangeLabel;
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new TransactionsSummaryCard.
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes the table this
     *                        card summarises; the card listens to it but
     *                        does not render its spinner widget
     */
    public TransactionsSummaryCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

        setSpacing(CARD_SPACING);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.summary.title"));
        weekRangeLabel = StyledText.weekLabel();

        weekRangeFilter.fromWeekProperty().addListener((obs, oldVal, newVal) -> refresh());
        weekRangeFilter.toWeekProperty().addListener((obs, oldVal, newVal) -> refresh());

        HBox header = buildHeader();

        grid.setHgap(VALUE_HGAP);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(header, grid);
        refresh();
    }

    /**
     * Builds the title row: section title on the left, a flexible
     * spacer in the middle, and a read-only "Uke {from}–{to}" label
     * on the right. The label echoes the spinner that lives in
     * {@code TransactionsCard} above; it is not interactive itself.
     *
     * @return the configured header row
     */
    private HBox buildHeader() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, weekRangeLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /**
     * Refreshes value rows whenever the model changes — the filter's
     * upper bound is bumped by {@code TransactionsCard}, so this card
     * only needs to recompute the totals against the (possibly new)
     * transactions.
     */
    @Override
    public void onGameUpdated() {
        refresh();
    }

    /**
     * Refreshes the section title, the week-range label, and the row
     * contents so labels and value formatting follow the active
     * language.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.summary.title"));
        refresh();
    }

    /**
     * Rebuilds the value rows from the current filter selection and
     * updates the header's week-range label. The three rows (Kjøp,
     * Salg, Total) are laid out with the Total row separated by the
     * same divider {@code HoldingsCard} uses, so the two cards read
     * as a visual family.
     */
    private void refresh() {
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();

        weekRangeLabel.setText(MessageFormat.format(
                LanguageManager.get("transactions.summary.weekRange"), fromWeek, toWeek));

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        List<Transaction> transactions = collectRange(archive, fromWeek, toWeek);
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

    /**
     * Creates a label for the leftmost column. Bold variant is used for
     * the total row to match the {@code HoldingsCard} total-row styling.
     *
     * @param text the label text
     * @param bold whether to apply the bold modifier
     * @return a styled label
     */
    private Label rowLabel(String text, boolean bold) {
        Label label = TableCells.data(text);
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }

    /**
     * Creates a signed NOK amount label for the rightmost column. The
     * positive sign is rendered explicitly so sale inflows display as
     * {@code +1 234,56 NOK} rather than just {@code 1 234,56 NOK}, which
     * matches the mockup and the visual cue used elsewhere in the app.
     * Negative values keep the minus sign produced by
     * {@link TableCells#NUMBER_FORMAT}; zero is shown without a sign.
     * Color modifiers are not applied here — the summary card keeps a
     * neutral palette, only weight distinguishes the total row.
     *
     * @param amount the NOK amount to render
     * @param bold   whether to apply the bold modifier
     * @return a styled label
     */
    private Label rowValue(BigDecimal amount, boolean bold) {
        String sign = amount.signum() > 0 ? "+" : "";
        String text = sign + TableCells.NUMBER_FORMAT.format(amount) + " NOK";
        Label label = TableCells.data(text);
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }

    /**
     * Gathers every transaction in the archive within the given week
     * range (inclusive on both ends). Ordering does not matter for the
     * summary — only the sum does — so this method skips the sort that
     * {@code TransactionsCard} performs on the same data.
     *
     * @param archive  the archive to read transactions from
     * @param fromWeek the first week to include (inclusive)
     * @param toWeek   the last week to include (inclusive)
     * @return the transactions in the range, in archive order
     */
    private List<Transaction> collectRange(TransactionArchive archive, int fromWeek, int toWeek) {
        List<Transaction> list = new ArrayList<>();
        for (int week = fromWeek; week <= toWeek; week++) {
            list.addAll(archive.getTransactions(week));
        }
        return list;
    }
}