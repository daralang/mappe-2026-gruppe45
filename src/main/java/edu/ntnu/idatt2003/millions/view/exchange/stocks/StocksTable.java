package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SparklineChart;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Sortable, searchable and paginated table of all stocks listed on the exchange.
 *
 * <p>Displays ticker (with owner badge), company name, current price, weekly
 * change in NOK and percent, a {@link SparklineChart} trend and buy/sell action buttons.
 *
 * <p>Sort state is fully managed by {@link StocksSort}, which also builds the
 * header row. Pagination is delegated to {@link Pagination}. This class handles
 * data loading, filtering and row rendering.
 */
public class StocksTable extends Card {

    private static final int PAGE_SIZE = 20;
    private static final int MAX_SPARKLINE_WEEKS = 8;

    private final GameService gameService;
    private final PortfolioController controller;
    private final StocksSort sort = new StocksSort();

    private List<Stock> allStocks = new ArrayList<>();
    private List<Stock> filteredStocks = new ArrayList<>();

    private int currentPage = 0;
    private String currentFilterTerm = "";
    private Runnable onSortChanged = null;

    private final GridPane grid = new GridPane();
    private final Pagination pagination = new Pagination(PAGE_SIZE, page -> {
        currentPage = page;
        refresh();
    });

    /**
     * Constructs a new StocksTable.
     *
     * @param gameService the game service containing exchange and player state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksTable(GameService gameService, PortfolioController controller) {
        super(gameService);
        this.gameService = gameService;
        this.controller = controller;

        setSpacing(12);
        grid.setHgap(16);
        configureColumns();

        getChildren().addAll(grid, pagination);
        onGameUpdated();
    }

    /**
     * Configures the percentage widths and horizontal alignments of the table columns.
     * Column order: ticker, company, price, change NOK, change %, trend, trade.
     */
    private void configureColumns() {
        TableCells.configureColumns(grid,
                new double[]{12, 25, 10, 10, 10, 12, 21},
                new HPos[]{HPos.LEFT, HPos.LEFT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.CENTER, HPos.LEFT});
    }

    /**
     * Filters the table to stocks matching the given term and refreshes the display.
     * Resets to the first page. An empty or blank term clears the filter.
     *
     * @param term the search term to filter by
     */
    public void filter(String term) {
        applyFilter(term);
        refresh();
    }

    /**
     * Returns the number of stocks currently shown after filtering.
     *
     * @return filtered stock count
     */
    public int getFilteredCount() {
        return filteredStocks.size();
    }

    /**
     * Returns the total number of stocks on the exchange.
     *
     * @return total stock count
     */
    public int getTotalCount() {
        return allStocks.size();
    }

    /**
     * Rebuilds the table from the current filtered and sorted stock list.
     * If a sort is active, sorts via {@link StocksSort#applySort(List)}.
     * If no sort is active, restores the original exchange order via {@link #restoreOrder()}.
     * Clears the grid, renders the header and the current page of rows, then updates pagination.
     */
    private void refresh() {
        if (sort.isActive()) {
            sort.applySort(filteredStocks);
        } else {
            restoreOrder();
        }

        grid.getChildren().clear();
        sort.buildHeader(grid, this::refresh);

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredStocks.size());
        List<Stock> page = filteredStocks.subList(fromIndex, toIndex);

        for (int i = 0; i < page.size(); i++) {
            buildDataRow(page.get(i), i + 1);
        }

