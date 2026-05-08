package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.model.player.Player;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class HoldingsCard extends VBox {

    private static final DecimalFormat NUMBER_FORMAT;
    private static final DecimalFormat PERCENT_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("nb-NO"));
        NUMBER_FORMAT = new DecimalFormat("#,##0.00", symbols);
        PERCENT_FORMAT = new DecimalFormat("+#,##0.0;-#,##0.0", symbols);
    }

    private final GameManager gameManager;
    private final GridPane grid = new GridPane();

    /**
     * Constructs a new HoldingsCard.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public HoldingsCard(GameManager gameManager) {
        this.gameManager = gameManager;
        getStyleClass().add("card");

        Label title = new Label("Beholdning");
        title.getStyleClass().add("holdings-title");

        grid.setHgap(20);
        configureColumns();

        getChildren().addAll(title, grid);

        // TODO: hook into the same observer mechanism the other cards use.
        // For now we just render once. When the model becomes observable,
        // call refresh() on change.
        refresh();
    }

    private void configureColumns() {
        // 0: actions    -> 18% (Kjøp / Selg / Selg alt)
        // 1: company    -> 22%
        // 2: quantity   -> 11%
        // 3: weekly %   -> 12%
        // 4: value NOK  -> 12%
        // 5: return %   -> 11%
        // 6: return NOK -> 11%
        // 7: details    -> 3%  (chevron icon)
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
     * Rebuilds the table contents based on the player's current portfolio.
     */
    public void refresh() {
        grid.getChildren().clear();

        Player player = gameManager.getPlayer();
        Portfolio portfolio = player.getPortfolio();
        List<Share> shares = portfolio.getShares();

        addHeaderRow();

        if (shares.isEmpty()) {
            Label empty = new Label("Du eier ingen andeler ennå");
            empty.getStyleClass().add("holdings-empty");
            GridPane.setColumnSpan(empty, 8);
            GridPane.setHalignment(empty, HPos.CENTER);
            grid.add(empty, 0, 1);
            return;
        }

        BigDecimal totalValueNok = BigDecimal.ZERO;
        BigDecimal totalReturnNok = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        int row = 1;
        for (Share share : shares) {
            Stock stock = share.getStock();
            BigDecimal quantity = share.getQuantity();
            BigDecimal purchasePrice = share.getPurchasePrice();
            BigDecimal salesPrice = stock.getSalesPrice();

            BigDecimal cost = purchasePrice.multiply(quantity);
            BigDecimal currentValue = salesPrice.multiply(quantity);
            BigDecimal returnNok = currentValue.subtract(cost);
            BigDecimal returnPercent = cost.signum() == 0
                    ? BigDecimal.ZERO
                    : returnNok.multiply(BigDecimal.valueOf(100))
                    .divide(cost, 2, RoundingMode.HALF_UP);

            BigDecimal weeklyChange = stock.getLatestPriceChange();
            BigDecimal previousPrice = salesPrice.subtract(weeklyChange);
            BigDecimal weeklyPercent = previousPrice.signum() == 0
                    ? BigDecimal.ZERO
                    : weeklyChange.multiply(BigDecimal.valueOf(100))
                    .divide(previousPrice, 2, RoundingMode.HALF_UP);

            addDataRow(row++, share, stock, weeklyPercent, currentValue,
                    returnPercent, returnNok);

            totalCost = totalCost.add(cost);
            totalValueNok = totalValueNok.add(currentValue);
            totalReturnNok = totalReturnNok.add(returnNok);
        }

        BigDecimal totalReturnPercent = totalCost.signum() == 0
                ? BigDecimal.ZERO
                : totalReturnNok.multiply(BigDecimal.valueOf(100))
                .divide(totalCost, 2, RoundingMode.HALF_UP);

        addTotalRow(row, totalValueNok, totalReturnPercent, totalReturnNok);
    }

    private void addHeaderRow() {
        String[] headers = {
                "", "Selskap", "Antall", "Denne uken %",
                "Verdi NOK", "Avkast. %", "Avkast. NOK", ""
        };
        for (int i = 0; i < headers.length; i++) {
            Label label = new Label(headers[i]);
            label.getStyleClass().add("holdings-header");
            grid.add(label, i, 0);
        }
    }

    private void addDataRow(int row, Share share, Stock stock,
                            BigDecimal weeklyPercent, BigDecimal currentValue,
                            BigDecimal returnPercent, BigDecimal returnNok) {

        grid.add(buildActionButtons(share), 0, row);
        grid.add(cell(stock.getSymbol() + ", " + stock.getCompany()), 1, row);
        grid.add(cell(NUMBER_FORMAT.format(share.getQuantity())), 2, row);
        grid.add(coloredPercentCell(weeklyPercent), 3, row);
        grid.add(cell(NUMBER_FORMAT.format(currentValue)), 4, row);
        grid.add(coloredPercentCell(returnPercent), 5, row);
        grid.add(coloredAmountCell(returnNok), 6, row);
        grid.add(buildDetailsButton(share), 7, row);
    }

    private void addTotalRow(int row, BigDecimal totalValueNok,
                             BigDecimal totalReturnPercent, BigDecimal totalReturnNok) {

        // Divider line spanning all columns
        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        GridPane.setColumnSpan(divider, 8);
        grid.add(divider, 0, row);

        int dataRow = row + 1;

        Label totalLabel = new Label("Totalt");
        totalLabel.getStyleClass().add("holdings-total-label");
        grid.add(totalLabel, 1, dataRow);

        Label valueNok = new Label(NUMBER_FORMAT.format(totalValueNok));
        valueNok.getStyleClass().add("holdings-total-label");
        grid.add(valueNok, 4, dataRow);

        grid.add(coloredPercentCell(totalReturnPercent), 5, dataRow);
        grid.add(coloredAmountCell(totalReturnNok), 6, dataRow);
    }

    private HBox buildActionButtons(Share share) {
        Button buy = actionButton("Kjøp", "holdings-action-buy");
        Button sell = actionButton("Selg", "holdings-action-sell");
        Button sellAll = actionButton("Selg alt", "holdings-action-sell");

        buy.setOnAction(e -> {
            // TODO: open Kjøp-popup for share.getStock()
            System.out.println("Kjøp clicked for " + share.getStock().getSymbol());
        });
        sell.setOnAction(e -> {
            // TODO: open Selg-popup for share
            System.out.println("Selg clicked for " + share.getStock().getSymbol());
        });
        sellAll.setOnAction(e -> {
            // TODO: open Selg alt-popup for share.getStock()
            System.out.println("Selg alt clicked for " + share.getStock().getSymbol());
        });

        HBox primaryActions = new HBox(8, buy, sell);
        primaryActions.setAlignment(Pos.CENTER_LEFT);

        HBox box = new HBox(16, primaryActions, sellAll);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button buildDetailsButton(Share share) {
        Button details = new Button("\u276F");
        details.getStyleClass().add("holdings-details-chevron");
        details.setOnAction(e -> {
            // TODO: open Detaljer-popup ...
            System.out.println("Detaljer clicked for " + share.getStock().getSymbol());
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
        Label label = new Label(PERCENT_FORMAT.format(value) + "%");
        label.getStyleClass().add(value.signum() < 0 ? "holdings-negative" : "holdings-positive");
        return label;
    }

    private Label coloredAmountCell(BigDecimal value) {
        String formatted = (value.signum() >= 0 ? "+" : "") + NUMBER_FORMAT.format(value);
        Label label = new Label(formatted);
        label.getStyleClass().add(value.signum() < 0 ? "holdings-negative" : "holdings-positive");
        return label;
    }
}