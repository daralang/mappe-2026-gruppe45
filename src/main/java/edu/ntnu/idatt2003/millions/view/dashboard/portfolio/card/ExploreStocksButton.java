package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;

/**
 * Full-width button shown below the holdings table that navigates the user
 * to the Exchange view to explore stocks beyond their current holdings.
 *
 * <p>Pure presentation: takes a {@link Runnable} callback and invokes it on
 * click. Updates its own label when the application language changes.</p>
 */
public class ExploreStocksButton extends Button {

    public ExploreStocksButton(Runnable onClick) {
        getStyleClass().add("explore-stocks-button");
        setMaxWidth(Double.MAX_VALUE);
        setOnAction(e -> onClick.run());

        Runnable updateLabel = () -> setText(LanguageManager.get("dashboard.exploreStocks") + "  →");
        updateLabel.run();
        LanguageManager.addObserver(updateLabel);
    }
}