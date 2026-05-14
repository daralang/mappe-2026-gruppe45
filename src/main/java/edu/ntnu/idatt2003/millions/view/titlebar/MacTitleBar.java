package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.view.component.Header;
import javafx.scene.Node;

/**
 * Title bar for macOS.
 *
 * <p>The OS supplies the native traffic-light controls; this variant exposes only
 * the shared {@link Header} navbar with no custom minimize / maximize / close buttons.
 * The dark native bar is requested at JVM startup via
 * {@code apple.awt.application.appearance=NSAppearanceNameDarkAqua} in
 * {@code App.main()}.</p>
 *
 * <p>Requires {@link javafx.stage.StageStyle#UNIFIED} to be set on the stage
 * before the scene is attached — the factory handles this.</p>
 */
public class MacTitleBar implements TitleBar {

    private Runnable onDashboard = () -> {};
    private Runnable onExchange  = () -> {};
    private Runnable onSave      = () -> {};
    private Runnable onExit      = () -> {};

    private final Header header;

    public MacTitleBar() {
        header = new Header(
                () -> onDashboard.run(),
                () -> onExchange.run(),
                () -> onSave.run(),
                () -> onExit.run()
        );
    }

    @Override public Node getNode()                    { return header; }
    @Override public void setOnDashboard(Runnable r)   { onDashboard = r; }
    @Override public void setOnExchange(Runnable r)    { onExchange = r; }
    @Override public void setOnSave(Runnable r)        { onSave = r; }
    @Override public void setOnExit(Runnable r)        { onExit = r; }
    @Override public void onGameUpdated()              {}
    @Override public void onLanguageChanged()          {}
}
