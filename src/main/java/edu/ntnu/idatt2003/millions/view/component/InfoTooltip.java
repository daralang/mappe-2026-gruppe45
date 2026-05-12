package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A self-contained "ⓘ" icon that owns its own i18n-aware tooltip.
 *
 * <p>The tooltip is installed on the icon itself by default. Call
 * {@link #attachToParent(Node)} to additionally install it on a surrounding
 * container (e.g. an HBox holding a label and this icon), so the tooltip
 * triggers anywhere the user hovers over that container — not only over the
 * icon.</p>
 *
 * <p>The tooltip text is resolved via {@link LanguageManager} and refreshes
 * automatically when the application language changes.</p>
 */
public class InfoTooltip extends Label {

    private final Tooltip tooltip;

    /**
     * @param i18nKey the language key used to look up the tooltip text
     */
    public InfoTooltip(String i18nKey) {
        super("ⓘ");
        getStyleClass().add("info-tooltip-icon");
        tooltip = new Tooltip(LanguageManager.get(i18nKey));
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(this, tooltip);
        LanguageManager.addObserver(() -> tooltip.setText(LanguageManager.get(i18nKey)));
    }

    /**
     * Installs the same tooltip on {@code parent}, so that hovering anywhere
     * over the parent node (e.g. a label-and-icon HBox) triggers the tooltip.
     *
     * @param parent the node to extend the hover target to
     */
    public void attachToParent(Node parent) {
        Tooltip.install(parent, tooltip);
    }
}
