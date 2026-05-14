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
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.List;

/**
 * Responsible for rendering a single stock row into a {@link SortColumnTable}.
 *
 * <p>Each call to {@link #buildRow(Stock, int, SortColumnTable)} populates one row
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

    /**
     * Constructs a new StocksRowRenderer.
     *
     * @param gameService the game service used to read player portfolio state
     * @param controller  the controller used to open buy/sell dialogs
     */
    StocksRowRenderer(GameService gameService, PortfolioController controller) {
        this.gameService = gameService;
        this.controller = controller;
    }

    /**
     * Renders the given stock as a row at {@code rowIndex} in the provided table.
     * Adds the ticker cell (with optional owner badge), company name, current price,
     * weekly change in NOK and percent, 4-week high/low, a sparkline trend,
     * and trade buttons.
     *
     * @param stock    the stock to render
     * @param rowIndex the table row index (0 is reserved for the header)
     * @param table    the {@link SortColumnTable} to add the row nodes into
     */
    void buildRow(Stock stock, int rowIndex, SortColumnTable<?> table) {
        CurrencyConverter converter = gameService.getCurrencyConverter();

        Label tickerLabel = new Label(stock.getSymbol());
        tickerLabel.getStyleClass().add("holdings-cell");

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

        HBox tradeButtons = buildBuyButton(stock);

        GridPane.setValignment(tickerCell, VPos.TOP);
        GridPane.setValignment(companyLabel, VPos.TOP);
        GridPane.setValignment(priceLabel, VPos.TOP);
        GridPane.setValignment(priceNokLabel, VPos.TOP);
        GridPane.setValignment(changeKrLabel, VPos.TOP);
        GridPane.setValignment(changePctLabel, VPos.TOP);
        GridPane.setValignment(highLowLabel, VPos.TOP);
        GridPane.setValignment(sparkline, VPos.TOP);
        GridPane.setValignment(tradeButtons, VPos.TOP);

        table.addRow(rowIndex, tickerCell, companyLabel, priceLabel, priceNokLabel,
                changeKrLabel, changePctLabel, highLowLabel, sparkline, tradeButtons);
    }

    /**
     * Formats the lowest and highest prices from the latest
     * {@value #HIGH_LOW_WEEKS} historical price entries.
     *
     * <p>Returns {@code "-"} when the stock has no historical price data,
     * avoiding a {@link java.util.NoSuchElementException} on an empty stream.
     *
     * @param stock the stock to read prices from
     * @return a formatted {@code "low / high"} string, or {@code "—"} if
     *         insufficient price history is available
     */
    private String formatHighLow(Stock stock) {
        List<BigDecimal> prices = lastPrices(stock, HIGH_LOW_WEEKS);
        if (prices.isEmpty()) {
            return "-";
        }
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
