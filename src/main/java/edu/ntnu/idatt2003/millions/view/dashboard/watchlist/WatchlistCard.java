package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.model.watchlist.WatchlistEntry;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.ExploreStocksButton;
import edu.ntnu.idatt2003.millions.view.component.Pagination;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.SortableTableCard;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
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
 * <p>Extends {@link SortableTableCard} for shared pagination, search, sort state,
 * and the common refresh Template Method. Row rendering is delegated to
 * {@link WatchlistRowRenderer}. The header row contains the section title on the
 * left and an explore-stocks button on the right.</p>
 */
public class WatchlistCard extends SortableTableCard<WatchlistItem, WatchlistSort.SortColumn> {

    private static final int PAGE_SIZE = 8;
    private static final double COL_GAP = 8;

    private final GameService gameService;
    private final WatchlistRowRenderer rowRenderer;
    private final StyledText title;
    private final Runnable onExploreStocks;

    /**
     * Constructs a new WatchlistCard.
     *
     * @param gameService     the game service containing player and exchange state
     * @param tradeController the controller used to open buy dialogs
     * @param onExploreStocks callback invoked when the player clicks the explore button
     */
    public WatchlistCard(GameService gameService,
                         TradeController tradeController,
                         Runnable onExploreStocks) {
        super(gameService, PAGE_SIZE, "watchlist.status", "watchlist.empty");
        this.gameService = gameService;
        this.onExploreStocks = onExploreStocks;

        WatchlistSort sort = new WatchlistSort(gameService.getCurrencyConverter());
        this.sortProvider = sort;
        this.rowRenderer = new WatchlistRowRenderer(
                gameService, tradeController,
                gameService::removeFromWatchlist,
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
                .collect(Collectors.toCollection(ArrayList::new));
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
    protected void renderPage(List<WatchlistItem> page) {
        for (int i = 0; i < page.size(); i++) {
            rowRenderer.buildRow(page.get(i), i + 1, table);
        }
    }

    @Override
    protected String emptyStateMessage(String term) {
        if (!term.isBlank()) {
            return MessageFormat.format(LanguageManager.get("watchlist.empty.search"), term);
        }
        return LanguageManager.get("watchlist.empty");
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

    private void openNoteDialog(WatchlistItem item) {
        WatchlistNoteDialog dialog = new WatchlistNoteDialog(
                item.stock().getSymbol(), item.entry().note());
        dialog.setOnSave(note ->
                gameService.updateWatchlistNote(item.stock().getSymbol(), note));
        dialog.show();
    }
}
