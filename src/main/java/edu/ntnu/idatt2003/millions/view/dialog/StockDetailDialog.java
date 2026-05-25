package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.StockHistoryService;
import edu.ntnu.idatt2003.millions.service.StockStatsService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
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
import java.util.Objects;

/**
 * Read-only detail dialog for a single {@link Stock}, opened from the market table chevron.
 */
public class StockDetailDialog extends Modal {

    private final Stock stock;
    private final GameService gameService;
    private final TradeController controller;
    private final StockStatsService statsService = new StockStatsService();
    private final StockHistoryService historyService = new StockHistoryService();

    /**
     * Constructs a {@code StockDetailDialog} for the given stock.
     *
     * @param stock       the stock to show details for; must not be {@code null}
     * @param gameService the game service used to read week, watchlist and converter state;
     *                    must not be {@code null}
     * @param controller  the controller used to open the buy dialog; must not be {@code null}
     * @throws NullPointerException if any argument is {@code null}
     */
    public StockDetailDialog(Stock stock, GameService gameService, TradeController controller) {
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
     * Builds the header: a small eyebrow line, the company name with the ticker as a
     * muted subtitle, and a close button in the top-right corner.
     *
     * @return the header node
     */
    private Region buildHeader() {
        StyledText eyebrow = StyledText.detailLabel(LanguageManager.get("stockDetail.eyebrow"));
        eyebrow.getStyleClass().add("stock-detail-eyebrow");

        Label company = new Label(stock.getCompany());
        company.getStyleClass().add("modal-title");
        StyledText ticker = StyledText.detailLabel("· " + stock.getSymbol());

        HBox titleLine = new HBox(8, company, ticker);
        titleLine.setAlignment(Pos.BASELINE_LEFT);

        VBox titleBlock = new VBox(2, eyebrow, titleLine);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button close = new Button("✕");
        close.getStyleClass().add("modal-close");
        close.setOnAction(e -> close());

        HBox header = new HBox(titleBlock, spacer, close);
        header.getStyleClass().add("modal-header");
        return header;
    }

    /**
     * Builds the dialog body. Sections (stat grid, chart + price history, weekly-change
     * table, actions) are added in subsequent steps.
     *
     * @return the body container
     */
    private VBox buildBody() {
        VBox body = new VBox();
        body.getStyleClass().add("modal-body");
        body.getChildren().add(buildStatGrid());
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
     * Builds one stat card: a small muted label above a bold value.
     *
     * @param labelText the card label
     * @param valueNode the value node
     * @return the stat card
     */
    private VBox statCard(String labelText, Label valueNode) {
        VBox card = new VBox(4, StyledText.detailLabel(labelText), valueNode);
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
}
