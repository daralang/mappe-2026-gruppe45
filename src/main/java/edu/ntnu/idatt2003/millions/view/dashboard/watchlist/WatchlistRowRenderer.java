package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.RowRenderer;
import edu.ntnu.idatt2003.millions.view.component.chart.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Responsible for building the column-keyed cells for a single {@link WatchlistItem} row.
 *
 * <p>Each call to {@link #buildRow(WatchlistItem)} produces a {@link RowCells} map with
 * ticker, company, price in NOK, price in the stock's native currency, weekly change
 * (NOK and %), 4-week high/low, a sparkline trend, a buy button, a note button, and a
 * remove button. The owning card inserts the cells and registers the row for navigation.</p>
 *
 * <p>This class is stateless and may be reused across refreshes.</p>
 */
class WatchlistRowRenderer extends RowRenderer {

    private static final int MAX_SPARKLINE_WEEKS = 8;
    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    private final GameService gameService;
    private final TradeController tradeController;
    private final Consumer<String> onRemove;
    private final Consumer<WatchlistItem> onNote;

    /**
     * @param gameService     the game service used to read converter and game-over state
     * @param tradeController the controller used to open the buy dialog
     * @param onRemove        callback invoked with the stock symbol when the player clicks ×
     * @param onNote          callback invoked with the item when the player clicks the note button
     */
    WatchlistRowRenderer(GameService gameService,
                         TradeController tradeController,
                         Consumer<String> onRemove,
                         Consumer<WatchlistItem> onNote) {
        this.gameService = gameService;
        this.tradeController = tradeController;
        this.onRemove = onRemove;
        this.onNote = onNote;
    }

    /**
     * Builds the column-keyed cells for the given watchlist item.
     *
     * <p>The note button is inserted first so it serves as the row's default
     * keyboard-focus anchor (see {@link RowCells#firstNode()}); unlike the buy
     * button it is never disabled when the game is over.</p>
     *
     * @param item the watchlist item to render
     * @return the column-keyed cells for this item, keyed by {@link WatchlistSort.SortColumn}
     */
    RowCells<WatchlistSort.SortColumn> buildRow(WatchlistItem item) {
        Stock stock = item.stock();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        Label tickerLabel = TableCells.data(stock.getSymbol());
        Label companyLabel = TableCells.data(stock.getCompany());

        BigDecimal priceNok = converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
        Label priceNokLabel = TableCells.data(ChangeFormatter.formatPlain(priceNok));
        Label currencyLabel = TableCells.data(stock.getCurrency().getCurrencyCode());
        Label priceAltLabel = TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice()));

        BigDecimal changeNok = converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
        Label changeNokLabel = ChangeFormatter.styledAmount(changeNok, "table-cell");
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "table-cell");

        Label highLowLabel = TableCells.data(formatHighLow(stock, HIGH_LOW_WEEKS));

        SparklineChart sparkline = buildSparkline(stock, MAX_SPARKLINE_WEEKS);

        HBox actions = buildActionButtons(item);
        Button noteButton = buildNoteButton(item);
        Button removeButton = buildRemoveButton(stock.getSymbol());

        return RowCells.<WatchlistSort.SortColumn>builder()
                .put(WatchlistSort.SortColumn.NOTE, noteButton)
                .put(WatchlistSort.SortColumn.TICKER, tickerLabel)
                .put(WatchlistSort.SortColumn.COMPANY, companyLabel)
                .put(WatchlistSort.SortColumn.CURRENCY, currencyLabel)
                .put(WatchlistSort.SortColumn.PRICE_ALT, priceAltLabel)
                .put(WatchlistSort.SortColumn.PRICE_NOK, priceNokLabel)
                .put(WatchlistSort.SortColumn.CHANGE_NOK, changeNokLabel)
                .put(WatchlistSort.SortColumn.CHANGE_PCT, changePctLabel)
                .put(WatchlistSort.SortColumn.HIGH_LOW, highLowLabel)
                .put(WatchlistSort.SortColumn.TREND, sparkline)
                .put(WatchlistSort.SortColumn.TRADE, actions)
                .put(WatchlistSort.SortColumn.REMOVE, removeButton);
    }

    private HBox buildActionButtons(WatchlistItem item) {
        boolean gameOver = gameService.isGameOver();
        Button buyButton = new Button(LanguageManager.get("watchlist.buy"));
        buyButton.getStyleClass().addAll("table-action-link", "table-action-buy");
        buyButton.setDisable(gameOver);
        buyButton.setOnAction(e -> tradeController.openBuyDialog(item.stock()));
        HBox box = new HBox(buyButton);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * Builds the note button for the given watchlist item.
     *
     * @param item the watchlist item
     * @return a styled {@link Button} with an icon graphic
     */
    private Button buildNoteButton(WatchlistItem item) {
        int size = 20;
        boolean hasNote = !item.entry().note().isBlank();
        String defaultPath = hasNote ? "/icons/edit-blue.png" : "/icons/edit-default.png";

        Button noteButton = new Button();
        noteButton.setGraphic(loadIcon(defaultPath, size));
        noteButton.getStyleClass().addAll("table-action-link", "watchlist-note-btn");
        noteButton.setOnMouseEntered(e -> noteButton.setGraphic(loadIcon("/icons/edit-blue.png", size)));
        noteButton.setOnMouseExited(e -> noteButton.setGraphic(loadIcon(defaultPath, size)));
        noteButton.setOnAction(e -> onNote.accept(item));
        return noteButton;
    }

    /**
     * Loads an icon from the classpath and returns a sized {@link ImageView}.
     *
     * @param path the classpath resource path (e.g. {@code /icons/edit-blue.png})
     * @param size the desired width and height in pixels
     * @return an {@link ImageView} with preserved aspect ratio
     */
    private ImageView loadIcon(String path, int size) {
        ImageView iv = new ImageView(new Image(
                Objects.requireNonNull(getClass().getResource(path)).toExternalForm()));
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        return iv;
    }

    private Button buildRemoveButton(String symbol) {
        Button removeButton = new Button("×");
        removeButton.getStyleClass().addAll("table-action-link", "watchlist-action-remove");
        removeButton.setOnAction(e -> onRemove.accept(symbol));
        return removeButton;
    }

}
