package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * A "ⓘ" icon that owns an i18n-aware tooltip.
 *
 * <p>The tooltip is <em>not</em> installed on the icon itself. Call
 * {@link #attachToParent(Node)} to install it on the desired hover target —
 * typically an HBox that contains both a label and this icon, so the tooltip
 * triggers anywhere the user hovers over that row. Installing the same
 * {@link Tooltip} instance on both a parent node and its child causes
 * {@code MOUSE_EXITED_TARGET} on the child to cancel the tooltip while the
 * mouse is still inside the parent, so only one node should own the install.</p>
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
        LanguageManager.addObserver(() -> tooltip.setText(LanguageManager.get(i18nKey)));
    }

    /**
     * Installs the tooltip on {@code parent}. The tooltip will trigger
     * whenever the user hovers anywhere over {@code parent}.
     * Pass {@code this} if the icon itself should be the sole hover target.
     *
     * @param parent the node to install the tooltip on
     */
    public void attachToParent(Node parent) {
        Tooltip.install(parent, tooltip);
    }
}
