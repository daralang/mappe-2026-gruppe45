package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.ChevronButton;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import edu.ntnu.idatt2003.millions.view.dashboard.loans.LoansSort;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Card showing the player's outstanding loans in a sortable table.
 * Uses {@link SortColumnTable} with {@link LoansSort} for consistent headers,
 * sort arrows and tooltips, the same infrastructure as HoldingsCard and TransactionsCard.
 */
public class ActiveLoansCard extends Card {

    private final GameService gameService;
    private final LoanController controller;
    private final LoansSort loansSort;
    private final SortColumnTable<LoansSort.SortColumn> table;
    private final StyledText title = StyledText.sectionTitle();
    private final GridPane totalGrid = new GridPane();

    /**
     * Constructs a new ActiveLoansCard.
     *
     * @param gameService the game service used to read active loans and exchange state
     * @param controller  the controller handling repay and details actions
     */
    public ActiveLoansCard(GameService gameService, LoanController controller) {
        super(gameService);
        this.gameService = gameService;
        this.controller = controller;
        this.loansSort = new LoansSort(gameService);
        this.table = new SortColumnTable<>(loansSort::getColumnDefs);

        totalGrid.setHgap(20);
        initTotalGridColumns();
        setTotalVisible(false);

        VBox.setMargin(totalGrid, new Insets(-16, 0, 0, 0));

        setSpacing(16);
        getChildren().addAll(title, table.asNode(), totalGrid);
        refresh();
    }

    @Override
    public void onGameUpdated() {
        refresh();
    }

    @Override
    protected void onLanguageChanged() {
        refresh();
    }

    /**
     * Rebuilds the table from the current loan list.
     * Clears existing rows, refreshes the header, applies any active sort,
     * then renders one data row per loan followed by a total row.
     */
    private void refresh() {
        title.setText(LanguageManager.get("loans.active.title"));
        table.clearRows();

        List<Loan> loans = gameService.getPlayer().getActiveLoans();

        table.refreshHeader(this::refresh);

        if (loans.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("loans.active.empty"));
            setTotalVisible(false);
            return;
        }

        if (table.isSortActive()) {
            loansSort.applySort(loans, table.getSortState());
        }

        Map<String, Integer> typeCount = new HashMap<>();
        int row = 1;
        for (Loan loan : loans) {
            int count = typeCount.merge(loan.offer().id(), 1, Integer::sum);
            addDataRow(row++, loan, count);
        }

        setTotalVisible(true);
        refreshTotal(loans);
    }

    /**
     * Renders one loan as a data row in the table.
     *
     * @param row       the table row index to write to
     * @param loan      the loan to render
     * @param typeIndex the sequential index of this loan within its offer type,
     *                  used to build the display label and passed to the controller
     */
    private void addDataRow(int row, Loan loan, int typeIndex) {
        String offerName = LanguageManager.get("loans.offer." + loan.offer().id() + ".name");
        String loanLabel = offerName + " #" + typeIndex;

        BigDecimal weeklyRate = loan.offer().weeklyInterestRate()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        int currentWeek = gameService.getExchange().getWeek();
        int weeksLeft = loan.weeksRemaining(currentWeek);
        String weeksLeftText = MessageFormat.format(
                LanguageManager.get("loans.active.weeksLeft"), weeksLeft, loan.offer().termWeeks());

        table.addRow(row,
                TableCells.data(loanLabel),
                TableCells.data(MoneyFormatter.format(weeklyRate) + " %"),
                TableCells.data(weeksLeftText),
                TableCells.data(MoneyFormatter.format(loan.weeklyInterest())),
                TableCells.data(MoneyFormatter.format(loan.principal())),
                buildActionCell(loan, typeIndex)
        );
    }

    /**
     * Rebuilds the total row in {@link #totalGrid} with aggregated values
     * from the full loans list.
     *
     * @param loans the active loan list used to compute totals
     */
    private void refreshTotal(List<Loan> loans) {
        totalGrid.getChildren().clear();

        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, loansSort.getColumnDefs().size());
        totalGrid.add(divider, 0, 0);

        totalGrid.add(TableCells.boldData(LanguageManager.get("loans.active.total")),
                table.columnIndex(LoansSort.SortColumn.LOAN), 1);

        BigDecimal totalWeeklyCost = loans.stream()
                .map(Loan::weeklyInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalGrid.add(TableCells.boldData(
                MoneyFormatter.format(totalWeeklyCost)),
                table.columnIndex(LoansSort.SortColumn.WEEKLY_COST), 1);

        totalGrid.add(TableCells.boldData(
                MoneyFormatter.format(gameService.getPlayer().getTotalDebt())),
                table.columnIndex(LoansSort.SortColumn.REMAINING), 1);
    }

    /**
     * Configures {@link #totalGrid} with percentage column constraints mirroring
     * {@link LoansSort#getColumnDefs()} so total values align with table columns.
     */
    private void initTotalGridColumns() {
        for (TableColumnDef<LoansSort.SortColumn> col : loansSort.getColumnDefs()) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            totalGrid.getColumnConstraints().add(cc);
        }
    }

    /**
     * Shows or hides the total divider and grid.
     *
     * @param visible {@code true} to show, {@code false} to hide and unmanage
     */
    private void setTotalVisible(boolean visible) {
        totalGrid.setVisible(visible);
        totalGrid.setManaged(visible);
    }

    /**
     * Builds the repay and details action buttons for a loan row.
     * Both buttons are disabled when the game is over.
     *
     * @param loan      the loan the buttons act on
     * @param typeIndex the sequential index within its offer type, passed to the controller
     * @return an {@link HBox} containing the action buttons
     */
    private HBox buildActionCell(Loan loan, int typeIndex) {
        Button repay = new Button(LanguageManager.get("loans.active.button.repay"));
        repay.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        repay.setDisable(gameService.isGameOver());
        repay.setOnAction(e -> controller.openRepayDialog(loan, typeIndex));

        ChevronButton details = new ChevronButton(
                () -> controller.openLoanDetailsModal(loan, typeIndex),
                "tooltip.loans.chevron");

        HBox box = new HBox(24, repay, details);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }
}
