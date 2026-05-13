package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.controller.PortfolioController;
import edu.ntnu.idatt2003.millions.model.player.Portfolio;
import edu.ntnu.idatt2003.millions.model.stock.Share;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.service.PortfolioService;
import edu.ntnu.idatt2003.millions.util.ChangeFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.TableCells;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.component.card.Card;
import edu.ntnu.idatt2003.millions.view.component.table.SortColumnTable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Card displaying the player's holdings (shares owned), with sortable column
 * headers, search, action buttons for buy / sell / sell all / details, and a
 * total row at the bottom.
 *
 * <p>Column structure, sort state and header rendering are owned by
 * {@link SortColumnTable}. Domain-specific sort logic is delegated to
 * {@link HoldingsSort}. This card is responsible for data fetching, sort
 * orchestration and cell construction only.</p>
 */
public class HoldingsCard extends Card {

    private final GameService gameService;
    private final PortfolioService portfolioService = new PortfolioService();
    private final PortfolioController controller;
    private final HoldingsSort sort;
    private final SortColumnTable<HoldingsSort.SortColumn> table;
    private String currentSearchTerm = "";

    /**
     * Constructs a new HoldingsCard.
     *
     * <p>Column definitions, widths, alignments and tooltip keys are read from
     * {@link HoldingsSort} via a method reference so that
     * {@link SortColumnTable} can resolve fresh i18n labels on every header refresh.</p>
     *
     * @param gameService the game manager containing player and exchange
     * @param controller  the controller handling portfolio actions
     */
    public HoldingsCard(GameService gameService, PortfolioController controller) {
        super(gameService);
        this.gameService = gameService;
        this.controller = controller;
        this.sort = new HoldingsSort(portfolioService, gameService.getCurrencyConverter());
        this.table = new SortColumnTable<>(sort::getColumnDefs);

        StyledText title = StyledText.sectionTitle(LanguageManager.get("dashboard.portfolio.title"));
        setSpacing(16);

        getChildren().addAll(title, createSearchBar(), table.asNode());
        refresh();
    }

    private SearchBar createSearchBar() {
        SearchBar searchBar = new SearchBar(
                "search.placeholder",
                "search.button",
                term -> {
                    currentSearchTerm = term == null ? "" : term;
                    refresh();
                });
        searchBar.setMaxWidth(Double.MAX_VALUE);
        return searchBar;
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
     * Rebuilds the table so column headers and labels are re-resolved.
     */
    @Override
    protected void onLanguageChanged() {
        refresh();
    }

    /**
     * Rebuilds the table contents based on the player's current portfolio.
     * If a sort is active, delegates sorting to {@link HoldingsSort#applySort}
     * before rendering rows.
     */
    private void refresh() {
        table.clearRows();
        table.refreshHeader(this::refresh);

        Portfolio portfolio = gameService.getPlayer().getPortfolio();
        List<Share> shares = new ArrayList<>(portfolio.getShares().stream()
                .filter(this::matchesSearch)
                .toList());

        if (shares.isEmpty()) {
            table.renderEmptyState(LanguageManager.get("dashboard.portfolio.empty"));
            return;
        }

        if (table.isSortActive()) {
            sort.applySort(shares, table.getSortState());
        }

        int row = 1;
        for (Share share : shares) {
            addDataRow(row++, share);
        }

        addTotalRow(row, portfolio);
    }

    private boolean matchesSearch(Share share) {
        if (currentSearchTerm.isBlank()) {
            return true;
        }

        String normalized = currentSearchTerm.toLowerCase();
        Stock stock = share.getStock();
        return stock.getSymbol().toLowerCase().contains(normalized)
                || stock.getCompany().toLowerCase().contains(normalized);
    }

    /**
     * Renders one share as a data row in the table.
     *
     * @param row   the grid row index to write to
     * @param share the share to render
     */
    private void addDataRow(int row, Share share) {
        Stock stock = share.getStock();
        table.addRow(row,
                buildActionButtons(share),
                TableCells.data(stock.getSymbol() + ", " + stock.getCompany()),
                TableCells.data(TableCells.NUMBER_FORMAT.format(share.getQuantity())),
                coloredPercentCell(stock.getWeeklyChangePercent()),
                TableCells.data(TableCells.NUMBER_FORMAT.format(
                        portfolioService.getShareValueInNok(share, gameService.getCurrencyConverter()))),
                coloredPercentCell(share.getReturnPercent()),
                coloredAmountCell(
                        portfolioService.getShareReturnInNok(share, gameService.getCurrencyConverter())),
                buildDetailsButton(share)
        );
    }

    /**
     * Renders the total row below all data rows.
     * A full-width divider separates the data rows from the totals.
     *
     * @param row       the grid row index for the divider
     * @param portfolio the portfolio supplying the total values
     */
    private void addTotalRow(int row, Portfolio portfolio) {
        Region divider = new Region();
        divider.getStyleClass().add("holdings-total-divider");
        table.addFullWidthRow(divider, row);

        int dataRow = row + 1;

        Label totalLabel = new Label(LanguageManager.get("dashboard.portfolio.total"));
        totalLabel.getStyleClass().addAll("holdings-cell", "bold");
        table.addCell(totalLabel, 1, dataRow);

        Label valueNok = new Label(TableCells.NUMBER_FORMAT.format(
                portfolioService.getValue(gameService.getPlayer(), gameService.getCurrencyConverter())));
        valueNok.getStyleClass().addAll("holdings-cell", "bold");
        table.addCell(valueNok, 4, dataRow);

        table.addCell(coloredPercentCell(portfolioService.getTotalReturnPercent(
                gameService.getPlayer(), gameService.getCurrencyConverter())), 5, dataRow);
        table.addCell(coloredAmountCell(portfolioService.getTotalReturnInNok(
                gameService.getPlayer(), gameService.getCurrencyConverter())), 6, dataRow);
    }

    /**
     * Builds the buy, sell and sell-all action buttons for a share row.
     *
     * @param share the share the buttons act on
     * @return an {@link HBox} containing the action buttons
     */
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

    /**
     * Builds the details navigation button for a share row.
     *
     * @param share the share to open details for
     * @return a styled chevron button
     */
    private Button buildDetailsButton(Share share) {
        Button details = new Button("❯");
        details.getStyleClass().add("holdings-details-chevron");
        details.setOnAction(e -> controller.openDetailsModal(share));
        return details;
    }

    /**
     * Creates a styled action button with the given label and colour class.
     *
     * @param text       the button label
     * @param colorClass the CSS class controlling the button colour
     * @return a styled action button
     */
    private Button actionButton(String text, String colorClass) {
        Button b = new Button(text);
        b.getStyleClass().addAll("holdings-action-link", colorClass);
        return b;
    }

    /**
     * Creates a coloured percentage label using {@link ChangeFormatter}.
     *
     * @param value the percentage value to format
     * @return a styled label
     */
    private Label coloredPercentCell(BigDecimal value) {
        return ChangeFormatter.styledPercent(value, "holdings-cell");
    }

    /**
     * Creates a coloured signed amount label using {@link ChangeFormatter}.
     *
     * @param value the amount value to format
     * @return a styled label
     */
    private Label coloredAmountCell(BigDecimal value) {
        return ChangeFormatter.styledAmount(value, "holdings-cell");
    }
}
