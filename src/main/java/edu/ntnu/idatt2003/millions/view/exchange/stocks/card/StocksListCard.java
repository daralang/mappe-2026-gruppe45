package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Market card with controls for searching, sorting, and paginating stocks listed on the exchange.
 *
 * <p>Displays a title, a {@link SearchBar} with result metadata, reset-sort action,
 * pagination, ticker, company, prices, weekly change, 4-week high/low, trend and
 * trade actions. Column structure and sort state are owned by {@link SortColumnTable};
 * domain-specific sort logic is delegated to {@link StocksSort}; row rendering is
 * delegated to {@link StocksRowRenderer}.</p>
 */
public class StocksListCard extends Card {

    public static final int PAGE_SIZE = Pagination.DEFAULT_PAGE_SIZE;

    private final GameService gameService;
    private final StocksSort sort;
    private final StocksRowRenderer rowRenderer;
    private final SortColumnTable<StocksSort.SortColumn> table;
    private final StyledText title;
    private final StyledText statusLabel = StyledText.widgetLabel();
    private final Button clearSortButton;

    private List<Stock> allStocks = new ArrayList<>();
    private List<Stock> filteredStocks = new ArrayList<>();

    private int currentPage = 0;
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
        super(gameService);
        this.gameService = gameService;
        this.sort = new StocksSort(gameService.getCurrencyConverter());
        this.rowRenderer = new StocksRowRenderer(gameService, controller);
        this.table = new SortColumnTable<>(sort::getColumnDefs, 10);
        this.title = StyledText.sectionTitle(LanguageManager.get("exchange.stocks.market"));
        this.clearSortButton = new Button(LanguageManager.get("exchange.stocks.sort.clear"));

        setSpacing(12);
        setMinWidth(0);

        configureClearSortButton();
        getChildren().addAll(title, createSearchBar(), table.asNode());
        onGameUpdated();
    }

    private SearchBar createSearchBar() {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                this::filter,
                createMetadataRow());
        searchBar.setMaxWidth(Double.MAX_VALUE);
        return searchBar;
    }

    private HBox createMetadataRow() {
        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        HBox metadataRow = new HBox(statusLabel, statusSpacer, clearSortButton);
        metadataRow.setAlignment(Pos.CENTER_LEFT);
        metadataRow.setMaxWidth(Double.MAX_VALUE);
        return metadataRow;
    }

    private void configureClearSortButton() {
        clearSortButton.getStyleClass().add("clear-sort-button");
        clearSortButton.setVisible(false);
        clearSortButton.setOnAction(e -> clearSort());
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
     * Rebuilds the table from the current filtered and sorted stock list.
     * If a sort is active, delegates sorting to {@link StocksSort#applySort}.
     * If no sort is active, restores the original exchange order via {@link #restoreOrder()}.
     */
    private void refresh() {
        if (table.isSortActive()) {
            sort.applySort(filteredStocks, table.getSortState());
        } else {
            restoreOrder();
        }

        table.clearRows();
        table.refreshHeader(this::refresh);
        clearSortButton.setVisible(table.isSortActive());
        updateStatus();

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
     * Updates the status label to reflect the current filtered and total stock counts.
     * Uses the {@code exchange.stocks.status} i18n key with two positional arguments.
     */
    private void updateStatus() {
        statusLabel.setText(MessageFormat.format(
                LanguageManager.get("exchange.stocks.status"),
                filteredStocks.size(),
                allStocks.size()));
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
        clampCurrentPage();
    }

    private void clampCurrentPage() {
        int lastPage = Math.max(0, (filteredStocks.size() - 1) / PAGE_SIZE);
        if (currentPage > lastPage) {
            currentPage = lastPage;
        }
    }

    /**
     * Returns whether a sort column is currently active.
     * Delegates to {@link SortColumnTable#isSortActive()}.
     *
     * @return {@code true} if a sort is active, {@code false} otherwise
     */
    public boolean isSortActive() {
        return table.isSortActive();
    }

    /**
     * Clears the active sort and refreshes the table, restoring the original
     * exchange order. Delegates to {@link SortColumnTable#clearSort()}.
     */
    public void clearSort() {
        table.clearSort();
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
     * Use this to update external controls such as {@link Pagination}.
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
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("exchange.stocks.market"));
        clearSortButton.setText(LanguageManager.get("exchange.stocks.sort.clear"));
        refresh();
    }
}
