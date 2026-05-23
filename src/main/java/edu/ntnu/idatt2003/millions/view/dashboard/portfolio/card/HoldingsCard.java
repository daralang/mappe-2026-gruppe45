package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.MoneyFormatter;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.HoldingsSort;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Card displaying the player's holdings (shares owned), with sortable column
 * headers, search, pagination, action buttons for buy / sell / sell all / details,
 * and a persistent total row below the pagination.
 *
 * <p>Extends {@link SortableTableCard} for shared pagination, search, sort state,
 * and the common refresh Template Method. The total row lives in a separate
 * {@link GridPane} with the same column constraints as the table, so values align
 * regardless of which page is active. The {@link #afterFilter} hook is overridden
 * to update the total row's visibility and values after each filter pass.</p>
 */
public class HoldingsCard extends SortableTableCard<Share, HoldingsSort.SortColumn> {

    private static final int PAGE_SIZE = 9;

    /** Grid column index for the company name cell in the total row. */
    private static final int TOTAL_COL_COMPANY = 1;

    /** Grid column index for the value (NOK) cell in the total row. */
    private static final int TOTAL_COL_VALUE_NOK = 4;

    /** Grid column index for the return-percent cell in the total row. */
    private static final int TOTAL_COL_RETURN_PCT = 5;

    /** Grid column index for the return (NOK) cell in the total row. */
    private static final int TOTAL_COL_RETURN_NOK = 6;

    private final GameService gameService;
    private final PortfolioService portfolioService;
    private final TradeController controller;
    private final HoldingsSort sort;
    private final GridPane totalGrid = new GridPane();

    /**
     * Constructs a new HoldingsCard.
     *
     * @param gameService      the game manager containing player and exchange
     * @param controller       the controller handling portfolio actions
     * @param portfolioService the service used to compute share values and returns
     */
    public HoldingsCard(GameService gameService, TradeController controller,
                        PortfolioService portfolioService) {
        super(gameService, PAGE_SIZE,
                "dashboard.portfolio.holdings.status", "dashboard.portfolio.empty");
        this.gameService = gameService;
        this.controller = controller;
        this.portfolioService = portfolioService;
        this.sort = new HoldingsSort(portfolioService, gameService.getCurrencyConverter());
        this.sortProvider = sort;
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);

        totalGrid.setHgap(20);
        initTotalGridColumns();
        setTotalVisible(false);

        VBox.setMargin(pagination, new Insets(-16, 0, 0, 0));
        VBox.setMargin(totalGrid, new Insets(-16, 0, 0, 0));

        StyledText title = StyledText.sectionTitle(LanguageManager.get("dashboard.portfolio.title"));
        setSpacing(16);

        getChildren().addAll(title, buildSearchRow(clearSortButton), table.asNode(), pagination, totalGrid);
        refresh();
    }

    /**
     * Returns all shares in the player's portfolio, or an empty list if no player is active.
     *
     * @return mutable list of all {@link Share} objects owned by the player
     */
    @Override
    protected List<Share> fetchAll() {
        if (gameService.getPlayer() == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(gameService.getPlayer().getPortfolio().getShares());
    }

    @Override
    protected List<Share> applySearch(List<Share> all, String term) {
        if (term.isBlank()) {
            return new ArrayList<>(all);
        }
        return new ArrayList<>(all.stream()
                .filter(share -> share.getStock().matches(term))
                .toList());
    }

    @Override
    protected void renderPage(List<Share> page) {
        int row = 1;
        for (Share share : page) {
            addDataRow(row++, share);
        }
    }

    /**
     * Returns a localised empty-state message that includes the search term when active.
     *
     * @param term the active search term; may be blank
     * @return the localised empty-state message
     */
    @Override
    protected String emptyStateMessage(String term) {
        if (!term.isBlank()) {
            return MessageFormat.format(
                    LanguageManager.get("dashboard.portfolio.empty.search"), term);
        }
        return LanguageManager.get("dashboard.portfolio.empty");
    }

    /**
     * Updates the total row visibility and values after the filter pass.
     * The total row is shown whenever the unfiltered portfolio has shares,
     * regardless of the active search term.
     *
     * @param all      unfiltered share list
     * @param filtered text-filtered share list
     */
    @Override
    protected void afterFilter(List<Share> all, List<Share> filtered) {
        boolean hasPortfolioShares = !all.isEmpty();
        setTotalVisible(hasPortfolioShares);
        if (hasPortfolioShares) {
            refreshTotal(gameService.getPlayer().getPortfolio());
        }
    }

    /**
     * Configures {@link #totalGrid} with the same percentage column constraints
     * as the holdings table so total values align with their respective columns.
     */
    private void initTotalGridColumns() {
        for (TableColumnDef<HoldingsSort.SortColumn> col : sort.getColumnDefs()) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(col.percentWidth());
            cc.setHalignment(col.alignment());
            totalGrid.getColumnConstraints().add(cc);
        }
    }

    /**
     * Rebuilds the total row in {@link #totalGrid} with the latest portfolio values.
     *
     * @param portfolio the portfolio supplying the total values
     */
    private void refreshTotal(Portfolio portfolio) {
        totalGrid.getChildren().clear();

        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, sort.getColumnDefs().size());
        totalGrid.add(divider, 0, 0);

        Label totalLabel = TableCells.boldData(LanguageManager.get("dashboard.portfolio.total"));
        totalGrid.add(totalLabel, TOTAL_COL_COMPANY, 1);

        Label valueNok = TableCells.boldData(MoneyFormatter.format(
                portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
        totalGrid.add(valueNok, TOTAL_COL_VALUE_NOK, 1);

        totalGrid.add(coloredPercentCell(portfolioService.getTotalReturnPercent(
                gameService.getPlayer(), gameService.getCurrencyConverter())), TOTAL_COL_RETURN_PCT, 1);
        totalGrid.add(coloredAmountCell(portfolioService.getTotalReturnInNok(
                gameService.getPlayer(), gameService.getCurrencyConverter())), TOTAL_COL_RETURN_NOK, 1);
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
     * Renders one share as a data row in the table.
     *
     * @param row   the grid row index to write to
     * @param share the share to render
     */
    private void addDataRow(int row, Share share) {
        Stock stock = share.getStock();
        table.addRow(row,
                buildActionButtons(share),
                TableCells.data(stock.getSymbol() + ", " + stock.getCompany()),
                TableCells.data(MoneyFormatter.format(share.getQuantity())),
                coloredPercentCell(stock.getWeeklyChangePercent()),
                TableCells.data(MoneyFormatter.format(
                        portfolioService.getShareValueInNok(share, gameService.getCurrencyConverter()))),
                coloredPercentCell(share.getReturnPercent()),
                coloredAmountCell(
                        portfolioService.getShareReturnInNok(share, gameService.getCurrencyConverter())),
                buildDetailsButton(share)
        );
    }

    /**
     * Builds the buy, sell and sell-all action buttons for a share row.
     * All buttons are disabled when the game is over.
     *
     * @param share the share the buttons act on
     * @return an {@link HBox} containing the action buttons
     */
    private HBox buildActionButtons(Share share) {
        boolean gameOver = gameService.isGameOver();

        Button buy = actionButton(LanguageManager.get("dashboard.portfolio.buy"), "holdings-action-buy");
        Button sell = actionButton(LanguageManager.get("dashboard.portfolio.sell"), "holdings-action-sell");
        Button sellAll = actionButton(LanguageManager.get("dashboard.portfolio.sellAll"), "holdings-action-sell");

        buy.setDisable(gameOver);
        sell.setDisable(gameOver);
        sellAll.setDisable(gameOver);

        buy.setOnAction(e -> controller.openBuyDialog(share.getStock()));
        sell.setOnAction(e -> controller.openSellDialog(share));
        sellAll.setOnAction(e -> controller.openSellAllDialog(share));

        HBox primaryActions = new HBox(8, buy, sell);
        primaryActions.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox(16, primaryActions, sellAll);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    /**
     * Builds the details navigation button for a share row.
     *
     * @param share the share to open details for
     * @return a styled chevron button
     */
    private Button buildDetailsButton(Share share) {
        Button details = new Button("❯");
        details.getStyleClass().add("holdings-details-chevron");
        details.setOnAction(e -> controller.openDetailsModal(share));
        return details;
    }

    /**
     * Creates a styled action button with the given label and colour class.
     *
     * @param text       the button label
     * @param colorClass the CSS class controlling the button colour
     * @return a styled action button
     */
    private Button actionButton(String text, String colorClass) {
        Button b = new Button(text);
        b.getStyleClass().addAll("holdings-action-link", colorClass);
        return b;
    }

    /**
     * Creates a coloured percentage label using {@link ChangeFormatter}.
     *
     * @param value the percentage value to format
     * @return a styled label
     */
    private Label coloredPercentCell(BigDecimal value) {
        return ChangeFormatter.styledPercent(value, "holdings-cell");
    }

    /**
     * Creates a coloured signed amount label using {@link ChangeFormatter},
     * with the NOK currency symbol appended.
     *
     * @param value the amount value to format
     * @return a styled label
     */
    private Label coloredAmountCell(BigDecimal value) {
        return ChangeFormatter.styledAmount(value, "holdings-cell");
    }
}
