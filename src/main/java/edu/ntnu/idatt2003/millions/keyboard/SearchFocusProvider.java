package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.view.MainView;
import edu.ntnu.idatt2003.millions.view.component.SearchBar;

/**
 * Implemented by views that contain a {@link SearchBar}.
 *
 * <p>Allows {@link MainView} to delegate the
 * {@code Cmd/Ctrl+F} shortcut to whichever view is currently visible, without
 * the shortcut registration knowing anything about the view hierarchy.</p>
 *
 * <p>Views that have no active search bar should implement this as a no-op.</p>
 */
public interface SearchFocusProvider {

    /**
     * Requests focus on the most relevant search field in this view.
     * If no search field is available or visible, this method does nothing.
     */
    void focusSearch();
}
