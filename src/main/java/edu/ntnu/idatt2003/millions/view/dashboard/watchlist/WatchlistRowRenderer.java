package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.RowRenderer;
import edu.ntnu.idatt2003.millions.view.component.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.function.Consumer;

/**
 * Responsible for rendering a single {@link WatchlistItem} row into a {@link SortColumnTable}.
 *
 * <p>Each call to {@link #buildRow} populates one row with ticker, company,
 * price in NOK, price in the stock's native currency, weekly change (NOK and %),
 * 4-week high/low, a sparkline trend, the week the entry was added,
 * a buy button, a note button, and a remove button.</p>
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
     * Renders the given item as a row at {@code rowIndex} in the provided table.
     *
     * @param item     the watchlist item to render
     * @param rowIndex the table row index (0 is reserved for the header)
     * @param table    the {@link SortColumnTable} to add the row nodes into
     */
    void buildRow(WatchlistItem item, int rowIndex, SortColumnTable<?> table) {
        Stock stock = item.stock();
        CurrencyConverter converter = gameService.getCurrencyConverter();

        Label tickerLabel = TableCells.data(stock.getSymbol());
        Label companyLabel = TableCells.data(stock.getCompany());

        BigDecimal priceNok = converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
        Label priceNokLabel = TableCells.data(ChangeFormatter.formatPlain(priceNok));

        Label priceAltLabel = TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice()));

        BigDecimal changeNok = converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
        Label changeNokLabel = ChangeFormatter.styledAmount(changeNok, "holdings-cell");
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "holdings-cell");

        Label highLowLabel = TableCells.data(formatHighLow(stock, HIGH_LOW_WEEKS));

        SparklineChart sparkline = buildSparkline(stock, MAX_SPARKLINE_WEEKS);

        Label addedWeekLabel = TableCells.data(String.valueOf(item.entry().addedAtWeek()));

        HBox actions = buildActionButtons(item);
        Button noteButton = buildNoteButton(item);
        Button removeButton = buildRemoveButton(stock.getSymbol());

        GridPane.setValignment(tickerLabel, VPos.TOP);
        GridPane.setValignment(companyLabel, VPos.TOP);
        GridPane.setValignment(priceNokLabel, VPos.TOP);
        GridPane.setValignment(priceAltLabel, VPos.TOP);
        GridPane.setValignment(changeNokLabel, VPos.TOP);
        GridPane.setValignment(changePctLabel, VPos.TOP);
        GridPane.setValignment(highLowLabel, VPos.TOP);
        GridPane.setValignment(sparkline, VPos.TOP);
        GridPane.setValignment(addedWeekLabel, VPos.TOP);
        GridPane.setValignment(actions, VPos.TOP);
        GridPane.setValignment(noteButton, VPos.TOP);
        GridPane.setValignment(removeButton, VPos.TOP);

        Node[] cells = {tickerLabel, companyLabel, priceNokLabel, priceAltLabel,
                changeNokLabel, changePctLabel, highLowLabel, sparkline,
                addedWeekLabel, actions, noteButton, removeButton};
        table.addRow(rowIndex, cells);
    }

    private HBox buildActionButtons(WatchlistItem item) {
        boolean gameOver = gameService.isGameOver();
        Button buyButton = new Button(LanguageManager.get("watchlist.buy"));
        buyButton.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        buyButton.setDisable(gameOver);
        buyButton.setOnAction(e -> tradeController.openBuyDialog(item.stock()));
        return new HBox(buyButton);
    }

    private Button buildNoteButton(WatchlistItem item) {
        boolean hasNote = !item.entry().note().isBlank();
        Button noteButton = new Button(hasNote ? "✎●" : "✎");
        noteButton.getStyleClass().add("holdings-action-link");
        noteButton.setOnAction(e -> onNote.accept(item));
        return noteButton;
    }

    private Button buildRemoveButton(String symbol) {
        Button removeButton = new Button("×");
        removeButton.getStyleClass().addAll("holdings-action-link", "holdings-action-sell");
        removeButton.setOnAction(e -> onRemove.accept(symbol));
        return removeButton;
    }

}
