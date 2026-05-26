package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.RowRenderer;
import edu.ntnu.idatt2003.millions.view.component.chart.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.function.Consumer;

/**
 * Responsible for building the column-keyed cells for a single {@link WatchlistItem} row.
 *
 * <p>Each call to {@link #buildRow(WatchlistItem)} produces a {@link RowCells} map with
 * ticker, company, price in NOK, weekly change (NOK and %), a sparkline trend, a buy button,
 * a note button with a {@code fth-edit-3} icon, a chevron detail link, and a remove button.
 * The owning card inserts the cells and registers the row for navigation.</p>
 *
 * <p>This class is stateless and may be reused across refreshes.</p>
 */
class WatchlistRowRenderer extends RowRenderer {

    private static final int MAX_SPARKLINE_WEEKS = 8;

    private final GameService gameService;
    private final TradeController tradeController;
    private final Consumer<String> onRemove;
    private final Consumer<WatchlistItem> onNote;

    /**
     * Constructs a new WatchlistRowRenderer.
     *
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
     * @param item the watchlist item to render
     * @return the column-keyed cells for this item, keyed by {@link WatchlistSort.SortColumn}
     */
    RowCells<WatchlistSort.SortColumn> buildRow(WatchlistItem item) {
        Stock stock = item.stock();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        Label tickerLabel = TableCells.data(stock.getSymbol());
        Label companyLabel = TableCells.data(stock.getCompany());

        Label priceNokLabel = priceNokLabel(stock, converter);
        Label changeNokLabel = changeNokLabel(stock, converter);
        Label changePctLabel = changePctLabel(stock);

        SparklineChart sparkline = buildSparkline(stock, MAX_SPARKLINE_WEEKS);

        HBox actions = buildActionButtons(item);
        Button noteButton = buildNoteButton(item);
        Button removeButton = buildRemoveButton(stock.getSymbol());

        return RowCells.<WatchlistSort.SortColumn>builder()
                .put(WatchlistSort.SortColumn.NOTE, noteButton)
                .put(WatchlistSort.SortColumn.TICKER, tickerLabel)
                .put(WatchlistSort.SortColumn.COMPANY, companyLabel)
                .put(WatchlistSort.SortColumn.PRICE_NOK, priceNokLabel)
                .put(WatchlistSort.SortColumn.CHANGE_NOK, changeNokLabel)
                .put(WatchlistSort.SortColumn.CHANGE_PCT, changePctLabel)
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
     * Builds the note button for the given watchlist item using a {@link FontIcon}.
     *
     * @param item the watchlist item
     * @return a styled {@link Button} with a {@code fth-edit-3} icon graphic
     */
    private Button buildNoteButton(WatchlistItem item) {
        boolean hasNote = !item.entry().note().isBlank();
        FontIcon icon = new FontIcon("fth-edit-3");
        icon.getStyleClass().add("watchlist-note-icon");

        Button noteButton = new Button();
        noteButton.setGraphic(icon);
        noteButton.getStyleClass().addAll("table-action-link", "watchlist-note-btn");
        if (hasNote) {
            noteButton.getStyleClass().add("watchlist-note-btn--active");
        }
        noteButton.setOnAction(e -> onNote.accept(item));
        return noteButton;
    }

    private Button buildRemoveButton(String symbol) {
        Button removeButton = new Button("×");
        removeButton.getStyleClass().addAll("table-action-link", "watchlist-action-remove");
        removeButton.setOnAction(e -> onRemove.accept(symbol));
        return removeButton;
    }

}
