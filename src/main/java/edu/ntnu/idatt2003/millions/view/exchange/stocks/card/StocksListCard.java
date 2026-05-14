package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.SearchMetadataRow;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.PaginatedCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Market card with controls for searching, sorting, and paginating stocks listed on the exchange.
 * Extends {@link edu.ntnu.idatt2003.millions.view.component.card.PaginatedCard} for
 * shared pagination state and behaviour.
 *
 * <p>Displays a title, a {@link SearchBar} with result metadata, reset-sort action,
 * pagination, ticker, company, prices, weekly change, 4-week high/low, trend and
 * trade actions. Column structure and sort state are owned by {@link SortColumnTable};
 * domain-specific sort logic is delegated to {@link StocksSort}; row rendering is
 * delegated to {@link StocksRowRenderer}.</p>
 */
public class StocksListCard extends PaginatedCard {

    public static final int PAGE_SIZE = 20;
    private static final double ROW_HEIGHT = 34.0;

    private final GameService gameService;
    private final StocksSort sort;
    private final StocksRowRenderer rowRenderer;
    private final SortColumnTable<StocksSort.SortColumn> table;
    private final StyledText title;
    private final SearchMetadataRow metadataRow;

    private List<Stock> allStocks = new ArrayList<>();
    private List<Stock> filteredStocks = new ArrayList<>();

    private String currentFilterTerm = "";
    private Runnable onRefreshed = null;

    /**
     * Constructs a new StocksListCard.
     *
     * <p>Column definitions, widths and alignments are read from
     * {@link StocksSort#getColumnDefs()} via a method reference so that
     * {@link SortColumnTable} can resolve fresh i18n labels on every header refresh.
     * Row rendering is delegated to {@link StocksRowRenderer}.</p>
     *
     * @param gameService the game service containing exchange and player state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksListCard(GameService gameService, PortfolioController controller) {
        super(gameService, PAGE_SIZE);
        this.gameService = gameService;
        this.sort = new StocksSort(gameService.getCurrencyConverter());
        this.rowRenderer = new StocksRowRenderer(gameService, controller);
        this.table = new SortColumnTable<>(sort::getColumnDefs, 10);
        this.title = StyledText.sectionTitle(LanguageManager.get("exchange.stocks.market"));
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);
        this.metadataRow = new SearchMetadataRow();

        setSpacing(12);
        setMinWidth(0);
        table.setMinHeight(PAGE_SIZE * ROW_HEIGHT);

        getChildren().addAll(title, buildSearchRow(clearSortButton), table.asNode());
        onGameUpdated();
    }

    /**
     * Builds a row containing the search bar (expanding) and the clear-sort button
     * (right-aligned, visible only when a sort is active).
     *
     * @param clearSortButton the button returned by
     *                        {@link edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable#createClearSortButton}
     * @return the configured search row
     */
    private HBox buildSearchRow(Button clearSortButton) {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                this::filter,
                metadataRow);
        searchBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchBar, Priority.ALWAYS);

        HBox row = new HBox(8, searchBar, clearSortButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Filters the table to stocks matching the given term and refreshes the display.
     * Resets to the first page. An empty or blank term clears the filter.
     *
     * @param term the search term to filter by
     */
    public void filter(String term) {
        currentFilterTerm = term == null ? "" : term;
        syncFilteredList();
        currentPage = 0;
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
     * Rebuilds the table from the current filtered and sorted stock list.
     * If a sort is active, delegates sorting to {@link StocksSort#applySort}.
     * If no sort is active, syncs the filtered list to the original exchange order
     * via {@link #syncFilteredList()}.
     */
    @Override
    protected void refresh() {
        if (table.isSortActive()) {
            sort.applySort(filteredStocks, table.getSortState());
        } else {
            syncFilteredList();
        }

        table.clearRows();
        table.refreshHeader(this::refresh, !filteredStocks.isEmpty());
        metadataRow.update("exchange.stocks.status", filteredStocks.size(), allStocks.size());

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredStocks.size());
        List<Stock> page = filteredStocks.subList(fromIndex, toIndex);

        for (int i = 0; i < page.size(); i++) {
            rowRenderer.buildRow(page.get(i), i + 1, table);
        }

        if (filteredStocks.isEmpty()) {
            String msg = currentFilterTerm.isBlank()
                    ? LanguageManager.get("exchange.stocks.empty")
                    : MessageFormat.format(
                            LanguageManager.get("exchange.stocks.empty.search"),
                            currentFilterTerm);
            table.renderEmptyState(msg);
            notifyRefreshed();
            return;
        }

        notifyRefreshed();
    }

    private void notifyRefreshed() {
        if (onRefreshed != null) {
            onRefreshed.run();
        }
    }

    /**
     * Rebuilds {@link #filteredStocks} from {@link #allStocks} and the current filter
     * term, then clamps {@link #currentPage} into the valid range. Does not reset the page.
     * Called by {@link #filter}, {@link #onGameUpdated} and sort-clear flows.
     */
    private void syncFilteredList() {
        filteredStocks = currentFilterTerm.isBlank()
                ? new ArrayList<>(allStocks)
                : new ArrayList<>(gameService.getExchange().findStocks(currentFilterTerm));
        clampCurrentPage(filteredStocks.size());
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
     * Use this to update external controls such as {@link Pagination}.
     *
     * @param onRefreshed the callback to run after each refresh
     */
    public void setOnRefreshed(Runnable onRefreshed) {
        this.onRefreshed = onRefreshed;
    }

    /**
     * Called when the game state changes (week advanced, buy or sell).
     * Reloads the stock list from the exchange and syncs the filtered list
     * without resetting the current page.
     */
    @Override
    public void onGameUpdated() {
        allStocks = new ArrayList<>(gameService.getExchange().getStocks());
        syncFilteredList();
        refresh();
    }

    /**
     * Called when the application language changes.
     * Re-renders headers and pagination with updated labels.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("exchange.stocks.market"));
        refresh();
    }
}
