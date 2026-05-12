package edu.ntnu.idatt2003.millions.view.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.SparklineChart;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

/**
 * Responsible for rendering a single stock row into a {@link GridPane}.
 *
 * <p>Each call to {@link #buildRow(Stock, int, GridPane)} populates one row
 * with ticker, company, prices, weekly change, 4-week high/low,
 * a {@link SparklineChart} trend and trade actions.
 *
 * <p>This class is stateless and may be reused across refreshes.
 */
class StocksRowRenderer {

    private static final int MAX_SPARKLINE_WEEKS = 8;
    private static final int HIGH_LOW_WEEKS = 4;
    private static final Currency NOK = Currency.getInstance("NOK");

    private final GameService gameService;
    private final PortfolioController controller;
    private final CurrencyConverter converter;

    /**
     * Constructs a new StocksRowRenderer.
     *
     * @param gameService the game service used to read player portfolio state
     * @param controller  the controller used to open buy/sell dialogs
     */
    StocksRowRenderer(GameService gameService, PortfolioController controller) {
        this.gameService = gameService;
        this.controller = controller;
        this.converter = gameService.getCurrencyConverter();
    }

    /**
     * Renders the given stock as a row at {@code rowIndex} in the provided grid.
     * Adds the ticker cell (with optional owner badge), company name, current price,
     * weekly change in NOK and percent, 4-week high/low, a sparkline trend,
     * and trade buttons.
     *
     * @param stock    the stock to render
     * @param rowIndex the grid row index (0 is reserved for the header)
     * @param grid     the {@link GridPane} to add the row nodes into
     */
    void buildRow(Stock stock, int rowIndex, GridPane grid) {
        Label tickerLabel = new Label(stock.getSymbol());
        tickerLabel.getStyleClass().add("holdings-cell");

        List<Share> ownedShares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        HBox tickerCell = new HBox(4, tickerLabel);
        tickerCell.setAlignment(Pos.CENTER_LEFT);
        if (!ownedShares.isEmpty()) {
            Label ownerBadge = new Label(LanguageManager.get("exchange.stocks.badge.owner"));
            ownerBadge.getStyleClass().add("owner-cell");
            tickerCell.getChildren().add(ownerBadge);
        }

        Label companyLabel = TableCells.data(stock.getCompany());
        Label priceLabel = TableCells.data(ChangeFormatter.formatPlain(stock.getSalesPrice()));
        BigDecimal priceInNok = converter.convert(stock.getSalesPrice(), stock.getCurrency(), NOK);
        Label priceNokLabel = TableCells.data(ChangeFormatter.formatPlain(priceInNok));
        BigDecimal changeInNok = converter.convert(stock.getLatestPriceChange(), stock.getCurrency(), NOK);
        Label changeKrLabel = ChangeFormatter.styledAmount(changeInNok, "holdings-cell");
        Label changePctLabel = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "holdings-cell");
        Label highLowLabel = TableCells.data(formatHighLow(stock));

        List<BigDecimal> prices = stock.getHistoricalPrices();
        List<BigDecimal> sparkPrices = prices.subList(
                Math.max(0, prices.size() - MAX_SPARKLINE_WEEKS), prices.size());
        SparklineChart sparkline = new SparklineChart();
        sparkline.update(sparkPrices);

        HBox tradeButtons = buildTradeButtons(stock);

        grid.add(tickerCell, 0, rowIndex);
        grid.add(companyLabel, 1, rowIndex);
        grid.add(priceLabel, 2, rowIndex);
        grid.add(priceNokLabel, 3, rowIndex);
        grid.add(changeKrLabel, 4, rowIndex);
        grid.add(changePctLabel, 5, rowIndex);
        grid.add(highLowLabel, 6, rowIndex);
        grid.add(sparkline, 7, rowIndex);
        grid.add(tradeButtons, 8, rowIndex);

        GridPane.setValignment(tickerCell, VPos.TOP);
        GridPane.setValignment(companyLabel, VPos.TOP);
        GridPane.setValignment(priceLabel, VPos.TOP);
        GridPane.setValignment(priceNokLabel, VPos.TOP);
        GridPane.setValignment(changeKrLabel, VPos.TOP);
        GridPane.setValignment(changePctLabel, VPos.TOP);
        GridPane.setValignment(highLowLabel, VPos.TOP);
        GridPane.setValignment(sparkline, VPos.TOP);
        GridPane.setValignment(tradeButtons, VPos.TOP);
    }

    /**
     * Formats the lowest and highest prices from the latest price entries.
     *
     * @param stock the stock to read prices from
     * @return formatted low and high values
     */
    private String formatHighLow(Stock stock) {
        List<BigDecimal> prices = lastPrices(stock, HIGH_LOW_WEEKS);
        BigDecimal low = prices.stream().min(BigDecimal::compareTo).orElseThrow();
        BigDecimal high = prices.stream().max(BigDecimal::compareTo).orElseThrow();
        return formatWhole(low) + " / " + formatWhole(high);
    }

    private String formatWhole(BigDecimal value) {
        return value.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * Returns the latest prices up to the given limit.
     *
     * @param stock the stock to read prices from
     * @param limit the maximum number of prices to include
     * @return the latest prices
     */
    private List<BigDecimal> lastPrices(Stock stock, int limit) {
        List<BigDecimal> prices = stock.getHistoricalPrices();
        return prices.subList(Math.max(0, prices.size() - limit), prices.size());
    }

    /**
     * Builds the buy and optional sell buttons for the trade column.
     *
     * <p>Always shows a buy button that delegates to {@link PortfolioController}.
     * Shows a sell button when the player holds at least one {@link Share} of the stock,
     * delegating to {@link PortfolioController} with the first owned position.
     *
     * @param stock the stock the buttons act on
     * @return an {@link HBox} containing the action buttons
     */
    private HBox buildTradeButtons(Stock stock) {
        Button buyButton = new Button(LanguageManager.get("exchange.stocks.buy"));
        buyButton.getStyleClass().addAll("holdings-action-link", "holdings-action-buy");
        buyButton.setOnAction(e -> controller.openBuyDialog(stock));

        HBox buttons = new HBox(4, buyButton);

        List<Share> shares = gameService.getPlayer().getPortfolio().getShares(stock.getSymbol());
        if (!shares.isEmpty()) {
            Share share = shares.getFirst();
            Button sellButton = new Button(LanguageManager.get("exchange.stocks.sell"));
            sellButton.getStyleClass().addAll("holdings-action-link", "holdings-action-sell");
            sellButton.setOnAction(e -> controller.openSellDialog(share));
            buttons.getChildren().add(sellButton);
        }

        return buttons;
    }
}
