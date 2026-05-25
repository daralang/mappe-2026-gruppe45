package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;

/**
 * Stateless policy deciding whether the currently focused control should keep the
 * vertical arrow keys for itself rather than letting them scroll the page.
 *
 * <p>Arrow keys serve two purposes in this application: navigating between focusable
 * table rows and scrolling the active view. A single high-level handler (installed
 * on the window root in {@code MainView}) owns page scrolling, and consults this
 * policy to decide when to step aside. The policy answers one question: does the
 * focus owner use UP/DOWN itself?</p>
 *
 */
public final class VerticalArrowPolicy {

    /**
     * Key placed in a {@link Node}'s property map to mark it as a keyboard-navigable
     * table row. Set by {@code SortColumnTable} on each row's focus anchor and read
     * by {@link #ownsVerticalArrows(Node)}.
     */
    public static final String ARROW_NAVIGABLE_ROW = "millions.arrowNavigableRow";

    private VerticalArrowPolicy() {
    }

    /**
     * Returns whether the given focus owner uses the vertical arrow keys itself and
     * should therefore not have them hijacked for page scrolling.
     *
     * @param focusOwner the scene's current focus owner; may be {@code null}
     * @return {@code true} if the focus owner navigates with UP/DOWN; {@code false}
     *         when it is {@code null} or has no claim on the keys
     */
    public static boolean ownsVerticalArrows(Node focusOwner) {
        if (focusOwner == null) {
            return false;
        }
        if (focusOwner instanceof TextInputControl || focusOwner instanceof ComboBoxBase<?>) {
            return true;
        }
        for (Node node = focusOwner; node != null; node = node.getParent()) {
            if (node.getProperties().containsKey(ARROW_NAVIGABLE_ROW)) {
                return true;
            }
        }
        return false;
    }
}
