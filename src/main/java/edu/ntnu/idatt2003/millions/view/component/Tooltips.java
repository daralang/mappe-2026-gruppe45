package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utility for attaching i18n-aware tooltips to any JavaFX node.
 * The tooltip text refreshes automatically when the application language changes.
 */
public final class Tooltips {

    private Tooltips() {}

    /**
     * Installs a tooltip on {@code node} whose text is resolved from the given
     * i18n key and is kept up to date when the language changes.
     *
     * @param node     the node to attach the tooltip to
     * @param i18nKey  the language key used to look up the tooltip text
     */
    public static void attach(Node node, String i18nKey) {
        Tooltip tooltip = new Tooltip(LanguageManager.get(i18nKey));
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(100));
        Tooltip.install(node, tooltip);
        LanguageManager.addObserver(() -> tooltip.setText(LanguageManager.get(i18nKey)));
    }
}
