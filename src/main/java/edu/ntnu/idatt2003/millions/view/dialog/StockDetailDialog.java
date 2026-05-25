package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.controller.TradeController;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.StockHistoryService;
import edu.ntnu.idatt2003.millions.service.StockStatsService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        return body;
    }
}
