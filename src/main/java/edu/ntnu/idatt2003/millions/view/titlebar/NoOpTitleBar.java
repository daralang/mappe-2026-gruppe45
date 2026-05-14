package edu.ntnu.idatt2003.millions.view.titlebar;

import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * A do-nothing {@link TitleBar} used on macOS for views that have no shared
 * navbar (e.g. the start screen). The OS supplies the native traffic-light
 * controls; no custom node is needed.
 */
class NoOpTitleBar implements TitleBar {

    private static final Region EMPTY = new Region();

    @Override public Node getNode()                  { return EMPTY; }
    @Override public void setOnDashboard(Runnable r) {}
    @Override public void setOnExchange(Runnable r)  {}
    @Override public void setOnSave(Runnable r)      {}
    @Override public void setOnExit(Runnable r)      {}
    @Override public void onGameUpdated()            {}
    @Override public void onLanguageChanged()        {}
}
