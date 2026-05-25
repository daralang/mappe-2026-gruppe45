package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.StockHistoryService;
import edu.ntnu.idatt2003.millions.service.StockHistoryService.WeeklyPriceChange;
import edu.ntnu.idatt2003.millions.service.StockStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.PriceHistoryList;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.chart.TimeSeriesChart;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

/**
 * Read-only detail modal for a single {@link Stock}, opened from the market table chevron
 * and reusable from other views.
 *
 * <p>Shows a stat grid of current key figures, a price-history chart beside a scrollable
 * native-price list, an adaptive weekly-change table, and buy/sell actions with a watchlist
 * toggle. Wider than the standard modal via the {@code modal-card-stock-detail} style class.
 * Reads derived values from {@link StockStatsService} and {@link StockHistoryService};
 * holds no business logic itself.</p>
 */
public class StockDetailModal extends Modal {

    private static final double CHART_HEIGHT = 200;

    private final Stock stock;
    private final GameService gameService;
    private final TradeController controller;
    private final StockStatsService statsService = new StockStatsService();
    private final StockHistoryService historyService = new StockHistoryService();

    /**
     * Constructs a {@code StockDetailModal} for the given stock.
     *
     * @param stock       the stock to show details for; must not be {@code null}
     * @param gameService the game service used to read week, watchlist and converter state;
     *                    must not be {@code null}
     * @param controller  the controller used to open the buy and sell dialogs; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public StockDetailModal(Stock stock, GameService gameService, TradeController controller) {
        this.stock = Objects.requireNonNull(stock, "stock must not be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService must not be null");
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
    }

    /**
     * Widens the modal card beyond the standard dialog width via a dedicated style class.
     *
     * @param card the modal card node
     */
    @Override
    protected void configureCard(VBox card) {
        card.getStyleClass().add("modal-card-stock-detail");
    }

    /**
     * Forces a layout and resize pass after the stage is shown so the wider card
     * dimensions are reflected in the stage size.
     */
    @Override
    protected void onBeforeShow() {
        sizeToContent();
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(buildHeader(), buildBody());
        return content;
    }

    /**
     * Builds the header on a single line: the eyebrow, the stock identity «SYMBOL, Company»,
     * an owner badge when the stock is held, and a close button in the top-right corner.
     *
     * @return the header node
     */
    private Region buildHeader() {
        StyledText eyebrow = StyledText.detailLabel(LanguageManager.get("stockDetail.eyebrow"));
        eyebrow.getStyleClass().add("stock-detail-eyebrow");

        Label identity = new Label(stock.getSymbol() + ", " + stock.getCompany());
        identity.getStyleClass().add("modal-title");

        HBox left = new HBox(8, eyebrow, identity);
        left.setAlignment(Pos.BASELINE_LEFT);
        if (isOwned()) {
            Label badge = new Label(LanguageManager.get("exchange.stocks.badge.owner"));
            badge.getStyleClass().add("badge-owner");
            left.getChildren().add(badge);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().add("modal-close");
        close.setOnAction(e -> close());

        HBox header = new HBox(left, spacer, close);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("modal-header");
        return header;
    }

    /**
     * Builds the dialog body: stat grid, chart + price history, weekly-change table and actions.
     *
     * @return the body container
     */
    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(buildStatGrid(), buildChartRow(), buildWeeklyChangeTable(),
                buildActions(), buildFooter());
        return body;
    }

