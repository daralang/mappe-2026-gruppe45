package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Registry that maps {@link KeyCombination}s to actions.
 *
 * <p>Shortcuts are evaluated in insertion order; the first match wins.
 * Register shortcuts and remove them when a component is torn down.</p>
 *
 * <p>Instances are not thread-safe and must be used on the JavaFX
 * application thread.</p>
 */
public final class ShortcutRegistry {

    private static final KeyCode[] DIGIT_KEYS = {
        KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4
    };

    private final Map<KeyCombination, BooleanSupplier> shortcuts = new LinkedHashMap<>();

    /**
     * Registers an action for the given combination, replacing any previous mapping.
     * The event is always consumed when the combination matches.
     *
     * @param combination the key combination
     * @param action      the action to run
     */
    public void register(KeyCombination combination, Runnable action) {
        Objects.requireNonNull(combination, "combination must not be null");
        Objects.requireNonNull(action, "action must not be null");
        shortcuts.put(combination, () -> { action.run(); return true; });
    }

    /**
     * Registers a conditional action for the given combination, replacing any previous mapping.
     * The event is consumed only if {@code action} returns {@code true}, allowing the action
     * to decline handling (e.g. when the focused node is a text input rather than a button).
     *
     * @param combination the key combination
     * @param action      the action; return {@code true} if the event was handled,
     *                    {@code false} to pass it through
     */
    public void register(KeyCombination combination, BooleanSupplier action) {
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
        for (Map.Entry<KeyCombination, BooleanSupplier> entry : shortcuts.entrySet()) {
            if (entry.getKey().match(event)) {
                return entry.getValue().getAsBoolean();
            }
        }
        return false;
    }

    /**
     * Registers a sequence of tab actions on {@code Shift+1}, {@code Shift+2}, …
     * {@code Shift+N}, up to a maximum of four tabs.
     *
     * @param tabActions the actions to register, in tab order; at most four
     *                   entries are used
     * @throws NullPointerException if any element of {@code tabActions} is null
     */
    public void registerTabShortcuts(Runnable... tabActions) {
        for (int i = 0; i < tabActions.length && i < DIGIT_KEYS.length; i++) {
            register(new KeyCodeCombination(DIGIT_KEYS[i], KeyCombination.SHIFT_DOWN), tabActions[i]);
        }
    }

    /**
     * Removes all registered shortcuts.
     */
    public void clear() {
        shortcuts.clear();
    }
}
