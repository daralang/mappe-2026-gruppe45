package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry that maps {@link KeyBinding}s to actions.
 *
 * <p>Shortcuts are evaluated in insertion order; the first match wins.
 * Register shortcuts via {@link #register} and remove them via
 * {@link #unregister} when a component is torn down.</p>
 *
 * <p>Instances are not thread-safe and must be used on the JavaFX
 * application thread.</p>
 */
public final class ShortcutRegistry {

    private final Map<KeyBinding, Runnable> shortcuts = new LinkedHashMap<>();

    /**
     * Registers an action for the given binding, replacing any previous mapping.
     *
     * @param binding the key binding
     * @param action  the action to run
     */
    public void register(KeyBinding binding, Runnable action) {
        Objects.requireNonNull(binding, "binding must not be null");
        Objects.requireNonNull(action, "action must not be null");
        shortcuts.put(binding, action);
    }

    /**
     * Removes the action mapped to the given binding, if any.
     *
     * @param binding the key binding to remove
     */
    public void unregister(KeyBinding binding) {
        Objects.requireNonNull(binding, "binding must not be null");
        shortcuts.remove(binding);
    }

    /**
     * Tries to match the event against registered bindings and runs the
     * first match.
     *
     * @param event the key event to dispatch
     * @return {@code true} if a binding matched and its action was executed
     */
    public boolean dispatch(KeyEvent event) {
        for (Map.Entry<KeyBinding, Runnable> entry : shortcuts.entrySet()) {
            if (entry.getKey().matches(event)) {
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
