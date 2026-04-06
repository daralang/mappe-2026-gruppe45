package edu.ntnu.idatt2003.millions.view.dashboard;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.ViewHeader;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * The dashboard view of the application.
 * Contains a tab bar for navigating between portfolio, transactions,
 * watchlist and loans.
 */
public class DashboardView extends VBox {

    private final ViewHeader viewHeader;

    /**
     * Constructs a new DashboardView with a tab bar.
     */
    public DashboardView() {
        getStyleClass().add("content-area");

        viewHeader = new ViewHeader(List.of(
                LanguageManager.get("dashboard.tab.portfolio"),
                LanguageManager.get("dashboard.tab.transactions"),
                LanguageManager.get("dashboard.tab.watchlist"),
                LanguageManager.get("dashboard.tab.loans")
        ));

        getChildren().add(viewHeader);
    }
}