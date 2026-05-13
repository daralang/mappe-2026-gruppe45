package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.PaginatedCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.component.table.TableColumnDef;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.HoldingsSort;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Card displaying the player's holdings (shares owned), with sortable column
 * headers, search, pagination, action buttons for buy / sell / sell all / details,
 * and a persistent total row below the pagination.
 *
 * <p>Extends {@link edu.ntnu.idatt2003.millions.view.component.card.PaginatedCard} for
 * shared pagination state and behaviour. Column structure, sort state and header
 * rendering are owned by {@link SortColumnTable}. Domain-specific sort logic is
 * delegated to {@link HoldingsSort}. The total row lives in a separate {@link GridPane}
 * with the same column constraints as the table, so values align regardless
 * of which page is active.</p>
 */
public class HoldingsCard extends PaginatedCard {

    private static final int PAGE_SIZE = 9;

    private final GameService gameService;
    private final PortfolioService portfolioService = new PortfolioService();
    private final PortfolioController controller;
    private final HoldingsSort sort;
    private final SortColumnTable<HoldingsSort.SortColumn> table;
    private final Pagination pagination;
    private final Region totalDivider = new Region();
    private final GridPane totalGrid = new GridPane();

    private String currentSearchTerm = "";

    /**
     * Constructs a new HoldingsCard.
     *
     * <p>Column definitions, widths, alignments and tooltip keys are read from
     * {@link HoldingsSort} via a method reference so that
     * {@link SortColumnTable} can resolve fresh i18n labels on every header refresh.
     * The total grid is initialised with the same column constraints so values
     * align with the table above it.</p>
     *
     * @param gameService the game manager containing player and exchange
     * @param controller  the controller handling portfolio actions
     */
    public HoldingsCard(GameService gameService, PortfolioController controller) {
        super(gameService, PAGE_SIZE);
        this.gameService = gameService;
        this.controller = controller;
        this.sort = new HoldingsSort(portfolioService, gameService.getCurrencyConverter());
        this.table = new SortColumnTable<>(sort::getColumnDefs);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);

        totalDivider.getStyleClass().add("holdings-total-divider");
        totalGrid.setHgap(20);
        initTotalGridColumns();
        setTotalVisible(false);

        StyledText title = StyledText.sectionTitle(LanguageManager.get("dashboard.portfolio.title"));
        setSpacing(16);

        getChildren().addAll(title, createSearchBar(clearSortButton), table.asNode(), pagination, totalDivider, totalGrid);
        refresh();
    }

    /**
     * Builds the search bar with a right-aligned clear-sort button in the metadata row.
     *
     * @param clearSortButton the button returned by {@link SortColumnTable#createClearSortButton}
     * @return the configured search bar
     */
    private SearchBar createSearchBar(Button clearSortButton) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox metadataRow = new HBox(spacer, clearSortButton);
        metadataRow.setAlignment(Pos.CENTER_RIGHT);

        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                term -> {
                    currentSearchTerm = term == null ? "" : term;
                    resetPageAndRefresh();
                },
                metadataRow);
        searchBar.setMaxWidth(Double.MAX_VALUE);
        return searchBar;
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
     * Called when the game state changes (week advanced, buy or sell).
     * Rebuilds the holdings table to reflect the current portfolio.
     */
    @Override
    public void onGameUpdated() {
        refresh();
    }

    /**
     * Called when the application language changes.
     * Rebuilds the table so column headers and labels are re-resolved.
     */
    @Override
    protected void onLanguageChanged() {
        refresh();
    }

    /**
     * Rebuilds the table contents based on the player's current portfolio.
     * Applies search filtering, optional sort, and renders the current page.
     * The total row is always shown below the pagination when the portfolio
     * has any shares.
     */
    @Override
    protected void refresh() {
        table.clearRows();
        table.refreshHeader(this::refresh);

        Portfolio portfolio = gameService.getPlayer().getPortfolio();
        List<Share> shares = new ArrayList<>(portfolio.getShares().stream()
                .filter(share -> currentSearchTerm.isBlank() || share.getStock().matches(currentSearchTerm))
                .toList());

        boolean hasPortfolioShares = !portfolio.getShares().isEmpty();
        setTotalVisible(hasPortfolioShares);
        if (hasPortfolioShares) {
            refreshTotal(portfolio);
        }

        if (shares.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("dashboard.portfolio.empty"));
            pagination.update(0, 0);
            return;
        }

        if (table.isSortActive()) {
            sort.applySort(shares, table.getSortState());
        }

        clampCurrentPage(shares.size());
        pagination.update(currentPage, shares.size());

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, shares.size());
        List<Share> page = shares.subList(fromIndex, toIndex);

        int row = 1;
        for (Share share : page) {
            addDataRow(row++, share);
        }
    }

    /**
     * Rebuilds the total row in {@link #totalGrid} with the latest portfolio values.
     *
     * @param portfolio the portfolio supplying the total values
     */
    private void refreshTotal(Portfolio portfolio) {
        totalGrid.getChildren().clear();

        Label totalLabel = new Label(LanguageManager.get("dashboard.portfolio.total"));
        totalLabel.getStyleClass().addAll("holdings-cell", "bold");
        totalGrid.add(totalLabel, 1, 0);

        Label valueNok = new Label(TableCells.NUMBER_FORMAT.format(
                portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
        valueNok.getStyleClass().addAll("holdings-cell", "bold");
        totalGrid.add(valueNok, 4, 0);

        totalGrid.add(coloredPercentCell(portfolioService.getTotalReturnPercent(
                gameService.getPlayer(), gameService.getCurrencyConverter())), 5, 0);
        totalGrid.add(coloredAmountCell(portfolioService.getTotalReturnInNok(
                gameService.getPlayer(), gameService.getCurrencyConverter())), 6, 0);
    }

    /**
     * Shows or hides the total divider and grid.
     *
     * @param visible {@code true} to show, {@code false} to hide and unmanage
     */
    private void setTotalVisible(boolean visible) {
        totalDivider.setVisible(visible);
        totalDivider.setManaged(visible);
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
                TableCells.data(TableCells.NUMBER_FORMAT.format(share.getQuantity())),
                coloredPercentCell(stock.getWeeklyChangePercent()),
                TableCells.data(TableCells.NUMBER_FORMAT.format(
                        portfolioService.getShareValueInNok(share, gameService.getCurrencyConverter()))),
                coloredPercentCell(share.getReturnPercent()),
                coloredAmountCell(
                        portfolioService.getShareReturnInNok(share, gameService.getCurrencyConverter())),
                buildDetailsButton(share)
        );
    }

    /**
     * Builds the buy, sell and sell-all action buttons for a share row.
     *
     * @param share the share the buttons act on
     * @return an {@link HBox} containing the action buttons
     */
    private HBox buildActionButtons(Share share) {
        Button buy = actionButton(LanguageManager.get("dashboard.portfolio.buy"), "holdings-action-buy");
        Button sell = actionButton(LanguageManager.get("dashboard.portfolio.sell"), "holdings-action-sell");
        Button sellAll = actionButton(LanguageManager.get("dashboard.portfolio.sellAll"), "holdings-action-sell");

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
     * Creates a coloured signed amount label using {@link ChangeFormatter}.
     *
     * @param value the amount value to format
     * @return a styled label
     */
    private Label coloredAmountCell(BigDecimal value) {
        return ChangeFormatter.styledAmount(value, "holdings-cell");
    }
}
