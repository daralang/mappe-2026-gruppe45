package edu.ntnu.idatt2003.millions.view.exchange;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import edu.ntnu.idatt2003.millions.view.component.WeekBar;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The dashboard view of the application.
 * Contains a tab bar for navigating between portfolio, transactions,
 * watchlist and loans.
 */
public class ExchangeView extends VBox {

    private final GameManager gameManager;

    /**
     * Constructs a new DashboardView with a tab bar.
     */
    public ExchangeView(GameManager gameManager, WeekBar weekBar) {
        this.gameManager = gameManager;

        getStyleClass().add("content-area");

        ViewHeader viewHeader = new ViewHeader(
                "exchange.title",
                List.of(
                        "exchange.tab.overview",
                        "exchange.tab.stocks",
                        "exchange.tab.analysis"
                ),
                weekBar
        );
        getChildren().add(viewHeader);
    }
}