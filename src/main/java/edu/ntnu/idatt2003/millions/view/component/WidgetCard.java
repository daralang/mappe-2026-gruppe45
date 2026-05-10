package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.Label;

/**
 * Abstract base class for read-only dashboard widget cards.
 * Handles title setup, spacing, and wires language/game observer
 * hooks to {@link #refreshDisplay()}, which subclasses implement.
 */
public abstract class WidgetCard extends Card {

    private final String titleKey;
    protected final Label titleLabel = new Label();

    protected WidgetCard(GameManager gameManager, String titleKey) {
        super(gameManager);
        this.titleKey = titleKey;
        setSpacing(4);
        titleLabel.getStyleClass().add("widget-label");
        titleLabel.setText(LanguageManager.get(titleKey));
    }

    protected abstract void refreshDisplay();

    @Override
    protected void onLanguageChanged() {
        titleLabel.setText(LanguageManager.get(titleKey));
        refreshDisplay();
    }

    @Override
    public void onGameUpdated() {
        refreshDisplay();
    }
}
