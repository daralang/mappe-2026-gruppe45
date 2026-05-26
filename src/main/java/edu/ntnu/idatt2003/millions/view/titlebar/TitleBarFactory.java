// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import edu.ntnu.idatt2003.millions.util.OsDetector;
import javafx.stage.Stage;

/**
 * Factory that creates the platform-appropriate {@link TitleBar} implementation.
 */
public final class TitleBarFactory {

    private TitleBarFactory() {}

    /**
     * Creates the title bar for the main game view.
     *
     * @param stage       the primary stage
     * @param gameService the game service passed to the title bar for notification state
     * @return a {@link MacTitleBar} on macOS, or a {@link WindowsTitleBar} on other platforms
     */
    public static TitleBar create(Stage stage, GameService gameService) {
        if (OsDetector.isMac()) {
            return new MacTitleBar(gameService);
        }
        return new WindowsTitleBar(stage, gameService);
    }

    /**
     * Creates the title bar for the start screen.
     *
     * @param stage the primary stage
     * @return a {@link NoOpTitleBar} on macOS, or a {@link WindowsTitleBar} with start-screen
     *         styling on other platforms
     */
    public static TitleBar createForStartScreen(Stage stage) {
        if (OsDetector.isMac()) {
            return new NoOpTitleBar();
        }
        return new WindowsTitleBar(stage, "title-bar", false);
    }
}
