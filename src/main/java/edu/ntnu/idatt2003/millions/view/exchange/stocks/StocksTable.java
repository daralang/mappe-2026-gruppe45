package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Sortable, searchable and paginated table of all stocks listed on the exchange.
 *
 * <p>Displays ticker (with owner badge), company name, current price, weekly
 * change in NOK and percent, a SparklineChart trend and buy/sell action buttons.
 * Registers as a {@link GameObserver}.
 *
 * <p>Sorting state is tracked via the {@link SortColumn} enum. Only one column
 * can be active at a time; clicking the same header again reverses direction.
 */
public class StocksTable extends VBox implements GameObserver {

    private static final int PAGE_SIZE = 15;
    private static final int MAX_SPARKLINE_WEEKS = 8;

    /** Columns that support ascending/descending sort. */
    private enum SortColumn {
        TICKER, PRICE, CHANGE_KR, CHANGE_PCT
    }

    private final GameService gameService;
    private final PortfolioController controller;

    private List<Stock> allStocks = new ArrayList<>();
    private List<Stock> filteredStocks = new ArrayList<>();

    private int currentPage = 0;
    private SortColumn activeSortColumn = null;
    private boolean sortAscending = true;

    private final TextField searchField = new TextField();
    private final Label statusLabel = new Label();
    private final GridPane grid = new GridPane();
    private final HBox paginationBar = new HBox(8);

    /**
     * Constructs a new StocksTable.
     *
     * @param gameService the game service containing exchange and player state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksTable(GameService gameService, PortfolioController controller) {
        this.gameService = gameService;
        this.controller = controller;

        setSpacing(12);

        gameService.addObserver(this);
        LanguageManager.addObserver(this::onLanguageChanged);

        grid.setHgap(16);
        configureColumns();

        getChildren().addAll(buildSearchBar(), grid, paginationBar);
        refresh();
    }

    /**
     * Configures the percentage widths and horizontal alignments of the table columns.
     * Column order: ticker, company, price, change NOK, change %, trend, trade.
     */
    private void configureColumns() {
        double[] widths = {12, 25, 10, 10, 10, 12, 21};
        HPos[] alignments = {
                HPos.LEFT, HPos.LEFT, HPos.RIGHT,
                HPos.RIGHT, HPos.RIGHT, HPos.CENTER, HPos.LEFT
        };
        for (int i = 0; i < widths.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(widths[i]);
            col.setHalignment(alignments[i]);
            grid.getColumnConstraints().add(col);
        }
    }

    /**
     * Builds the search field and status label area.
     * Wires real-time filtering on the search field's text property.
     *
     * @return a VBox containing the search field and status label
     */
    private VBox buildSearchBar() {
        return new VBox(4, searchField, statusLabel);
    }


    /**
     * Rebuilds the table from the current filtered and sorted stock list.
     * Resets to page 0 when the stock list or filter changes.
     */
    private void refresh() {
    }

    /**
     * Filters {@link #allStocks} by the current search field text into
     * {@link #filteredStocks}. Resets {@link #currentPage} to 0.
     */
    private void applyFilter() {
    }

    /**
     * Sorts {@link #filteredStocks} in-place according to {@link #activeSortColumn}
     * and {@link #sortAscending}. No-op when {@link #activeSortColumn} is {@code null}.
     */
    private void applySort() {
    }

    /**
     * Builds and adds the header row and data rows for the current page to the grid.
     */
    private void buildTableHeader() {
    }

    /**
     * Builds a single data row for the given stock at the specified grid row index.
     *
     * @param stock    the stock to display
     * @param rowIndex the zero-based grid row index (0 = header)
     */
    private void buildDataRow(Stock stock, int rowIndex) {
    }

    /**
     * Builds the buy and optional sell buttons for the trade column.
     * Always shows a buy button; shows a sell button when the player owns the stock.
     *
     * @param stock the stock the buttons act on
     * @return an HBox containing the action buttons
     */
    private HBox buildTradeButtons(Stock stock) {
        return new HBox();
    }


    /**
     * Rebuilds the pagination bar for the current page and total filtered stock count.
     */
    private void buildPagination() {
    }

    /**
     * Creates a pagination button with the given label text.
     * Clicking the button sets {@link #currentPage} to {@code targetPage}
     * and calls {@link #refresh()}.
     *
     * @param text       the button label
     * @param targetPage the page index to navigate to on click
     * @param disabled   whether the button should be disabled
     * @return a configured Button
     */
    private Button buildPageButton(String text, int targetPage, boolean disabled) {
        return new Button(text);
    }

    /**
     * Called when the game state changes (week advanced, buy or sell).
     * Reloads the stock list from the exchange and refreshes the table.
     */
    @Override
    public void onGameUpdated() {
        allStocks = new ArrayList<>(gameService.getExchange().getStocks());
        applyFilter();
        refresh();
    }

    /**
     * Called when the application language changes.
     * Updates placeholder text and re-renders headers and status.
     */
    private void onLanguageChanged() {
        searchField.setPromptText(LanguageManager.get("exchange.stocks.search.placeholder"));
        refresh();
    }
}
