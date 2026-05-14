package edu.ntnu.idatt2003.millions.view.leaderboard;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.leaderboard.card.LeaderboardCard;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Top-level leaderboard view.
 *
 * <p>A single-card page with no sub-view tabs. The {@link LeaderboardCard}
 * handles search, sorting, pagination and data loading. The view auto-refreshes
 * via {@link edu.ntnu.idatt2003.millions.observer.GameObserver} callbacks wired
 * inside the card.</p>
 */
public class LeaderboardView extends VBox {

    /**
     * Constructs a new LeaderboardView.
     *
     * @param gameService the game service used by the card for observer registration
     */
    public LeaderboardView(GameService gameService) {
        getStyleClass().add("content-area");

        StyledText pageTitle = StyledText.pageTitle(LanguageManager.get("leaderboard.title"));
        LanguageManager.addObserver(
                () -> pageTitle.setText(LanguageManager.get("leaderboard.title")));

        LeaderboardCard card = new LeaderboardCard(gameService);
        VBox.setVgrow(card, Priority.ALWAYS);

        getChildren().addAll(pageTitle, card);
    }
}
