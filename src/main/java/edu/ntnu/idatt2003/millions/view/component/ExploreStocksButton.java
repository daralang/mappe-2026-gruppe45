package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;

/**
 * Reusable button that navigates the user to the Exchange view to explore stocks.
 *
 * <p>Pure presentation: takes a {@link Runnable} callback and invokes it on
 * click. Updates its own label when the application language changes.</p>
 *
 * <p>Does not set a maximum width, callers are responsible for sizing.
 * Use {@code setMaxWidth(Double.MAX_VALUE)} where a full-width button is needed.</p>
 */
public class ExploreStocksButton extends Button {

    /**
     * Constructs a new ExploreStocksButton.
     *
     * @param onClick callback invoked when the button is clicked
     */
    public ExploreStocksButton(Runnable onClick) {
        getStyleClass().add("explore-stocks-button");
        setOnAction(e -> onClick.run());

        Runnable updateLabel = () -> setText(LanguageManager.get("dashboard.exploreStocks") + "  →");
        updateLabel.run();
        LanguageManager.addObserver(updateLabel);
    }
}
