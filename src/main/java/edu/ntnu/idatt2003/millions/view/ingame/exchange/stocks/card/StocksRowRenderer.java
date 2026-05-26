package edu.ntnu.idatt2003.millions.view.ingame.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.util.format.TableCells;
import edu.ntnu.idatt2003.millions.view.ingame.component.RowRenderer;
import edu.ntnu.idatt2003.millions.view.ingame.component.chart.SparklineChart;
import edu.ntnu.idatt2003.millions.view.ingame.component.table.RowCells;
import edu.ntnu.idatt2003.millions.view.ingame.exchange.stocks.StocksSort;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.List;

import static edu.ntnu.idatt2003.millions.view.ingame.exchange.stocks.StocksSort.SortColumn.*;

/**
 * Responsible for building the column-keyed cells for a single stock row.
 *
 * <p>Each call to {@link #buildRow(Stock)} produces a {@link RowCells} map with
 * a watchlist star toggle, ticker, company, NOK price, weekly change,
 * sparkline chart trend, a chevron button detail link, and trade actions.
 * The owning card inserts the cells and registers the row for navigation.</p>
 *
 * <p>This class is stateless and may be reused across refreshes.
 */
class StocksRowRenderer extends RowRenderer {

    private static final int MAX_SPARKLINE_WEEKS = 8;

    private final GameService gameService;
    private final TradeController controller;
    private final Consumer<String> onWatchlistToggle;

    /**
     * Constructs a new StocksRowRenderer.
     *
     * @param gameService       the game service used to read player portfolio and watchlist state
     * @param controller        the controller used to open buy dialogs
     * @param onWatchlistToggle callback invoked with the stock symbol when the player
     *                          clicks the watchlist star button
     */
    StocksRowRenderer(GameService gameService,
                      TradeController controller,
                      Consumer<String> onWatchlistToggle) {
        this.gameService = gameService;
        this.controller = controller;
        this.onWatchlistToggle = onWatchlistToggle;
    }

    /**
     * Builds the column-keyed cells for the given stock.
     * Produces a watchlist star toggle, ticker (with optional owner badge), company name,
     * NOK price, weekly change in NOK and percent, a {@link SparklineChart} trend, trade buttons
     * and chevron.
     *
     * @param stock the stock to render
     * @return the column-keyed cells for this stock, keyed by {@link StocksSort.SortColumn}
     */
    RowCells<StocksSort.SortColumn> buildRow(Stock stock) {
        CurrencyConverter converter = gameService.getCurrencyConverter();

        boolean watched = gameService.getPlayer().isOnWatchlist(stock.getSymbol());
        Button starButton = new Button(watched ? "★" : "☆");
        starButton.getStyleClass().addAll("table-action-link", "table-action-star");
        if (watched) {
            starButton.getStyleClass().add("table-action-star--active");
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
        Label priceNokLabel = priceNokLabel(stock, converter);
        Label changeKrLabel = changeNokLabel(stock, converter);
        Label changePctLabel = changePctLabel(stock);
        SparklineChart sparkline = buildSparkline(stock, MAX_SPARKLINE_WEEKS);
        Button tradeButton = buildBuyButton(stock);

        return RowCells.<StocksSort.SortColumn>builder()
                .put(WATCHLIST, starButton)
                .put(TICKER, tickerCell)
                .put(COMPANY, companyLabel)
                .put(PRICE_NOK, priceNokLabel)
                .put(CHANGE_KR, changeKrLabel)
                .put(CHANGE_PCT, changePctLabel)
                .put(TREND, sparkline)
                .put(TRADE, tradeButton);
    }

    /**
     * Builds the buy button for the trade column, delegating to {@link TradeController}.
     *
     * <p>Returned as a bare {@link Button} so the table's column alignment centres it
     * directly; wrap it in a container only if more than one control is ever needed.</p>
     *
     * @param stock the stock the button acts on
     * @return the configured buy {@link Button}
     */
    private Button buildBuyButton(Stock stock) {
        Button buyButton = new Button(LanguageManager.get("exchange.stocks.buy"));
        buyButton.getStyleClass().addAll("table-action-link", "table-action-buy");
        buyButton.setDisable(gameService.isGameOver());
        buyButton.setOnAction(e -> controller.openBuyDialog(stock));
        return buyButton;
    }
}
