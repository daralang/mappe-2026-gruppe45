package edu.ntnu.idatt2003.millions.view.dialog.stockdetail;

import edu.ntnu.idatt2003.millions.controller.trade.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.PriceHistoryList;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.chart.TimeSeriesChart;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * Read-only detail modal for a single {@link Stock}, opened from chevron
 * and reusable from other views.
 */
public class StockDetailModal extends Modal {

    private static final double CHART_HEIGHT = 200;

    /**
     * Pixels subtracted from the primary screen height to leave room for the
     * modal header, the OS title bar, the taskbar and window chrome.
     */
    private static final double SCREEN_MARGIN = 140;

    private final Stock stock;
    private final GameService gameService;
    private final TradeController controller;
    private Runnable onExplore;

    /**
     * Constructs a {@code StockDetailModal} for the given stock.
     *
     * @param stock       the stock to show details for; must not be {@code null}
     * @param gameService the game service used to read week, watchlist and converter state;
     *                    must not be {@code null}
     * @param controller  the controller used to open the buy and sell dialogs;
     *                    must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public StockDetailModal(Stock stock, GameService gameService, TradeController controller) {
        this.stock = Objects.requireNonNull(stock, "stock must not be null");
        this.gameService = Objects.requireNonNull(gameService, "gameService must not be null");
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
    }

    /**
     * Sets an optional «explore other stocks» action. When set, a full-width button is shown
     * at the bottom of the actions; when unset (e.g. opened from the market itself), it is hidden.
     *
     * @param onExplore the action to run when the explore button is clicked
     */
    public void setOnExplore(Runnable onExplore) {
        this.onExplore = onExplore;
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

    /**
     * Builds the full modal content.
     *
     * @return the root content node
     */
    @Override
    protected Region buildContent() {
        double maxBodyHeight =
                Screen.getPrimary().getVisualBounds().getHeight() - SCREEN_MARGIN;

        ScrollPane bodyScroll = new ScrollPane(buildBody());
        bodyScroll.setFitToWidth(true);
        bodyScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bodyScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        bodyScroll.setMaxHeight(maxBodyHeight);
        bodyScroll.getStyleClass().addAll("modal-body-scroll", "content-scroll");

        VBox content = new VBox();
        content.getChildren().addAll(buildHeader(), bodyScroll);
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
        eyebrow.getStyleClass().add("modal-title");

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
     * Builds the scrollable dialog body: stat grid, chart + price history,
     * weekly-change section and actions.
     *
     * @return the body container
     */
    private VBox buildBody() {
        int currentWeek = gameService.getExchange().getWeek();
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().addAll(
                new StockStatGrid(stock, gameService.getCurrencyConverter(), currentWeek),
                buildChartRow(),
                new WeeklyChangeCard(stock, gameService.getCurrencyConverter(), currentWeek),
                buildActions());
        return body;
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
                StyledText.sectionTitle(MessageFormat.format(
                        LanguageManager.get("stockDetail.chartTitle"),
                        stock.getCurrency().getCurrencyCode())),
                chart);
        chartSection.getStyleClass().add("stock-detail-panel");
        HBox.setHgrow(chartSection, Priority.ALWAYS);

        PriceHistoryList list = new PriceHistoryList(stock.getHistoricalPrices(),
                stock.getCurrency());
        list.setMaxHeight(CHART_HEIGHT);
        VBox listSection = new VBox(8,
                StyledText.sectionTitle(LanguageManager.get("stockDetail.historyTitle")), list);
        listSection.getStyleClass().addAll("stock-detail-panel", "stock-detail-panel--scroll");
        listSection.setMinWidth(220);

        HBox row = new HBox(16, chartSection, listSection);
        row.getStyleClass().add("stock-detail-chart-row");
        return row;
    }

    /**
     * Builds the bottom actions: buy and sell buttons side by side with the watchlist star,
     * and an optional explore button below. When the stock is owned the sell button grows
     * equally alongside the buy button. All trade buttons are disabled when the game is over.
     *
     * @return the actions node
     */
    private Region buildActions() {
        Button buy = buildBuyButton();
        HBox.setHgrow(buy, Priority.ALWAYS);

        Button star = buildWatchlistStar();

        HBox buttonRow;
        if (isOwned()) {
            Button sell = buildSellButton();
            HBox.setHgrow(sell, Priority.ALWAYS);
            buttonRow = new HBox(8, buy, sell, star);
        } else {
            buttonRow = new HBox(8, buy, star);
        }
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        if (onExplore != null) {
            return new VBox(8, buttonRow, buildExploreButton());
        }
        return buttonRow;
    }

    /**
     * Builds the primary buy button.
     *
     * @return the buy button
     */
    private Button buildBuyButton() {
        Button buy = new Button(LanguageManager.get("exchange.stocks.buy"));
        buy.getStyleClass().addAll("modal-button", "modal-button-primary");
        buy.setMaxWidth(Double.MAX_VALUE);
        buy.setDisable(gameService.isGameOver());
        buy.setOnAction(e -> {
            close();
            Platform.runLater(() -> controller.openBuyDialog(stock));
        });
        return buy;
    }

    /**
     * Builds the sell button, shown only when the player owns the stock.
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
     * watchlist is the single source of truth and observing views refresh; the star's own
     * glyph is updated locally.
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
     * Returns the player's held share for this stock. Only call when {@link #isOwned()} is
     * {@code true}.
     *
     * @return the owned share
     */
    private Share ownedShare() {
        return gameService.getPlayer().getPortfolio().getShares(stock.getSymbol()).get(0);
    }

    /**
     * Builds the full-width «explore other stocks» button. Only added when an explore action
     * has been set via {@link #setOnExplore(Runnable)}; runs that action on click.
     *
     * @return the explore button
     */
    private Button buildExploreButton() {
        Button explore = new Button(LanguageManager.get("dashboard.exploreStocks") + "  →");
        explore.getStyleClass().add("explore-stocks-button");
        explore.setMaxWidth(Double.MAX_VALUE);
        explore.setOnAction(e -> onExplore.run());
        return explore;
    }
}
