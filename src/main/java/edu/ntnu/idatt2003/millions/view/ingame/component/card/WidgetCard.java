package edu.ntnu.idatt2003.millions.view.ingame.component.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;

/**
 * Abstract base class for read-only dashboard widget cards.
 * Handles title setup, spacing, and wires language/game observer
 * hooks to {@link #refreshDisplay()}, which subclasses implement.
 */
public abstract class WidgetCard extends Card {

    private final String titleKey;
    protected final StyledText titleLabel = StyledText.widgetLabel();

    protected WidgetCard(GameService gameService, String titleKey) {
        super(gameService);
        this.titleKey = titleKey;
        setSpacing(4);
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
