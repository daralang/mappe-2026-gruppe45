package edu.ntnu.idatt2003.millions.view.dashboard.transactions.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.model.transaction.TransactionArchive;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.WeekRangeFilter;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.MessageFormat;

/**
 * Dashboard card for the transactions tab that counts trading activity over the selected
 * week range: number of purchases, number of sales, and their sum.
 *
 * <p>Sits beside {@code TransactionsSummaryCard} on the transactions
 * tab. The summary card shows <em>how much money moved</em>; this
 * card shows <em>how often the player traded</em>.</p>
 *
 * <p>Listens to the shared {@link WeekRangeFilter} owned by
 * {@code TransactionsCard} so all three components on the tab show
 * data for the same period.</p>
 */
public class TransactionsActivityCard extends Card {

    /** Spacing between the header row and the value grid. */
    private static final int CARD_SPACING = 16;

    /** Horizontal gap between the label and value columns. */
    private static final int VALUE_HGAP = 20;

    /** Equal-width two-column layout: label left, count right. */
    private static final double[] COLUMN_WIDTHS = {50, 50};
    private static final HPos[] COLUMN_ALIGNMENTS = {HPos.LEFT, HPos.RIGHT};

    private final GameService gameService;
    private final WeekRangeFilter weekRangeFilter;

    private final StyledText title;
    private final StyledText weekRangeLabel;
    private final GridPane grid = new GridPane();

    /**
     * @param gameService     game manager containing player and exchange
     * @param weekRangeFilter shared filter that scopes the table this card counts against
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
        title.setText(LanguageManager.get("transactions.activity.title"));
        refresh();
    }

    /**
     * Rebuilds the value rows from the current filter selection and
     * updates the week-range label. The Total row uses the same
     * divider as {@code HoldingsCard} so the two cards read as a
     * visual family.
     */
    private void refresh() {
        int fromWeek = weekRangeFilter.getFromWeek();
        int toWeek = weekRangeFilter.getToWeek();

        weekRangeLabel.setText(MessageFormat.format(
                LanguageManager.get("transactions.summary.weekRange"), fromWeek, toWeek));

        TransactionArchive archive = gameService.getPlayer().getTransactionArchive();
        int purchaseCount = archive.countPurchasesInRange(fromWeek, toWeek);
        int saleCount = archive.countSalesInRange(fromWeek, toWeek);
        int total = purchaseCount + saleCount;

        grid.getChildren().clear();

        grid.add(rowLabel(LanguageManager.get("transactions.summary.purchases"), false), 0, 0);
        grid.add(rowValue(purchaseCount, false), 1, 0);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.sales"), false), 0, 1);
        grid.add(rowValue(saleCount, false), 1, 1);

        Region divider = new Region();
        divider.getStyleClass().add("table-total-divider");
        GridPane.setColumnSpan(divider, 2);
        grid.add(divider, 0, 2);

        grid.add(rowLabel(LanguageManager.get("transactions.summary.total"), true), 0, 3);
        grid.add(rowValue(total, true), 1, 3);
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
     * Right-column count. No sign or unit - these are counts, not
     * signed amounts, so "5" reads cleaner than "+5".
     */
    private Label rowValue(int count, boolean bold) {
        Label label = TableCells.data(Integer.toString(count));
        if (bold) {
            label.getStyleClass().add("bold");
        }
        return label;
    }
}