package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.view.SearchFocusProvider;

/**
 * Tracks the currently active {@link SearchFocusProvider} and delegates
 * focus requests to it.
 *
 * <p>The view layer calls {@link SearchFocusProvider} whenever
 * the visible top-level view changes. The controller calls {@link #focusActive()}
 * in response to the {@code Cmd/Ctrl+F} shortcut, without needing to know
 * which view is currently shown.</p>
 *
 * <p>If no provider is registered, {@link #focusActive()} is a no-op.</p>
 */
public final class SearchFocusRegistry {

    private SearchFocusProvider active;

    /**
     * Registers the given provider as the active search target.
     * Pass {@code null} to clear the active provider.
     *
     * @param provider the provider to activate, or {@code null}
     */
    public void setActive(SearchFocusProvider provider) {
        this.active = provider;
    }

    /**
     * Delegates focus to the active {@link SearchFocusProvider}, if any.
     * Does nothing if no provider is currently active.
     */
    public void focusActive() {
        if (active != null) {
            active.focusSearch();
        }
    }
}
