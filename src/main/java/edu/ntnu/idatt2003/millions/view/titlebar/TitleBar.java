package edu.ntnu.idatt2003.millions.view.titlebar;

import javafx.scene.Node;

/**
 * Shared contract for both title-bar variants.
 *
 * <p>Callers receive an implementation from {@link TitleBarFactory} and wire
 * callbacks against this interface — no type-checks or casts are ever needed.</p>
 */
public interface TitleBar {

    /** The root node to place at the top of the main layout. */
    Node getNode();

    /** The toast overlay node to layer over the main layout. Returns null if not applicable. */
    default Node getOverlayNode() { return null; }

    void setOnDashboard(Runnable callback);
    void setOnExchange(Runnable callback);
    default void setOnLeaderboard(Runnable callback) {}
    default void setOnNewGame(Runnable callback) {}
    void setOnSave(Runnable callback);
    void setOnExit(Runnable callback);

    /** Called after every game-state change (e.g. to refresh a notification badge). */
    void onGameUpdated();

    /** Called after a language switch so all text can be refreshed. */
    void onLanguageChanged();
}
