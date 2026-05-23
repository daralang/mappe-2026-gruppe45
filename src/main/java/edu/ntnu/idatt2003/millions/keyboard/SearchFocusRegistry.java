package edu.ntnu.idatt2003.millions.keyboard;

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
