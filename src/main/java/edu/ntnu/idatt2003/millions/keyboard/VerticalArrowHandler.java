package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyEvent;

/**
 * Strategy a focused control registers to claim the vertical arrow keys.
 *
 * <p>The handler reports whether it consumed the key. Returning {@code false} lets the
 * dispatcher fall back to page scrolling, which is how a table releases the key at its
 * first/last row and how a search field behaves when it has no row to hand focus to.</p>
 */
@FunctionalInterface
public interface VerticalArrowHandler {

    /**
     * Handles a vertical arrow {@code KEY_PRESSED} while the owning node has focus.
     *
     * @param event the key event (UP or DOWN)
     * @return {@code true} if the key was handled and should be consumed;
     *         {@code false} to let the dispatcher scroll the page instead
     */
    boolean handle(KeyEvent event);
}
