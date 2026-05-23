package edu.ntnu.idatt2003.millions.keyboard;

import java.util.function.IntConsumer;

/**
 * Tracks the tab navigator of the currently active view and delegates
 * tab-selection requests to it.
 */
public final class TabNavigationRegistry {

    private IntConsumer active;

    /**
     * Registers the given navigator as the active tab handler.
     * Pass {@code null} to clear the active navigator (e.g. for views with no tabs).
     *
     * @param navigator an {@link IntConsumer} that switches to the tab at the given
     *                  zero-based index, or {@code null}
     */
    public void setActive(IntConsumer navigator) {
        this.active = navigator;
    }

    /**
     * Delegates a tab-selection request to the active navigator, if any.
     * Does nothing if no navigator is currently registered.
     *
     * @param index the zero-based index of the tab to select
     */
    public void selectTab(int index) {
        if (active != null) {
            active.accept(index);
        }
    }
}
