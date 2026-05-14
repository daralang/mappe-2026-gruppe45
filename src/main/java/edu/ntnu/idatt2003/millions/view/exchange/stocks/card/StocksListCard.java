package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort;
import javafx.scene.control.Button;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Market card with controls for searching, sorting, and paginating stocks listed on the exchange.
 * Extends {@link SortableTableCard} for shared pagination, search, sort state, and refresh algorithm.
 *
 * <p>Column structure and sort state are owned by {@link SortColumnTable};
 * domain-specific sort logic is delegated to {@link StocksSort};
 * row rendering is delegated to {@link StocksRowRenderer}.</p>
 */
public class StocksListCard extends SortableTableCard<Stock, StocksSort.SortColumn> {

    public static final int PAGE_SIZE = 20;
    private static final double ROW_HEIGHT = 34.0;

    private final GameService gameService;
    private final StocksSort sort;
    private final StocksRowRenderer rowRenderer;
    private final StyledText title;

    /**
     * Constructs a new StocksListCard.
     *
     * @param gameService the game service containing exchange and player state
     * @param controller  the controller used to open buy/sell dialogs
     */
    public StocksListCard(GameService gameService, PortfolioController controller) {
        super(gameService, PAGE_SIZE, "exchange.stocks.status", "exchange.stocks.empty");
        this.gameService = gameService;
        this.sort = new StocksSort(gameService.getCurrencyConverter());
        this.sortProvider = sort;
        this.rowRenderer = new StocksRowRenderer(gameService, controller);
        this.table = new SortColumnTable<>(sort::getColumnDefs, 10);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        this.title = StyledText.sectionTitle(LanguageManager.get("exchange.stocks.market"));
        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("exchange.stocks.sort.clear"), this::refresh);

        setSpacing(12);
        setMinWidth(0);
        table.setMinHeight(PAGE_SIZE * ROW_HEIGHT);

        getChildren().addAll(title, buildSearchRow(clearSortButton), table.asNode(), pagination);
        refresh();
    }

    @Override
    protected List<Stock> fetchAll() {
        return new ArrayList<>(gameService.getExchange().getStocks());
    }

    @Override
    protected List<Stock> applySearch(List<Stock> all, String term) {
        if (term.isBlank()) {
            return new ArrayList<>(all);
        }
        return new ArrayList<>(gameService.getExchange().findStocks(term));
    }

    @Override
    protected void renderPage(List<Stock> page) {
        for (int i = 0; i < page.size(); i++) {
            rowRenderer.buildRow(page.get(i), i + 1, table);
        }
    }

    @Override
    protected String emptyStateMessage(String term) {
        if (term.isBlank()) {
            return LanguageManager.get("exchange.stocks.empty");
        }
        return MessageFormat.format(LanguageManager.get("exchange.stocks.empty.search"), term);
    }

    /**
     * Re-renders headers and pagination with updated labels.
     */
    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("exchange.stocks.market"));
        refresh();
    }
}
