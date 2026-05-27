package edu.ntnu.idatt2003.millions.view.ingame.exchange.overview.card;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.exchange.Exchange;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.format.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.util.format.TableCells;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.ChevronButton;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.Card;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.SortColumnTable;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.TableColumnDef;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import javafx.geometry.HPos;
import javafx.scene.control.Label;

/**
 * Card displaying a ranked list of stocks with price and weekly change.
 * Used for both winners and losers in the exchange overview.
 */
public class StockRankingCard extends Card {

    private static final double COL_DETAILS =  15;
    private static final double COL_TICKER = 10;
    private static final double COL_COMPANY = 40;
    private static final double COL_PRICE = 20;
    private static final double COL_CHANGE = 15;

    /** Column keys for the ranking table; all non-sortable. */
    private enum Col { TICKER, COMPANY, PRICE, CHANGE, DETAILS}

    private final GameService gameService;
    private final TradeController controller;
    private final String titleKey;
    private final Function<Exchange, List<Stock>> selector;
    private final StyledText titleLabel;
    private final SortColumnTable<Col> table;

    /**
     * Constructs a {@code StockRankingCard}.
     *
     * @param gameService the game service supplying the exchange and observer registration
     * @param controller  the controller used to open the stock detail modal
     * @param titleKey    i18n key for the table title
     * @param selector    selects which stocks to display, e.g. {@code ex -> ex.getGainers(5)}
     * @throws NullPointerException if any argument is null
     */
    public StockRankingCard(GameService gameService, TradeController controller,
                            String titleKey, Function<Exchange, List<Stock>> selector) {
        super(gameService);
        this.gameService = gameService;
        this.controller = Objects.requireNonNull(controller, "controller cannot be null");
        this.titleKey = Objects.requireNonNull(titleKey, "titleKey cannot be null");
        this.selector = Objects.requireNonNull(selector, "selector cannot be null");
        setSpacing(12);

        titleLabel = StyledText.widgetValue(LanguageManager.get(titleKey));
        table = new SortColumnTable<>(this::columnDefs);
        getChildren().addAll(titleLabel, table.asNode());

        renderCurrent();
    }

    /** Re-renders rows with the latest exchange data. */
    @Override
    public void onGameUpdated() {
        renderCurrent();
    }

    /** Refreshes the title and re-renders all rows. */
    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get(titleKey));
        renderCurrent();
    }

    /** Clears and rebuilds the table from the stocks returned by {@code selector}. */
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
     * Builds and inserts one selectable data row for {@code stock}.
     *
     * @param rowIndex grid row index (row 0 is the header)
     * @param stock    the stock to display
     */
    private void addDataRow(int rowIndex, Stock stock) {
        Label changeLabel = ChangeFormatter.styledPercent(
                stock.getWeeklyChangePercent(), "table-cell");
        ChevronButton chevron = new ChevronButton(
                () -> openDetail(stock), "tooltip.stocks.chevron");
        chevron.setFocusTraversable(true);

        RowCells<Col> cells = RowCells.<Col>builder()
                .put(Col.TICKER, TableCells.data(stock.getSymbol()))
                .put(Col.COMPANY,TableCells.data(stock.getCompany()))
                .put(Col.PRICE, TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice())))
                .put(Col.CHANGE, changeLabel)
                .put(Col.DETAILS,chevron);

        table.addSelectableRow(rowIndex, chevron, () -> openDetail(stock), cells);
    }

    /**
     * Opens the stock detail modal for {@code stock}.
     *
     * @param stock the stock to show details for
     */
    private void openDetail(Stock stock) {
        controller.openStockDetail(stock);
    }

    /**
     * Returns the column definitions for this table.
     *
     * @return ordered list of {@link TableColumnDef}
     */
    private List<TableColumnDef<Col>> columnDefs() {
        List<Stock> stocks = selector.apply(gameService.getExchange());
        String currencyCode = stocks.isEmpty()
                ? "" : stocks.get(0).getCurrency().getCurrencyCode();
        String priceHeader = MessageFormat.format(
                LanguageManager.get("col.priceNative"), currencyCode);

        return List.of(
                TableColumnDef.nonSortable(Col.TICKER,"col.ticker", COL_TICKER, HPos.LEFT),
                TableColumnDef.nonSortable(Col.COMPANY, "col.stock", COL_COMPANY, HPos.LEFT),
                new TableColumnDef<>(() -> priceHeader, Col.PRICE, false, null, COL_PRICE, HPos.RIGHT),
                TableColumnDef.nonSortable(Col.CHANGE,  "col.change", COL_CHANGE, HPos.RIGHT),
                TableColumnDef.nonSortable(Col.DETAILS, "col.details", COL_DETAILS, HPos.CENTER)
        );
    }
}
