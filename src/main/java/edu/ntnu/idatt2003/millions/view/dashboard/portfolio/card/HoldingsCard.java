package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Card displaying the player's holdings (shares owned), with action buttons
 * for buy / sell / sell all / details, and a total row at the bottom.
 *
 * <p>Currency-related columns ("Valuta" and "Verdi") are intentionally
 * omitted from the main table and will be shown in the details popup
 * instead. This keeps the main table clean while Dara's currency PR is
 * pending.</p>
 */
public class HoldingsCard extends Card {

    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private final GameService gameService;
    private final PortfolioService portfolioService = new PortfolioService();
    private final PortfolioController controller;
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new HoldingsCard.
     *
     * @param gameService the game manager containing player and exchange
     * @param controller the controller handling portfolio actions
     */
    public HoldingsCard(GameService gameService, PortfolioController controller) {
        super(gameService);
        this.gameService = gameService;
        this.controller = controller;

        StyledText title = StyledText.sectionTitle(LanguageManager.get("dashboard.portfolio.title"));
        setSpacing(16);

        grid.setHgap(20);
        configureColumns();

        getChildren().addAll(title, grid);
        refresh();
    }

    private void configureColumns() {
        double[] widths = {18, 22, 10, 12, 12, 10, 11, 5};
        HPos[] alignments = {
                HPos.LEFT, HPos.LEFT, HPos.RIGHT, HPos.RIGHT,
                HPos.RIGHT, HPos.RIGHT, HPos.RIGHT, HPos.CENTER
        };
        for (int i = 0; i < widths.length; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHalignment(alignments[i]);
            col.setPercentWidth(widths[i]);
            grid.getColumnConstraints().add(col);
        }
    }

    /**
     * Called when the game state changes (week advanced, buy or sell).
     * Rebuilds the holdings table to reflect the current portfolio.
     */
    @Override
    public void onGameUpdated() {
        refresh();
    }

    /**
     * Called when the application language changes.
     * Rebuilds the table so any future i18n keys are picked up.
     */
    @Override
    protected void onLanguageChanged() {
        refresh();
    }

    /**
     * Rebuilds the table contents based on the player's current portfolio.
     */
    private void refresh() {
        grid.getChildren().clear();

        Portfolio portfolio = gameService.getPlayer().getPortfolio();
        List<Share> shares = portfolio.getShares();

        addHeaderRow();

        if (shares.isEmpty()) {
            StyledText empty = StyledText.widgetLabel(LanguageManager.get("dashboard.portfolio.empty"));
            empty.getStyleClass().add("holdings-empty");
            GridPane.setColumnSpan(empty, 8);
            GridPane.setHalignment(empty, HPos.CENTER);
            grid.add(empty, 0, 1);
            return;
        }

        int row = 1;
        for (Share share : shares) {
            addDataRow(row++, share);
        }

        addTotalRow(row, portfolio);
    }

    private void addHeaderRow() {
        String[] headers = {
                "",
                LanguageManager.get("dashboard.portfolio.company"),
                LanguageManager.get("dashboard.portfolio.quantity"),
                LanguageManager.get("dashboard.portfolio.weeklyChange"),
                LanguageManager.get("dashboard.portfolio.valueNok"),
                LanguageManager.get("dashboard.portfolio.returnPct"),
                LanguageManager.get("dashboard.portfolio.returnNok"),
                ""
        };
        for (int i = 0; i < headers.length; i++) {
            Label label = new Label(headers[i]);
            label.getStyleClass().add("holdings-header");
            grid.add(label, i, 0);
        }
    }

    private void addDataRow(int row, Share share) {
        Stock stock = share.getStock();
        grid.add(buildActionButtons(share), 0, row);
        grid.add(cell(stock.getSymbol() + ", " + stock.getCompany()), 1, row);
        grid.add(cell(NUMBER_FORMAT.format(share.getQuantity())), 2, row);
        grid.add(coloredPercentCell(stock.getWeeklyChangePercent()), 3, row);
        grid.add(cell(NUMBER_FORMAT.format(portfolioService.getShareValueInNok(share, gameService.getCurrencyConverter()))), 4, row);
        grid.add(coloredPercentCell(share.getReturnPercent()), 5, row);
        grid.add(coloredAmountCell(portfolioService.getShareReturnInNok(share, gameService.getCurrencyConverter())), 6, row);
        grid.add(buildDetailsButton(share), 7, row);
    }

    private void addTotalRow(int row, Portfolio portfolio) {
        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, 8);
        grid.add(divider, 0, row);

        int dataRow = row + 1;

        Label totalLabel = new Label(LanguageManager.get("dashboard.portfolio.total"));
        totalLabel.getStyleClass().addAll("holdings-cell", "bold");
        grid.add(totalLabel, 1, dataRow);

        Label valueNok = new Label(NUMBER_FORMAT.format(portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
        valueNok.getStyleClass().addAll("holdings-cell", "bold");
        grid.add(valueNok, 4, dataRow);

        grid.add(coloredPercentCell(portfolioService.getTotalReturnPercent(gameService.getPlayer(), gameService.getCurrencyConverter())), 5, dataRow);
        grid.add(coloredAmountCell(portfolioService.getTotalReturnInNok(gameService.getPlayer(), gameService.getCurrencyConverter())), 6, dataRow);
    }

    private HBox buildActionButtons(Share share) {
        Button buy = actionButton(LanguageManager.get("dashboard.portfolio.buy"), "holdings-action-buy");
        Button sell = actionButton(LanguageManager.get("dashboard.portfolio.sell"), "holdings-action-sell");
        Button sellAll = actionButton(LanguageManager.get("dashboard.portfolio.sellAll"), "holdings-action-sell");

        buy.setOnAction(e -> controller.openBuyDialog(share.getStock()));
        sell.setOnAction(e -> controller.openSellDialog(share));
        sellAll.setOnAction(e -> controller.openSellAllDialog(share));

        HBox primaryActions = new HBox(8, buy, sell);
        primaryActions.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox(16, primaryActions, sellAll);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button buildDetailsButton(Share share) {
        Button details = new Button("❯");
        details.getStyleClass().add("holdings-details-chevron");
        details.setOnAction(e -> {
            // TODO: open Detaljer-popup with detailed share info
        });
        return details;
    }

    private Button actionButton(String text, String colorClass) {
        Button b = new Button(text);
        b.getStyleClass().addAll("holdings-action-link", colorClass);
        return b;
    }

    private Label cell(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("holdings-cell");
        return label;
    }

    private Label coloredPercentCell(BigDecimal value) {
        return ChangeFormatter.styledPercent(value, "holdings-cell");
    }

    private Label coloredAmountCell(BigDecimal value) {
        return ChangeFormatter.styledAmount(value, "holdings-cell");
    }
}
