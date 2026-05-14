package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.view.component.Header;
import javafx.scene.Node;

public class MacTitleBar implements TitleBar {

    private Runnable onDashboard   = () -> {};
    private Runnable onExchange    = () -> {};
    private Runnable onLeaderboard = () -> {};
    private Runnable onSave        = () -> {};
    private Runnable onExit        = () -> {};

    private final Header header;

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
    @Override public void setOnSave(Runnable r)           { onSave = r; }
    @Override public void setOnExit(Runnable r)           { onExit = r; }
    @Override public void onGameUpdated()                 { header.onGameUpdated(); }
    @Override public void onLanguageChanged()             {}
}
