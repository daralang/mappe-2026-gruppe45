package edu.ntnu.idatt2003.millions.keyboard.navigation;

import javafx.scene.input.KeyCode;

/**
 * A scrollable surface that can be stepped up or down by an arrow key.
 *
 * <p>Implemented by the view's scroll pane and driven by {@link PageArrowDispatcher},
 * so the dispatcher (in the {@code keyboard} package) can scroll the active page
 * without depending on the concrete view component. This keeps the dependency
 * direction one-way: the view depends on {@code keyboard}, not the reverse.</p>
 *
 * <p>Marked as {@link FunctionalInterface} because it has a single abstract method,
 * allowing implementations to be expressed as lambdas where convenient.</p>
 */
@FunctionalInterface
public interface PageScroller {

    /**
     * Scrolls one step in the direction of the given arrow key.
     *
     * @param code the pressed key; implementations act on {@link KeyCode#UP} and
     *             {@link KeyCode#DOWN} and ignore others
     */
    void scrollByArrow(KeyCode code);
}
