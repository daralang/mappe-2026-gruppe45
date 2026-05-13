package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.GameService;
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

import java.text.MessageFormat;

/**
 * Dashboard card that counts the user's trading activity over the
 * week range selected in {@code TransactionsCard}: number of
 * purchases, number of sales, and their sum.
 *
 * <p>Sits beside {@code TransactionsSummaryCard} on the transactions
 * tab. Where the summary card answers "how much money moved", this
 * card answers "how often did I trade" — two different questions
 * over the same period, so the two cards complement each other
 * rather than overlap.</p>
 *
 * <p>Layout mirrors the summary card exactly: section title on the
 * left, read-only "UKE {from}-{to}" label on the right, then Kjøp /
 * Salg / Total rows separated by the same divider {@code HoldingsCard}
 * uses for its total row. Sharing the structure keeps the two cards
 * reading as a visual family.</p>
 *
 * <p>The card does not own a filter widget. It listens to the
 * {@link WeekRangeFilter} that {@code TransactionsView} created and
 * handed to all three components on the tab (the table card, the
 * summary card, and this card), so they always show data for the
 * same period. The spinner lives in {@code TransactionsCard}; this
 * card only echoes the current range as a label.</p>
 *
 * <p>Counts come straight from {@link TransactionArchive#getPurchases}
 * and {@link TransactionArchive#getSales}. No conversion or
 * aggregation service is needed — these are plain list sizes.</p>
 */
public class TransactionsActivityCard extends Card {

    /** Spacing between the header row and the value grid. */
    private static final int CARD_SPACING = 16;

    /** Horizontal gap between label and value columns in the value grid. */
    private static final int VALUE_HGAP = 20;

    /**
     * Column widths for the two-column value grid. Left column carries
     * the label, right column the integer count, right-aligned so
     * digits stack cleanly across the three rows.
     */
    private static final double[] COLUMN_WIDTHS = {50, 50};

    /** Left-aligned labels, right-aligned values. */
    private static final HPos[] COLUMN_ALIGNMENTS = {HPos.LEFT, HPos.RIGHT};

    private final GameService gameService;
    private final WeekRangeFilter weekRangeFilter;

    private final StyledText title;
    private final StyledText weekRangeLabel;
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new TransactionsActivityCard.
     *
     * @param gameService     the game manager containing player and exchange
     * @param weekRangeFilter the shared filter that scopes the table this
     *                        card counts against; the card listens to it
     *                        but does not render its spinner widget
     */
    public TransactionsActivityCard(GameService gameService, WeekRangeFilter weekRangeFilter) {
        super(gameService);
        this.gameService = gameService;
        this.weekRangeFilter = weekRangeFilter;

        setSpacing(CARD_SPACING);

        title = StyledText.sectionTitle(LanguageManager.get("transactions.activity.title"));
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
     * spacer in the middle, and the read-only "UKE {from}-{to}" label
     * on the right.
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
     * Picks up new transactions whenever the model changes. The
     * filter's upper bound is bumped by {@code TransactionsCard}, so
     * this card only needs to recount.
     */
    @Override
    public void onGameUpdated() {
        refresh();
    }

    /**
     * Refreshes title, week-range label, and the row contents so
     * everything follows the active language.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("transactions.activity.title"));
        refresh();
    }

    /**
     * Rebuilds the value rows from the current filter selection and
     * updates the header's week-range label. The three rows (Kjøp,
     * Salg, Total) follow the same divider pattern {@code HoldingsCard}
     * uses, so this card reads as part of the same visual family as
     * the summary card next to it.
     */
    private void refresh() {
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();

        weekRangeLabel.setText(MessageFormat.format(
                LanguageManager.get("transactions.summary.weekRange"), fromWeek, toWeek));

        int purchaseCount = countTransactions(fromWeek, toWeek, true);
        int saleCount = countTransactions(fromWeek, toWeek, false);
        int total = purchaseCount + saleCount;

        grid.getChildren().clear();

        grid.add(rowLabel(LanguageManager.get("transactions.summary.purchases"), false), 0, 0);
        grid.add(rowValue(purchaseCount, false), 1, 0);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.sales"), false), 0, 1);
        grid.add(rowValue(saleCount, false), 1, 1);

        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, 2);
        grid.add(divider, 0, 2);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.total"), true), 0, 3);
        grid.add(rowValue(total, true), 1, 3);
    }

    /**
     * Counts the purchases or sales in the archive within the given
     * week range (inclusive on both ends).
     *
     * @param fromWeek    the first week to include (inclusive)
     * @param toWeek      the last week to include (inclusive)
     * @param countBuys   {@code true} to count purchases, {@code false}
     *                    to count sales
     * @return the total number of matching transactions in the range
     */
    private int countTransactions(int fromWeek, int toWeek, boolean countBuys) {
        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int total = 0;
        for (int week = fromWeek; week <= toWeek; week++) {
            total += countBuys
                    ? archive.getPurchases(week).size()
                    : archive.getSales(week).size();
        }
        return total;
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
     * Creates a plain integer count label for the rightmost column.
     * No sign or unit — these are activity counts, not signed amounts,
     * so they read cleanly as "5", "3", "8" rather than "+5", "+3", "+8".
     *
     * @param count the value to render
     * @param bold  whether to apply the bold modifier
     * @return a styled label
     */
    private Label rowValue(int count, boolean bold) {
        Label label = TableCells.data(Integer.toString(count));
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }
}