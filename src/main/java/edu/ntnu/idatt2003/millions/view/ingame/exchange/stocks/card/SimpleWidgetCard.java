package edu.ntnu.idatt2003.millions.view.ingame.exchange.stocks.card;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.ColourChange;

import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.card.WidgetCard;

import java.math.BigDecimal;
import java.util.function.Supplier;

/**
 * A concrete {@link WidgetCard} that renders a title, a value and an optional subtitle.
 *
 */
public class SimpleWidgetCard extends WidgetCard {

    private final Supplier<String> valueSupplier;
    private final Supplier<String> subtitleSupplier;
    private final Supplier<BigDecimal> colourSource;

    private final StyledText valueLabel = StyledText.widgetValue();
    private final StyledText subtitleLabel = StyledText.widgetLabel();

    /**
     * Constructs a card with a title and a value.
     *
     * @param gameService   the game service to observe
     * @param titleKey      the i18n key for the card title
     * @param valueSupplier supplies the formatted value string on each refresh
     */
    public SimpleWidgetCard(GameService gameService,
                            String titleKey,
                            Supplier<String> valueSupplier) {
        this(gameService, titleKey, valueSupplier, null, null);
    }

    /**
     * Constructs a card with a title, a value and a subtitle.
     *
     * @param gameService      the game service to observe
     * @param titleKey         the i18n key for the card title
     * @param valueSupplier    supplies the formatted value string on each refresh
     * @param subtitleSupplier supplies the subtitle string on each refresh
     */
    public SimpleWidgetCard(GameService gameService,
                            String titleKey,
                            Supplier<String> valueSupplier,
                            Supplier<String> subtitleSupplier) {
        this(gameService, titleKey, valueSupplier, subtitleSupplier, null);
    }

    /**
     * Constructs a card with a title, a value, a subtitle and a colour source.
     * The colour source drives green/red styling on the value label via {@link ColourChange}.
     *
     * @param gameService      the game service to observe
     * @param titleKey         the i18n key for the card title
     * @param valueSupplier    supplies the formatted value string on each refresh
     * @param subtitleSupplier supplies the subtitle string on each refresh, or {@code null}
     * @param colourSource     supplies the sign value used for colour styling, or {@code null}
     */
    public SimpleWidgetCard(GameService gameService,
                            String titleKey,
                            Supplier<String> valueSupplier,
                            Supplier<String> subtitleSupplier,
                            Supplier<BigDecimal> colourSource) {
        super(gameService, titleKey);
        this.valueSupplier = valueSupplier;
        this.subtitleSupplier = subtitleSupplier;
        this.colourSource = colourSource;

        getChildren().add(titleLabel);
        getChildren().add(valueLabel);
        if (subtitleSupplier != null) {
            getChildren().add(subtitleLabel);
        }

        refreshDisplay();
    }

    /**
     * Updates the value label (and subtitle label if present) by invoking the suppliers.
     * Applies colour styling to the value label when a colour source is configured.
     */
    @Override
    protected void refreshDisplay() {
        valueLabel.setText(valueSupplier.get());
        if (colourSource != null) {
            ColourChange.applyChangeStyle(valueLabel, colourSource.get());
        }
        if (subtitleSupplier != null) {
            subtitleLabel.setText(subtitleSupplier.get());
        }
    }
}