        pagination.update(currentPage, filteredStocks.size());
        if (onSortChanged != null) {
            onSortChanged.run();
        }
    }

    /**
     * Filters all stocks by the given term and resets {@link #currentPage} to 0.
     * Delegates to {@link edu.ntnu.idatt2003.millions.model.exchange.Exchange#findStocks}
     * when a term is present; uses the full list otherwise.
     *
     * @param term the search term, or blank to show all stocks
     */
    private void applyFilter(String term) {
        currentFilterTerm = term == null ? "" : term;
        filteredStocks = currentFilterTerm.isBlank()
                ? new ArrayList<>(allStocks)
                : new ArrayList<>(gameService.getExchange().findStocks(currentFilterTerm));
        currentPage = 0;
    }

    /**
     * Restores {@link #filteredStocks} to the original exchange order without
     * resetting {@link #currentPage}. Used when sort is cleared so the list
     * returns to its pre-sort state while keeping the user on the current page.
     */
    private void restoreOrder() {
        filteredStocks = currentFilterTerm.isBlank()
                ? new ArrayList<>(allStocks)
                : new ArrayList<>(gameService.getExchange().findStocks(currentFilterTerm));
    }

    /**
     * Builds a single data row for the given stock at the specified grid row index.
     * Renders the ticker label with an owner badge, company name, current price,
     * weekly change in NOK and percent, a {@link SparklineChart} with the last
     * {@link #MAX_SPARKLINE_WEEKS} prices, and buy/sell action buttons.
     *
     * @param stock    the stock to display
     * @param rowIndex the grid row index (0 = header)
     */
    private void buildDataRow(Stock stock, int rowIndex) {
        Label tickerLabel = new Label(stock.getSymbol());
        tickerLabel.getStyleClass().add("holdings-cell");

        List<Share> ownedShares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        HBox tickerCell = new HBox(4, tickerLabel);
        tickerCell.setAlignment(Pos.CENTER_LEFT);
        if (!ownedShares.isEmpty()) {
            Label ownerBadge = new Label(LanguageManager.get("exchange.stocks.badge.owner"));
            ownerBadge.getStyleClass().add("owner-cell");
            tickerCell.getChildren().add(ownerBadge);
        }

        Label companyLabel = TableCells.data(stock.getCompany());
        Label priceLabel = TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice()));
        Label changeKrLabel = ChangeFormatter.styledAmount(stock.getLatestPriceChange(),"holdings-cell");
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(),"holdings-cell");

        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - MAX_SPARKLINE_WEEKS), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);

        HBox tradeButtons = buildTradeButtons(stock);

        grid.add(tickerCell, 0, rowIndex);
        grid.add(companyLabel, 1, rowIndex);
        grid.add(priceLabel, 2, rowIndex);
        grid.add(changeKrLabel, 3, rowIndex);
        grid.add(changePctLabel, 4, rowIndex);
        grid.add(sparkline, 5, rowIndex);
        grid.add(tradeButtons, 6, rowIndex);

        GridPane.setValignment(tickerCell, VPos.TOP);
        GridPane.setValignment(companyLabel, VPos.TOP);
        GridPane.setValignment(priceLabel, VPos.TOP);
        GridPane.setValignment(changeKrLabel, VPos.TOP);
        GridPane.setValignment(changePctLabel, VPos.TOP);
        GridPane.setValignment(sparkline, VPos.TOP);
        GridPane.setValignment(tradeButtons, VPos.TOP);
    }

    /**
     * Builds the buy and optional sell buttons for the trade column.
     *
     * <p>Always shows a buy button that delegates to {@link PortfolioController}.
     * Shows a sell button when the player holds at least one {@link Share} of the stock,
     * delegating to {@link PortfolioController} with the first position.
     *
     * @param stock the stock the buttons act on
     * @return an HBox containing the action buttons
     */
    private HBox buildTradeButtons(Stock stock) {
        Button buyButton = new Button(LanguageManager.get("exchange.stocks.buy"));
        buyButton.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        buyButton.setOnAction(e -> controller.openBuyDialog(stock));

        HBox buttons = new HBox(4, buyButton);

        List<Share> shares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        if (!shares.isEmpty()) {
            Share share = shares.get(0);
            Button sellButton = new Button(LanguageManager.get("exchange.stocks.sell"));
            sellButton.getStyleClass().addAll("holdings-action-link", "holdings-action-sell");
            sellButton.setOnAction(e -> controller.openSellDialog(share));
            buttons.getChildren().add(sellButton);
        }

        return buttons;
    }

    /**
     * Returns whether a sort column is currently active in {@link StocksSort}.
     *
     * @return {@code true} if a sort is active, {@code false} otherwise
     */
    public boolean isSortActive() {
        return sort.isActive();
    }

    /**
     * Clears the active sort and refreshes the table, restoring the original
     * exchange order. Delegates to {@link StocksSort#clearSort()}.
     */
    public void clearSort() {
        sort.clearSort();
        refresh();
    }

    /**
     * Registers a callback that is invoked at the end of every {@link #refresh()}.
     * Use this to react to sort state changes, e.g. to show or hide a clear sort button.
     *
     * @param onSortChanged the callback to run after each refresh
     */
    public void setOnSortChanged(Runnable onSortChanged) {
        this.onSortChanged = onSortChanged;
    }

    /**
     * Called when the game state changes (week advanced, buy or sell).
     * Reloads the stock list from the exchange and re-applies the current filter term.
     */
    @Override
    public void onGameUpdated() {
        allStocks = new ArrayList<>(gameService.getExchange().getStocks());
        applyFilter(currentFilterTerm);
        refresh();
    }

    /**
     * Called when the application language changes.
     * Re-renders headers and pagination with updated labels.
     * Logs any unexpected exception to prevent leaving the grid in an empty state.
     */
    @Override
    protected void onLanguageChanged() {
        refresh();
    }
}
