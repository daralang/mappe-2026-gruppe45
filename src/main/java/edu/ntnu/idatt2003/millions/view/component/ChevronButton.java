package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A styled navigation button displaying a chevron ({@code ❯}), used in table rows
 * to open a detail view for that row's item.
 */
public class ChevronButton extends Button {

    /**
     * Constructs a {@code ChevronButton} with a tooltip resolved from the given i18n key.
     *
     * @param onClick      the action to invoke when the button is clicked; must not be {@code null}
     * @param tooltipKey   the {@link LanguageManager} key for the tooltip text; must not be {@code null}
     * @throws NullPointerException if {@code onClick} or {@code tooltipKey} is {@code null}
     */
    public ChevronButton(Runnable onClick, String tooltipKey) {
        super("❯");
        Objects.requireNonNull(onClick, "onClick must not be null");
        Objects.requireNonNull(tooltipKey, "tooltipKey must not be null");
        getStyleClass().add("table-details-chevron");
        setOnAction(e -> onClick.run());

        Tooltip tooltip = new Tooltip(LanguageManager.get(tooltipKey));
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(this, tooltip);
    }
}
