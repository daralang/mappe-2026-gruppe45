package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.controller.MainController;
import edu.ntnu.idatt2003.millions.controller.StartController;
import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import edu.ntnu.idatt2003.millions.view.dashboard.portfolio.PortfolioView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The exchange view of the application.
 * Contains a tab bar for navigating between overview, stocks and analysis.
 * Tab navigation is purely visual state and handled internally by this view.
 */
public class ExchangeView extends VBox {

    private final GameManager gameManager;
    private final ViewHeader viewHeader;
    private final VBox contentArea;

    /**
     * Constructs a new ExchangeView with the overview tab active.
     *
     * @param gameManager the game manager containing player and exchange
     * @param weekBar     the week bar shared with the rest of the application
     */
    public ExchangeView(GameManager gameManager, WeekBar weekBar) {
        this.gameManager = gameManager;
        getStyleClass().add("content-area");

        viewHeader = new ViewHeader(
                "exchange.title",
                List.of(
                        "exchange.tab.overview",
                        "exchange.tab.stocks",
                        "exchange.tab.analysis"
                ),
                weekBar
        );

        contentArea = new VBox();
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        viewHeader.setTabAction(0, this::showOverview);
        viewHeader.setTabAction(1, this::showStocks);
        viewHeader.setTabAction(2, this::showAnalysis);

        getChildren().addAll(viewHeader, contentArea);
        showOverview();
    }

    /**
     * Activates the stocks tab and shows its content.
     * Called from outside this view to navigate users directly to the
     * stocks tab (e.g. from the "Explore other stocks" button).
     */
    public void selectStocksTab() {
        viewHeader.setActive(viewHeader.getTabButton(1));
        showStocks();
    }

    private void showOverview() {
        contentArea.getChildren().setAll(new ExchangeOverview(gameManager));
    }

    private void showStocks() {
        contentArea.getChildren().clear(); // remove this when implementing setAll
        // contentArea.getChildren().setAll(new StocksView(gameManager));
    }

    private void showAnalysis() {
        contentArea.getChildren().clear(); // remove this when implementing setAll
        // contentArea.getChildren().setAll(new AnalysisView(gameManager));
    }
}