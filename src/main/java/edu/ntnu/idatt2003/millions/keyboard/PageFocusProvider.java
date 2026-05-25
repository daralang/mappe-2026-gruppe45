package edu.ntnu.idatt2003.millions.keyboard;

/**
 * Implemented by top-level views that expose a keyboard focus entry point.
 *
 * <p>Allows the view coordinator to move focus into whichever page has just
 * become visible without knowing that page's internal controls.</p>
 */
public interface PageFocusProvider {

    /**
     * Requests focus on the most appropriate entry control in this page.
     */
    void focusPageEntry();
}
