package edu.ntnu.idatt2003.millions.keyboard.registry;

import edu.ntnu.idatt2003.millions.keyboard.SearchFocusProvider;

/**
 * Tracks the currently active {@link SearchFocusProvider} and delegates
 * search-focus requests to it.
 */
public final class SearchFocusRegistry {

    private final ActiveRegistry<SearchFocusProvider> registry = new ActiveRegistry<>();

    /**
     * Registers the given provider as the active search target.
     * Pass {@code null} to clear the active provider.
     *
     * @param provider the provider to activate, or {@code null}
     */
    public void setActive(SearchFocusProvider provider) {
        registry.setActive(provider);
    }

    /**
     * Delegates focus to the active {@link SearchFocusProvider}, if any.
     * Does nothing if no provider is currently active.
     */
    public void focusActive() {
        registry.ifActive(SearchFocusProvider::focusSearch);
    }
}
