package edu.ntnu.idatt2003.millions.view.dashboard.portfolio.card;

import edu.ntnu.idatt2003.millions.manager.GameManager;
import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Card;
import javafx.scene.control.Label;

/**
 * Card displaying the player's currently available funds for trading.
 */
public class AvailableFundsCard extends Card {

    private final GameManager gameManager;
    private final Label valueLabel;

    /**
     * Constructs a new AvailableFundsCard and initializes the display.
     *
     * @param gameManager the game manager containing player and exchange
     */
    public AvailableFundsCard(GameManager gameManager) {
        super(gameManager);
        this.gameManager = gameManager;
        setSpacing(4);

        Label titleLabel = new Label(LanguageManager.get("dashboard.availableFunds"));
        titleLabel.getStyleClass().add("card-label");

        valueLabel = new Label();
        valueLabel.getStyleClass().add("card-value");

        getChildren().addAll(titleLabel, valueLabel);

        valueLabel.setText(CurrencyFormatter.format(gameManager.getPlayer().getMoney()));
    }

    /**
     * Called when the game state has changed.
     * Refreshes the displayed available funds.
     */
    @Override
    public void onGameUpdated() {
        valueLabel.setText(CurrencyFormatter.format(gameManager.getPlayer().getMoney()));
    }
}