package edu.ntnu.idatt2003.millions.view.dashboard;

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
public class DashboardView extends VBox {

    private final GameManager gameManager;

    /**
     * Constructs a new DashboardView with a tab bar.
     */
    public DashboardView(GameManager gameManager, WeekBar weekBar) {
        this.gameManager = gameManager;
        getStyleClass().add("content-area");

        ViewHeader viewHeader = new ViewHeader(
                "dashboard.title",
                List.of(
                        "dashboard.tab.portfolio",
                        "dashboard.tab.transactions",
                        "dashboard.tab.watchlist",
                        "dashboard.tab.loans"
                ),
                weekBar
        );
        getChildren().add(viewHeader);
    }
}