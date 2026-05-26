// Javadoc generated with AI assistance - reviewed and approved by author.
package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.game.GameService;
import javafx.scene.Node;

/**
 * macOS implementation of {@link TitleBar} that delegates navigation and actions
 * to a {@link Header} component.
 */
public class MacTitleBar implements TitleBar {

    private Runnable onDashboard   = () -> {};
    private Runnable onExchange    = () -> {};
    private Runnable onLeaderboard = () -> {};
    private Runnable onNewGame     = () -> {};
    private Runnable onSave        = () -> {};
    private Runnable onExit        = () -> {};

    private final Header header;

    /**
     * Constructs a MacTitleBar backed by a {@link Header} component.
     *
     * @param gameService the game service passed to the header for notification state
     */
    public MacTitleBar(GameService gameService) {
        header = new Header(
                () -> onDashboard.run(),
                () -> onExchange.run(),
                () -> onSave.run(),
                () -> onExit.run(),
                gameService
        );
    }

    @Override public Node getNode()                       { return header; }
    @Override public Node getOverlayNode()                { return header.getOverlayNode(); }
    @Override public void setOnDashboard(Runnable r)      { onDashboard = r; }
    @Override public void setOnExchange(Runnable r)       { onExchange = r; }
    @Override public void setOnLeaderboard(Runnable r)    { onLeaderboard = r; header.setOnLeaderboard(r); }
    @Override public void setOnNewGame(Runnable r)        { onNewGame = r; header.setOnNewGame(r); }
    @Override public void setOnSave(Runnable r)           { onSave = r; }
    @Override public void setOnExit(Runnable r)           { onExit = r; }
    @Override public void onGameUpdated()                 { header.onGameUpdated(); }
    @Override public void onLanguageChanged()             {}
}
