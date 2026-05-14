package edu.ntnu.idatt2003.millions.view.dashboard.loans.card;

import edu.ntnu.idatt2003.millions.controller.LoanController;
import edu.ntnu.idatt2003.millions.model.loan.Loan;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Card showing the player's outstanding loans in a table.
 *
 * <p>Follows the same structure as {@code HoldingsCard} and
 * {@code TransactionsCard}: a section title above a {@link GridPane}
 * built via {@link TableCells}, rebuilt on every game or language change.</p>
 */
public class ActiveLoansCard extends Card {

    private static final int COLUMN_COUNT = 6;
    private static final double[] COLUMN_WIDTHS = {30, 12, 14, 18, 18, 8};
    private static final HPos[] COLUMN_ALIGNMENTS = {
            HPos.LEFT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT
    };

    private final GameService gameService;
    private final LoanController controller;
    private final GridPane grid = new GridPane();
    private final StyledText title = StyledText.sectionTitle();

    public ActiveLoansCard(GameService gameService, LoanController controller) {
        super(gameService);
        this.gameService = gameService;
        this.controller = controller;

        setSpacing(16);

        grid.setHgap(20);
        TableCells.configureColumns(grid, COLUMN_WIDTHS, COLUMN_ALIGNMENTS);

        getChildren().addAll(title, grid);
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

    private void refresh() {
        title.setText(LanguageManager.get("loans.active.title"));
        grid.getChildren().clear();

        List<Loan> loans = gameService.getPlayer().getActiveLoans();

        addHeaderRow();

        if (loans.isEmpty()) {
            TableCells.renderEmptyState(grid, LanguageManager.get("loans.active.empty"), COLUMN_COUNT);
            return;
        }

        int row = 1;
        Map<String, Integer> typeCount = new HashMap<>();
        for (Loan loan : loans) {
            int count = typeCount.merge(loan.offer().id(), 1, Integer::sum);
            addDataRow(row++, loan, count, count);
        }

        addTotalRow(row, loans);
    }

    private void addHeaderRow() {
        TableCells.addHeaderRow(grid, new String[]{
                LanguageManager.get("loans.active.col.loan"),
                LanguageManager.get("loans.active.col.rate"),
                LanguageManager.get("loans.active.col.weeksLeft"),
                LanguageManager.get("loans.active.col.weeklyCost"),
                LanguageManager.get("loans.active.col.remaining"),
                ""
        });
    }

    private void addDataRow(int row, Loan loan, int typeCount, int loanIndex) {
        String offerName = LanguageManager.get("loans.offer." + loan.offer().id() + ".name");

        BigDecimal weeklyRate = loan.offer().weeklyInterestRate()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        int currentWeek = gameService.getExchange().getWeek();
        int weeksLeft = loan.weeksRemaining(currentWeek);
        String weeksLeftText = MessageFormat.format(
                LanguageManager.get("loans.active.weeksLeft"), weeksLeft, loan.offer().termWeeks());

        BigDecimal weeklyCost = loan.weeklyInterest();

        grid.add(TableCells.data(offerName + " #" + typeCount), 0, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(weeklyRate) + " %"), 1, row);
        grid.add(TableCells.data(weeksLeftText), 2, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(weeklyCost) + " NOK"), 3, row);
        grid.add(TableCells.data(TableCells.NUMBER_FORMAT.format(loan.principal()) + " NOK"), 4, row);
        grid.add(buildActionCell(loan, loanIndex), 5, row);
    }

    private void addTotalRow(int row, List<Loan> loans) {
        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, COLUMN_COUNT);
        grid.add(divider, 0, row);

        int dataRow = row + 1;

        BigDecimal totalWeeklyCost = loans.stream()
                .map(Loan::weeklyInterest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        grid.add(TableCells.boldData(LanguageManager.get("loans.active.total")), 0, dataRow);
        grid.add(TableCells.boldData(TableCells.NUMBER_FORMAT.format(totalWeeklyCost) + " NOK"), 3, dataRow);
        grid.add(TableCells.boldData(
                TableCells.NUMBER_FORMAT.format(gameService.getPlayer().getTotalDebt()) + " NOK"), 4, dataRow);
    }

    private HBox buildActionCell(Loan loan, int loanIndex) {
        Button repay = new Button(LanguageManager.get("loans.active.button.repay"));
        repay.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        repay.setDisable(gameService.isGameOver());
        repay.setOnAction(e -> controller.openRepayDialog(loan, loanIndex));

        Button details = new Button("❯");
        details.getStyleClass().add("holdings-details-chevron");
        details.setOnAction(e -> controller.openLoanDetailsModal(loan, loanIndex));

        HBox box = new HBox(24, repay, details);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }
}
