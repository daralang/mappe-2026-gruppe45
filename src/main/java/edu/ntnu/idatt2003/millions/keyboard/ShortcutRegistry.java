package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry that maps {@link KeyCombination}s to actions.
 *
 * <p>Shortcuts are evaluated in insertion order; the first match wins.
 * Register shortcuts via {@link #register} and remove them via
 * {@link #unregister} when a component is torn down.</p>
 *
 * <p>Instances are not thread-safe and must be used on the JavaFX
 * application thread.</p>
 */
public final class ShortcutRegistry {

    private final Map<KeyCombination, Runnable> shortcuts = new LinkedHashMap<>();

    /**
     * Registers an action for the given combination, replacing any previous mapping.
     *
     * @param combination the key combination
     * @param action      the action to run
     */
    public void register(KeyCombination combination, Runnable action) {
        Objects.requireNonNull(combination, "combination must not be null");
        Objects.requireNonNull(action, "action must not be null");
        shortcuts.put(combination, action);
    }

    /**
     * Removes the action mapped to the given combination, if any.
     *
     * @param combination the key combination to remove
     */
    public void unregister(KeyCombination combination) {
        Objects.requireNonNull(combination, "combination must not be null");
        shortcuts.remove(combination);
    }

    /**
     * Tries to match the event against registered combinations and runs the
     * first match.
     *
     * @param event the key event to dispatch
     * @return {@code true} if a combination matched and its action was executed
     */
    public boolean dispatch(KeyEvent event) {
        for (Map.Entry<KeyCombination, Runnable> entry : shortcuts.entrySet()) {
            if (entry.getKey().match(event)) {
                entry.getValue().run();
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all registered shortcuts.
     */
    public void clear() {
        shortcuts.clear();
    }
}
