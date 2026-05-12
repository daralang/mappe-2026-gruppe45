package edu.ntnu.idatt2003.millions.view.exchange.stocks;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.observer.GameObserver;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.SparklineChart;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
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
        searchField.setPromptText(LanguageManager.get("exchange.stocks.search.placeholder"));
        //TODO: Connect to a css file, searchField.getStyleClass().add("stocks-search-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            applyFilter();
            refresh();
        });

        //TODO: Connect to a css file, statusLabel.getStyleClass().add("stocks-status-label");
        updateStatusLabel();

        return new VBox(4, searchField, statusLabel);
    }

    /**
     * Updates the status label text to reflect the current filtered and total stock counts.
     * Uses {@link java.text.MessageFormat} to inject the counts into the i18n pattern.
     */
    private void updateStatusLabel() {
        statusLabel.setText(java.text.MessageFormat.format(
                LanguageManager.get("exchange.stocks.status"),
                filteredStocks.size(),
                allStocks.size()
        ));
    }


    /**
     * Rebuilds the table from the current filtered and sorted stock list.
     * Applies sort, clears the grid, renders the header and the current page of rows,
     * then updates the pagination bar and status label.
     */
    private void refresh() {
        applySort();

        grid.getChildren().clear();
        buildTableHeader();

        int fromIndex = currentPage * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredStocks.size());
        List<Stock> page = filteredStocks.subList(fromIndex, toIndex);

        for (int i = 0; i < page.size(); i++) {
            buildDataRow(page.get(i), i + 1);
        }

        buildPagination();
        updateStatusLabel();
    }

    /**
     * Filters all stocks by the current search field text into.
     * Resets {@link #currentPage} to 0.
     * Delegates to findStock in Exchange.
     * when a search term is present; uses the full list otherwise.
     */
    private void applyFilter() {
        String term = searchField.getText();
        filteredStocks = term == null || term.isBlank()
                ? new ArrayList<>(allStocks)
                : new ArrayList<>(gameService.getExchange().findStocks(term));
        currentPage = 0;
    }

    /**
     * Sorts all stocks in-place according to active sort button with ascending.
     * No-op when active sort is {@code null}.
     */
    private void applySort() {
        if (activeSortColumn == null) return;

        java.util.Comparator<Stock> comparator = switch (activeSortColumn) {
            case TICKER -> java.util.Comparator.comparing(Stock::getSymbol);
            case PRICE -> java.util.Comparator.comparing(Stock::getSalesPrice);
            case CHANGE_KR -> java.util.Comparator.comparing(Stock::getLatestPriceChange);
            case CHANGE_PCT -> java.util.Comparator.comparing(Stock::getWeeklyChangePercent);
        };

        if (!sortAscending) comparator = comparator.reversed();
        filteredStocks.sort(comparator);
    }

    /**
     * Builds and adds the header row to the grid.
     * Sortable columns render as clickable buttons with a ↑/↓ indicator.
     * Non-sortable columns render as plain labels.
     */
    private void buildTableHeader() {
        grid.add(buildSortableHeader("exchange.stocks.col.ticker",    SortColumn.TICKER),     0, 0);
        grid.add(buildStaticHeader("exchange.stocks.col.company"),                             1, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.price",     SortColumn.PRICE),      2, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.changeKr",  SortColumn.CHANGE_KR),  3, 0);
        grid.add(buildSortableHeader("exchange.stocks.col.changePct", SortColumn.CHANGE_PCT), 4, 0);
        grid.add(buildStaticHeader("exchange.stocks.col.trend"),                               5, 0);
        grid.add(buildStaticHeader("exchange.stocks.col.trade"),                               6, 0);
    }

    /**
     * Builds a sortable header button for the given column.
     * Appends ↑ or ↓ when this column is the active sort column.
     * Clicking toggles sort direction if already active, or activates ascending sort otherwise.
     *
     * @param labelKey the i18n key for the column label
     * @param column   the sort column this header controls
     * @return a styled Button acting as the column header
     */
    private Button buildSortableHeader(String labelKey, SortColumn column) {
        String indicator = activeSortColumn == column ? (sortAscending ? " ↑" : " ↓") : "";
        Button header = new Button(LanguageManager.get(labelKey) + indicator);
        header.getStyleClass().add("holdings-header");
        //TODO: Connect to a css file, header.getStyleClass().add("stocks-header-button");
        header.setOnAction(e -> {
            if (activeSortColumn == column) {
                sortAscending = !sortAscending;
            } else {
                activeSortColumn = column;
                sortAscending = true;
            }
            refresh();
        });
        return header;
    }

    /**
     * Builds a non-sortable header label for the given column.
     *
     * @param labelKey the i18n key for the column label
     * @return a styled Label
     */
    private Label buildStaticHeader(String labelKey) {
        Label header = new Label(LanguageManager.get(labelKey));
        header.getStyleClass().add("holdings-header");
        return header;
    }

    /**
     * Builds a single data row for the given stock at the specified grid row index.
     * Renders the ticker label with an owner badge,company name, current price,
     * weekly change in NOK and percent, and {@link SparklineChart} with the last ¨
     * {@link #MAX_SPARKLINE_WEEKS} prices, and buy/sell action buttons.
     *
     * @param stock    the stock to display
     * @param rowIndex the grid row index (0 = header)
     */
    private void buildDataRow(Stock stock, int rowIndex) {
        Label tickerLabel = new Label(stock.getSymbol());
        //TODO: Connect to a CSS file, tickerLabel.getStyleClass().add("holdings-ticker");

        List<Share> ownedShares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        VBox tickerCell = new VBox(2, tickerLabel);
        if (!ownedShares.isEmpty()) {
            Label badge = new Label(LanguageManager.get("exchange.stocks.badge.owner"));
            //TODO: Connect to a CSS file, badge.getStyleClass().add("holdings-owner-badge");
            tickerCell.getChildren().add(badge);
        }

        Label companyLabel = new Label(stock.getCompany());

        Label priceLabel = new Label(ChangeFormatter.formatPlain(stock.getSalesPrice()));
        //TODO: Connect to a CSS file, priceLabel.getStyleClass().add("holdings-value");

        Label changeKrLabel = ChangeFormatter.styledAmount(stock.getLatestPriceChange());
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent());

        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - MAX_SPARKLINE_WEEKS), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);

        grid.add(tickerCell,               0, rowIndex);
        grid.add(companyLabel,             1, rowIndex);
        grid.add(priceLabel,               2, rowIndex);
        grid.add(changeKrLabel,            3, rowIndex);
        grid.add(changePctLabel,           4, rowIndex);
        grid.add(sparkline,                5, rowIndex);
        grid.add(buildTradeButtons(stock), 6, rowIndex);
    }

    /**
     * Builds the buy and optional sell buttons for the trade column.
     *
     * <p>Always shows a buy button that delegates to
     * {@link PortfolioController#openBuyDialog(Stock)}.
     * Shows a sell button when the player holds at least one {@link Share} of the stock,
     * delegating to {@link PortfolioController#openSellDialog(Share)} with the first position.
     *
     * @param stock the stock the buttons act on
     * @return an HBox containing the action buttons
     */
    private HBox buildTradeButtons(Stock stock) {
        Button buyButton = new Button(LanguageManager.get("exchange.stocks.buy"));
        buyButton.getStyleClass().add("buy-button");
        buyButton.setOnAction(e -> controller.openBuyDialog(stock));

        HBox buttons = new HBox(4, buyButton);

        List<Share> shares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        if (!shares.isEmpty()) {
            Share share = shares.get(0);
            Button sellButton = new Button(LanguageManager.get("exchange.stocks.sell"));
            sellButton.getStyleClass().add("sell-button");
            sellButton.setOnAction(e -> controller.openSellDialog(share));
            buttons.getChildren().add(sellButton);
        }

        return buttons;
    }


    /**
     * Rebuilds the pagination bar for the current page and total filtered stock count.
     *
     * <p>Renders a Prev button, one numbered button per page, and a Next button.
     * The Prev button is disabled on the first page; Next is disabled on the last.
     * The active page button receives the {@code pagination-active} style class.
     * The bar is cleared and left empty when there is only one page.
     */
    private void buildPagination() {
        paginationBar.getChildren().clear();
        int totalPages = (int) Math.ceil((double) filteredStocks.size() / PAGE_SIZE);
        if (totalPages <= 1) return;

        paginationBar.getChildren().add(
                buildPageButton(LanguageManager.get("exchange.stocks.pagination.prev"),
                        currentPage - 1, currentPage == 0));

        for (int i = 0; i < totalPages; i++) {
            Button pageBtn = buildPageButton(String.valueOf(i + 1), i, false);
            if (i == currentPage) {
                pageBtn.getStyleClass().add("pagination-active");
            }
            paginationBar.getChildren().add(pageBtn);
        }

        paginationBar.getChildren().add(
                buildPageButton(LanguageManager.get("exchange.stocks.pagination.next"),
                        currentPage + 1, currentPage >= totalPages - 1));
    }

    /**
     * Creates a pagination button with the given label text.
     *
     * <p>Clicking the button sets {@link #currentPage} to {@code targetPage}
     * and calls {@link #refresh()}.
     *
     * @param text       the button label
     * @param targetPage the page index to navigate to on click
     * @param disabled   whether the button should be disabled
     * @return a configured Button
     */
    private Button buildPageButton(String text, int targetPage, boolean disabled) {
        Button button = new Button(text);
        button.getStyleClass().add("pagination-button");
        button.setDisable(disabled);
        button.setOnAction(e -> {
            currentPage = targetPage;
            refresh();
        });
        return button;
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