    /**
     * Builds the 4×2 grid of key-figure stat cards (currency, native price, NOK price,
     * weekly change, all-time high/low, trend and current week).
     *
     * @return the stat grid
     */
    private GridPane buildStatGrid() {
        var converter = gameService.getCurrencyConverter();
        Label currency = plainValue(stock.getCurrency().getCurrencyCode());
        Label priceNative = plainValue(ChangeFormatter.formatPlain(stock.getSalesPrice()));
        Label priceNok = plainValue(
                ChangeFormatter.formatPlain(statsService.priceInNok(stock, converter)) + " NOK");
        Label weeklyChange = ChangeFormatter.styledPercent(stock.getWeeklyChangePercent(), "modal-section-value");
        Label high = plainValue(ChangeFormatter.formatPlain(stock.getHighestPrice()));
        Label low = plainValue(ChangeFormatter.formatPlain(stock.getLowestPrice()));
        Label trend = buildTrendValue();
        Label week = plainValue(String.valueOf(gameService.getExchange().getWeek()));

        GridPane grid = new GridPane();
        grid.getStyleClass().add("stock-detail-stats");
        grid.setHgap(12);
        grid.setVgap(12);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(25);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }
        grid.add(statCard(LanguageManager.get("col.currency"), currency), 0, 0);
        grid.add(statCard(LanguageManager.get("col.priceNative"), priceNative), 1, 0);
        grid.add(statCard(LanguageManager.get("col.priceNok"), priceNok), 2, 0);
        grid.add(statCard(LanguageManager.get("details.row.weeklyChange"), weeklyChange), 3, 0);
        grid.add(statCard(LanguageManager.get("stockDetail.high"), high), 0, 1);
        grid.add(statCard(LanguageManager.get("stockDetail.low"), low), 1, 1);
        grid.add(statCard(LanguageManager.get("col.trend"), trend), 2, 1);
        grid.add(statCard(LanguageManager.get("col.week"), week), 3, 1);
        return grid;
    }

    /**
     * Builds the chart-and-history row: the price chart on the left (growing) and a
     * scrollable native-price list on the right.
     *
     * @return the chart row
     */
    private HBox buildChartRow() {
        TimeSeriesChart chart = new TimeSeriesChart(stock.getHistoricalPrices(),
                LanguageManager.get("app.week").toUpperCase());
        chart.setPrefHeight(CHART_HEIGHT);
        VBox chartSection = new VBox(8,
                StyledText.sectionTitle(LanguageManager.get("stockDetail.chartTitle")), chart);
        HBox.setHgrow(chartSection, Priority.ALWAYS);

        PriceHistoryList list = new PriceHistoryList(stock.getHistoricalPrices(), stock.getCurrency());
        list.setMaxHeight(CHART_HEIGHT);
        VBox listSection = new VBox(8,
                StyledText.sectionTitle(LanguageManager.get("stockDetail.historyTitle")), list);
        listSection.setMinWidth(220);

        HBox row = new HBox(16, chartSection, listSection);
        row.getStyleClass().add("stock-detail-chart-row");
        return row;
    }

    /**
     * Builds the adaptive weekly-change table, or an info line when no change has
     * occurred yet (week 1). Rows come from {@link StockHistoryService}, newest first.
     *
     * @return the weekly-change section
     */
    private VBox buildWeeklyChangeTable() {
        List<WeeklyPriceChange> rows =
                historyService.getRecentWeeklyChanges(stock, gameService.getCurrencyConverter());
        StyledText title = StyledText.sectionTitle(LanguageManager.get("stockDetail.weeklyTitle"));
        if (rows.isEmpty()) {
            return new VBox(8, title,
                    StyledText.detailLabel(LanguageManager.get("stockDetail.weeklyEmpty")));
        }
        String code = stock.getCurrency().getCurrencyCode();
        GridPane table = new GridPane();
        table.getStyleClass().add("stock-detail-weekly");
        table.setHgap(12);
        table.setVgap(6);
        HPos[] aligns = {HPos.LEFT, HPos.RIGHT, HPos.RIGHT, HPos.RIGHT};
        for (HPos a : aligns) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setHalignment(a);
            table.getColumnConstraints().add(col);
        }
        table.addRow(0,
                StyledText.detailLabel(LanguageManager.get("col.week")),
                StyledText.detailLabel(MessageFormat.format(LanguageManager.get("stockDetail.changeNative"), code)),
                StyledText.detailLabel(LanguageManager.get("stockDetail.changeNok")),
                StyledText.detailLabel(LanguageManager.get("stockDetail.changePct")));
        int r = 1;
        for (WeeklyPriceChange row : rows) {
            table.addRow(r++,
                    StyledText.detailValue(String.valueOf(row.week())),
                    ChangeFormatter.styledAmount(row.nativeChange(), "detail-value"),
                    ChangeFormatter.styledAmount(row.nokChange(), "detail-value"),
                    ChangeFormatter.styledPercent(row.percentChange(), "detail-value"));
        }
        return new VBox(8, title, table);
    }

    /**
     * Builds one stat card: a small muted label above a bold value.
     *
     * @param labelText the card label
     * @param valueNode the value node
     * @return the stat card
     */
    private VBox statCard(String labelText, Label valueNode) {
        StyledText label = StyledText.detailLabel(labelText);
        label.setWrapText(true);
        VBox card = new VBox(4, label, valueNode);
        card.getStyleClass().add("stock-detail-stat");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /**
     * Builds a bold value label using the shared modal section-value style.
     *
     * @param text the value text
     * @return the value label
     */
    private Label plainValue(String text) {
        Label value = new Label(text);
        value.getStyleClass().add("modal-section-value");
        return value;
    }

    /**
     * Builds the trend value: an up/down arrow with a colour-coded label, derived from
     * the sign of the latest price change.
     *
     * @return the trend value label
     */
    private Label buildTrendValue() {
        BigDecimal change = stock.getLatestPriceChange();
        boolean up = change.signum() >= 0;
        Label value = new Label((up ? "▲ " : "▼ ")
                + LanguageManager.get(up ? "stockDetail.trend.up" : "stockDetail.trend.down"));
        value.getStyleClass().addAll("modal-section-value", up ? "positive" : "negative");
        return value;
    }

    /**
     * Builds the bottom actions: a primary buy button with a watchlist star beside it, and
     * a sell button beneath when the player owns the stock. All disabled when the game is over.
     *
     * @return the actions section
     */
    private VBox buildActions() {
        Button buy = new Button(LanguageManager.get("exchange.stocks.buy"));
        buy.getStyleClass().addAll("modal-button", "modal-button-primary");
        buy.setMaxWidth(Double.MAX_VALUE);
        buy.setDisable(gameService.isGameOver());
        buy.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openBuyDialog(stock));
        });
        HBox.setHgrow(buy, Priority.ALWAYS);

        HBox primaryRow = new HBox(12, buy, buildWatchlistStar());
        primaryRow.setAlignment(Pos.CENTER);

        VBox actions = new VBox(10, primaryRow);
        if (isOwned()) {
            actions.getChildren().add(buildSellButton());
        }
        return actions;
    }

    /**
     * Builds the full-width sell button, shown only when the player owns the stock.
     *
     * @return the sell button
     */
    private Button buildSellButton() {
        Button sell = new Button(LanguageManager.get("details.button.sell"));
        sell.getStyleClass().addAll("modal-button", "modal-button-danger");
        sell.setMaxWidth(Double.MAX_VALUE);
        sell.setDisable(gameService.isGameOver());
        sell.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openSellDialog(ownedShare()));
        });
        return sell;
    }

    /**
     * Builds the watchlist star toggle. Toggling routes through {@link GameService} so the
     * watchlist is the single source of truth and observing views (e.g. the market table)
     * refresh; the star's own glyph is updated locally.
     *
     * @return the star toggle button
     */
    private Button buildWatchlistStar() {
        Button star = new Button();
        star.getStyleClass().add("stock-detail-star");
        updateStar(star);
        star.setOnAction(e -> {
            String symbol = stock.getSymbol();
            if (gameService.getPlayer().isOnWatchlist(symbol)) {
                gameService.removeFromWatchlist(symbol);
            } else {
                gameService.addToWatchlist(symbol);
            }
            updateStar(star);
        });
        return star;
    }

    /**
     * Updates the star glyph and active style to reflect the current watchlist state.
     *
     * @param star the star button to update
     */
    private void updateStar(Button star) {
        boolean watched = gameService.getPlayer().isOnWatchlist(stock.getSymbol());
        star.setText(watched ? "★" : "☆");
        star.getStyleClass().remove("stock-detail-star--active");
        if (watched) {
            star.getStyleClass().add("stock-detail-star--active");
        }
    }

    /**
     * Returns whether the player currently holds any share of this stock.
     *
     * @return {@code true} if the stock is owned
     */
    private boolean isOwned() {
        return !gameService.getPlayer().getPortfolio().getShares(stock.getSymbol()).isEmpty();
    }

    /**
     * Returns the player's held share for this stock. Only call when {@link #isOwned()} is true.
     *
     * @return the owned share
     */
    private Share ownedShare() {
        return gameService.getPlayer().getPortfolio().getShares(stock.getSymbol()).get(0);
    }

    /**
     * Builds the centered footer link that closes the dialog.
     *
     * @return the footer node
     */
    private Region buildFooter() {
        Button link = new Button(LanguageManager.get("dashboard.exploreStocks") + "  →");
        link.getStyleClass().add("modal-button-link");
        link.setOnAction(e -> close());
        HBox footer = new HBox(link);
        footer.setAlignment(Pos.CENTER);
        return footer;
    }
}
