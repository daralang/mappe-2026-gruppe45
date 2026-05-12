package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort;
import javafx.geometry.HPos;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Sortable, searchable and paginated table of all stocks listed on the exchange.
 *
 * <p>Displays ticker (with owner badge), company name, current price, weekly
 * change in NOK and percent, a sparkline trend and buy/sell action buttons.
 * Row rendering is delegated to {@link StocksRowRenderer}.
 */
public class StocksListCard extends Card {

    public static final int PAGE_SIZE = 20;

    private final GameService gameService;
    private final StocksSort sort = new StocksSort();
    private final StocksRowRenderer rowRenderer;

    private List<Stock> allStocks = new ArrayList<>();
    private List<Stock> filteredStocks = new ArrayList<>();

    private int currentPage = 0;
    private String currentFilterTerm = "";
    private Runnable onRefreshed = null;

    private final GridPane grid = new GridPane();

    /**
     * Constructs a new StocksListCard.
     * Row rendering is delegated to {@link StocksRowRenderer}.
     *
     * @param gameService the game service containing exchange and player state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksListCard(GameService gameService, PortfolioController controller) {
        super(gameService);
        this.gameService = gameService;
        this.rowRenderer = new StocksRowRenderer(gameService, controller);

        setSpacing(12);
        grid.setHgap(16);
        configureColumns();

        getChildren().add(grid);
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
     * If a sort is active, sorts via {@link StocksSort}.
     * If no sort is active, restores the original exchange order via {@link #restoreOrder()}.
     * Clears the grid, renders the header and the current page of rows, then fires
     * the {@link #onRefreshed} callback so the parent view can update pagination and status.
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
            rowRenderer.buildRow(page.get(i), i + 1, grid);
        }

        if (onRefreshed != null) {
            onRefreshed.run();
        }
    }

    /**
     * Filters all stocks by the given term and resets {@link #currentPage} to 0.
     * Delegates to the exchange's search when a term is present; uses the full list otherwise.
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
     * Sets the current page and refreshes the table to show the corresponding rows.
     * Called by the parent view when the user navigates.
     *
     * @param page the zero-based page index to navigate to
     */
    public void setPage(int page) {
        currentPage = page;
        refresh();
    }

    /**
     * Returns the current zero-based page index.
     *
     * @return current page index
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Registers a callback that is invoked at the end of every {@link #refresh()}.
     * Use this to update pagination, status labels, or sort controls in the parent view.
     *
     * @param onRefreshed the callback to run after each refresh
     */
    public void setOnRefreshed(Runnable onRefreshed) {
        this.onRefreshed = onRefreshed;
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
