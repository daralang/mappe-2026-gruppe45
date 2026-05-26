package edu.ntnu.idatt2003.millions.view.ingame.exchange.overview.card;

import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.util.format.TableCells;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.Card;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.TableColumnDef;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.geometry.HPos;
import javafx.scene.control.Label;

/**
 * Card displaying a ranked list of stocks with their current price and weekly
 * percentage change. Used for both winners and losers in the exchange overview.
 *
 * <p>Extends {@link Card}, so it registers as a game and language observer through
 * the base class and refreshes itself: {@link #onGameUpdated()} and
 * {@link #onLanguageChanged()} both re-render the rows from the supplied
 * {@code selector}, which picks the stocks to show (e.g. gainers or losers) out of
 * the current {@link Exchange}. Rendered as a {@link SortColumnTable} with five
 * non-sortable columns so cell height and padding match the other dashboard tables.</p>
 */
public class StockRankingCard extends Card {

    private static final double COL_TICKER   = 18;
    private static final double COL_COMPANY  = 35;
    private static final double COL_CURRENCY = 13;
    private static final double COL_PRICE    = 10;
    private static final double COL_CHANGE   = 10;

    /** Column keys for the ranking table; non-sortable, used only as {@link RowCells} keys. */
    private enum Col { TICKER, COMPANY, CURRENCY, PRICE, CHANGE }

    private final GameService gameService;
    private final String titleKey;
    private final Function<Exchange, List<Stock>> selector;
    private final StyledText titleLabel;
    private final SortColumnTable<Col> table;

    /**
     * Constructs a StockRankingCard.
     *
     * @param gameService the game service supplying the exchange and observer registration
     * @param titleKey    the i18n key for the table title
     * @param selector    selects which stocks to display from the current exchange,
     *                    e.g. {@code ex -> ex.getGainers(5)}
     * @throws NullPointerException if any argument is null
     */
    public StockRankingCard(GameService gameService, String titleKey,
                            Function<Exchange, List<Stock>> selector) {
        super(gameService);
        this.gameService = gameService;
        this.titleKey = Objects.requireNonNull(titleKey, "titleKey cannot be null");
        this.selector = Objects.requireNonNull(selector, "selector cannot be null");
        setSpacing(12);

        titleLabel = StyledText.widgetValue(LanguageManager.get(titleKey));
        table = new SortColumnTable<>(this::columnDefs);
        getChildren().addAll(titleLabel, table.asNode());

        renderCurrent();
    }

    /**
     * Re-renders the ranking rows with the latest stocks from the exchange.
     */
    @Override
    public void onGameUpdated() {
        renderCurrent();
    }

    /**
     * Refreshes the title and re-renders all rows to reflect the active
     * language and currency.
     */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get(titleKey));
        renderCurrent();
    }

    /**
     * Clears and rebuilds the table from the stocks the {@code selector} picks out
     * of the current exchange, showing an empty-state message when there are none.
     */
    private void renderCurrent() {
        List<Stock> stocks = selector.apply(gameService.getExchange());
        table.clearRows();
        table.refreshHeader(() -> {});
        if (stocks.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("exchange.overview.noWeeklyData"));
            return;
        }
        int row = 1;
        for (Stock stock : stocks) {
            addDataRow(row++, stock);
        }
    }

    /**
     * Builds and inserts one data row for the given stock at the specified row index.
     *
     * @param rowIndex the grid row to write to (row 0 is reserved for the header)
     * @param stock    the stock to display
     */
    private void addDataRow(int rowIndex, Stock stock) {
        Label changeLabel = ChangeFormatter.styledPercent(
                stock.getWeeklyChangePercent(), "table-cell");
        table.addRow(rowIndex, RowCells.<Col>builder()
                .put(Col.TICKER, TableCells.data(stock.getSymbol()))
                .put(Col.COMPANY, TableCells.data(stock.getCompany()))
                .put(Col.CURRENCY, TableCells.data(stock.getCurrency().getCurrencyCode()))
                .put(Col.PRICE, TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice())))
                .put(Col.CHANGE, changeLabel));
    }

    /**
     * Returns the column definitions for this table.
     * Called by {@link SortColumnTable} on every header refresh so that labels
     * reflect the active language and currency.
     *
     * @return a list of five {@link TableColumnDef} in display order
     */
    private List<TableColumnDef<Col>> columnDefs() {
        return List.of(
                TableColumnDef.nonSortable(Col.TICKER,   "col.ticker",      COL_TICKER,   HPos.LEFT),
                TableColumnDef.nonSortable(Col.COMPANY,  "col.stock",       COL_COMPANY,  HPos.LEFT),
                TableColumnDef.nonSortable(Col.CURRENCY, "col.currency",    COL_CURRENCY, HPos.LEFT),
                TableColumnDef.nonSortable(Col.PRICE,    "col.priceNative", COL_PRICE,    HPos.RIGHT),
                TableColumnDef.nonSortable(Col.CHANGE,   "col.change",      COL_CHANGE,   HPos.RIGHT)
        );
    }
}
