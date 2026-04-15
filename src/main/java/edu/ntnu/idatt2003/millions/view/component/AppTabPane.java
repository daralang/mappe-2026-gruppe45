package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;
import java.util.Objects;

/**
 * Reusable application tab pane with sensible defaults.
 * Intended for use in views that need a consistent tab layout.
 */
public class AppTabPane extends TabPane {

    /**
     * Creates an empty tab pane with shared defaults.
     */
    public AppTabPane() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        setFocusTraversable(false);
    }

    /**
     * Creates a tab pane with the provided tabs.
     *
     * @param tabs the tabs to add
     * @throws NullPointerException if tabs or any tab is null
     */
    public AppTabPane(List<Tab> tabs) {
        this();
        Objects.requireNonNull(tabs, "Tabs cannot be null");
        tabs.forEach(tab -> Objects.requireNonNull(tab, "Tab cannot be null"));
        getTabs().addAll(tabs);
    }

    /**
     * Creates a standard non-closable tab.
     *
     * @param title   the tab title
     * @param content the content of the tab
     * @return a configured {@link Tab}
     * @throws NullPointerException if title or content is null
     * @throws IllegalArgumentException if title is blank
     */
    public static Tab createTab(String title, Node content) {
        Objects.requireNonNull(title, "Title cannot be null");
        Objects.requireNonNull(content, "Content cannot be null");

        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }

        Tab tab = new Tab(title, content);
        tab.setClosable(false);
        return tab;
    }
}