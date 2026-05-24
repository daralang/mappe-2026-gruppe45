package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.ChevronButton;
import edu.ntnu.idatt2003.millions.view.component.RowRenderer;
import edu.ntnu.idatt2003.millions.view.component.chart.SparklineChart;
import edu.ntnu.idatt2003.millions.view.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static edu.ntnu.idatt2003.millions.view.exchange.stocks.StocksSort.SortColumn.*;

/**
 * Responsible for building the column-keyed cells for a single stock row.
 *
 * <p>Each call to {@link #buildRow(Stock)} produces a {@link RowCells} map with
 * a watchlist star toggle, ticker, company, NOK price, weekly change,
 * a {@link SparklineChart} trend, a {@link ChevronButton} detail link, and trade actions.
 * The owning card inserts the cells and registers the row for navigation.</p>
 *
 * <p>This class is stateless and may be reused across refreshes.
 */
class StocksRowRenderer extends RowRenderer {

    private static final int MAX_SPARKLINE_WEEKS = 8;
    private static final Currency NOK = Currency.getInstance("NOK");

    private final GameService gameService;
    private final TradeController controller;
    private final Consumer<String> onWatchlistToggle;
    private final Consumer<Stock> onDetailClick;

    /**
     * Constructs a new StocksRowRenderer.
     *
     * @param gameService       the game service used to read player portfolio and watchlist state
     * @param controller        the controller used to open buy dialogs
     * @param onWatchlistToggle callback invoked with the stock symbol when the player
     *                          clicks the watchlist star button
     * @param onDetailClick     callback invoked with the {@link Stock} when the player
     *                          clicks the details chevron button
     */
    StocksRowRenderer(GameService gameService,
                      TradeController controller,
                      Consumer<String> onWatchlistToggle,
                      Consumer<Stock> onDetailClick) {
        this.gameService = gameService;
        this.controller = controller;
        this.onWatchlistToggle = onWatchlistToggle;
        this.onDetailClick = onDetailClick;
    }

    /**
     * Builds the column-keyed cells for the given stock.
     * Produces a watchlist star toggle, ticker (with optional owner badge), company name,
     * NOK price, weekly change in NOK and percent, a {@link SparklineChart} trend,
     * a {@link ChevronButton} that fires {@link #onDetailClick}, and trade buttons.
     *
     * <p>The watchlist star is inserted first so it serves as the row's default
     * keyboard-focus anchor (see {@link RowCells#firstNode()}). Row insertion and
     * navigation registration are handled by the owning card's base class.</p>
     *
     * @param stock the stock to render
     * @return the column-keyed cells for this stock, keyed by {@link StocksSort.SortColumn}
     */
    RowCells<StocksSort.SortColumn> buildRow(Stock stock) {
        CurrencyConverter converter = gameService.getCurrencyConverter();

        boolean watched = gameService.getPlayer().isOnWatchlist(stock.getSymbol());
        Button starButton = new Button(watched ? "★" : "☆");
        starButton.getStyleClass().addAll("holdings-action-link", "holdings-action-star");
        if (watched) {
            starButton.getStyleClass().add("holdings-action-star--active");
        }
        starButton.setOnAction(e -> onWatchlistToggle.accept(stock.getSymbol()));

        Label tickerLabel = TableCells.data(stock.getSymbol());
        HBox.setHgrow(tickerLabel, Priority.ALWAYS);
        tickerLabel.setMaxWidth(Double.MAX_VALUE);

        List<Share> ownedShares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        HBox tickerCell = new HBox(4, tickerLabel);
        tickerCell.setAlignment(Pos.BASELINE_LEFT);
        tickerCell.setMaxWidth(Double.MAX_VALUE);
        if (!ownedShares.isEmpty()) {
            Label ownerBadge = new Label(LanguageManager.get("exchange.stocks.badge.owner"));
            ownerBadge.getStyleClass().add("badge-owner");
            tickerCell.getChildren().add(ownerBadge);
        }

        Label companyLabel = TableCells.data(stock.getCompany());
        BigDecimal priceInNok = converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
        Label priceNokLabel = TableCells.data(ChangeFormatter.formatPlain(priceInNok));
        BigDecimal changeInNok = converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
        Label changeKrLabel = ChangeFormatter.styledAmount(changeInNok, "holdings-cell");
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "holdings-cell");
        SparklineChart sparkline = buildSparkline(stock, MAX_SPARKLINE_WEEKS);
        HBox tradeButtons = buildBuyButton(stock);
        ChevronButton detailsButton = new ChevronButton(
                () -> onDetailClick.accept(stock), "tooltip.stocks.chevron");

        Stream.<Node>of(starButton, tickerCell, companyLabel, priceNokLabel, changeKrLabel,
                changePctLabel, sparkline, tradeButtons, detailsButton)
                .forEach(n -> GridPane.setValignment(n, VPos.TOP));

        return RowCells.<StocksSort.SortColumn>builder()
                .put(WATCHLIST, starButton)
                .put(TICKER, tickerCell)
                .put(COMPANY, companyLabel)
                .put(PRICE_NOK, priceNokLabel)
                .put(CHANGE_KR, changeKrLabel)
                .put(CHANGE_PCT, changePctLabel)
                .put(TREND, sparkline)
                .put(TRADE, tradeButtons)
                .put(DETAILS, detailsButton);
    }

    /**
     * Builds the buy and optional sell buttons for the trade column.
     *
     * <p>Always shows a buy button that delegates to {@link TradeController}.
     *
     * @param stock the stock the buttons act on
     * @return an {@link HBox} containing the action buttons
     */
    private HBox buildBuyButton(Stock stock) {
        boolean gameOver = gameService.isGameOver();

        Button buyButton = new Button(LanguageManager.get("exchange.stocks.buy"));
        buyButton.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        buyButton.setDisable(gameOver);
        buyButton.setOnAction(e -> controller.openBuyDialog(stock));

        return new HBox(4, buyButton);
    }
}
