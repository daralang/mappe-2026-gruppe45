package edu.ntnu.idatt2003.millions.view.ingame.leaderboard;

import edu.ntnu.idatt2003.millions.keyboard.PageFocusProvider;
import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;
import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.service.toast.ToastService;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import edu.ntnu.idatt2003.millions.view.ingame.component.shell.WeekBar;
import edu.ntnu.idatt2003.millions.view.ingame.leaderboard.card.LeaderboardCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
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
public class LeaderboardView extends VBox implements SearchFocusProvider, PageFocusProvider {

    private final LeaderboardCard card;

    /**
     * Constructs a new LeaderboardView.
     *
     * @param gameService  the game service used by the card for observer registration
     * @param toastService the toast service used to display notifications
     * @param weekBar      the week bar owned exclusively by this view; each top-level view
     *                     receives its own instance since a JavaFX node can only belong to one parent at a time
     */
    public LeaderboardView(GameService gameService, ToastService toastService, WeekBar weekBar) {
        getStyleClass().add("content-area");

        StyledText pageTitle = StyledText.pageTitle(LanguageManager.get("leaderboard.title"));
        LanguageManager.addObserver(
                () -> pageTitle.setText(LanguageManager.get("leaderboard.title")));

        HBox titleRow = new HBox(12, pageTitle, weekBar);
        HBox.setHgrow(weekBar, Priority.ALWAYS);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        card = new LeaderboardCard(gameService, toastService);
        VBox.setVgrow(card, Priority.ALWAYS);
        VBox.setMargin(card, new Insets(10, 0, 0, 0));

        getChildren().addAll(titleRow, card);
    }

    /**
     * Focuses the leaderboard search field.
     */
    @Override
    public void focusSearch() {
        card.focusSearch();
    }

    /**
     * Focuses the search field as the keyboard entry point for this page.
     */
    @Override
    public void focusPageEntry() {
        focusSearch();
    }
}
