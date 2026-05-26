package edu.ntnu.idatt2003.millions.view.dashboard.watchlist.card;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.DetailTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.WatchlistItem;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.dialog.WatchlistNoteDialog;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.dialog.WatchlistRemoveDialog;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.WatchlistRowRenderer;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.WatchlistSort;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Card displaying the player's watchlist as a sortable, searchable, paginated table.
 *
 * <p>Extends {@link DetailTableCard} for shared pagination, search, sort state,
 * the common refresh Template Method, and row-activation. </p>
 */
public class WatchlistCard extends DetailTableCard<WatchlistItem, WatchlistSort.SortColumn> {

    private static final int PAGE_SIZE = 8;
    private static final double COL_GAP = 8;

    private final GameService gameService;
    private final WatchlistRowRenderer rowRenderer;
    private final StyledText title;

    /**
     * Constructs a new WatchlistCard.
     *
     * @param gameService     the game service containing player and exchange state
     * @param tradeController the controller used to open buy and detail dialogs
     */
    public WatchlistCard(GameService gameService,
                         TradeController tradeController) {
        super(gameService, tradeController, PAGE_SIZE, "watchlist.status", "watchlist.empty");
        this.gameService = gameService;

        WatchlistSort sort = new WatchlistSort(gameService.getCurrencyConverter());
        this.sortProvider = sort;
        this.rowRenderer = new WatchlistRowRenderer(
                gameService, controller,
                this::openRemoveDialog,
                this::openNoteDialog);
        this.table = new SortColumnTable<>(sort::getColumnDefs, COL_GAP);
        this.pagination = new Pagination(PAGE_SIZE, this::setPage);
        this.title = StyledText.sectionTitle(LanguageManager.get("watchlist.title"));

        Button clearSortButton = table.createClearSortButton(
                () -> LanguageManager.get("watchlist.sort.clear"), this::refresh);

        setSpacing(12);
        setMinWidth(0);


        getChildren().addAll(
                buildTitleRow(),
                buildSearchRow(clearSortButton),
                table.asNode(),
                pagination
                );
        refresh();
    }

    @Override
    protected List<WatchlistItem> fetchAll() {
        if (gameService.getPlayer() == null) return new ArrayList<>();
        return gameService.getPlayer().getWatchlist().stream()
                .map(entry -> {
                    Stock stock = gameService.getExchange().getStock(entry.symbol());
                    return stock != null ? new WatchlistItem(stock, entry) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    protected List<WatchlistItem> applySearch(List<WatchlistItem> all, String term) {
        if (term.isBlank()) return new ArrayList<>(all);
        String lower = term.toLowerCase();
        return all.stream()
                .filter(i -> i.stock().getSymbol().toLowerCase().contains(lower)
                        || i.stock().getCompany().toLowerCase().contains(lower))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    protected RowCells<WatchlistSort.SortColumn> buildRowCells(WatchlistItem item, int rowIndex) {
        return rowRenderer.buildRow(item)
                .put(WatchlistSort.SortColumn.DETAILS, buildDetailChevron(item));
    }

    @Override
    protected String emptyStateMessage(String term) {
        if (!term.isBlank()) {
            return MessageFormat.format(LanguageManager.get("watchlist.empty.search"), term);
        }
        return LanguageManager.get("watchlist.empty");
    }

    /**
     * Opens the stock detail modal for the given watchlist item. Whenever the user
     * activates a row via keyboard or mouse click on a data cell.
     *
     * @param item the watchlist item whose stock to show details for
     */
    @Override
    protected void openDetail(WatchlistItem item) {
        controller.openStockDetail(item.stock());
    }

    @Override
    protected void onLanguageChanged() {
        title.setText(LanguageManager.get("watchlist.title"));
        refresh();
    }

    private HBox buildTitleRow() {
        HBox row = new HBox(title);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Opens a {@link WatchlistRemoveDialog} for the given stock symbol.
     * Delegates the confirmed removal to
     * {@link edu.ntnu.idatt2003.millions.controller.TradeController#removeFromWatchlist(String)}
     * so that the controller can show the appropriate toast notification.
     *
     * @param symbol the symbol of the stock to remove
     */
    private void openRemoveDialog(String symbol) {
        Stock stock = gameService.getExchange().getStock(symbol);
        if (stock == null) return;
        WatchlistRemoveDialog dialog = new WatchlistRemoveDialog(
                stock, () -> controller.removeFromWatchlist(symbol));
        dialog.show();
    }

    private void openNoteDialog(WatchlistItem item) {
        WatchlistNoteDialog dialog = new WatchlistNoteDialog(
                item.stock(), gameService.getCurrencyConverter(), item.entry().note());
        dialog.setOnSave(note ->
                gameService.updateWatchlistNote(item.stock().getSymbol(), note));
        dialog.show();
    }
}
