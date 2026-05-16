package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable value object representing a keyboard shortcut.
 *
 * <p>Combines a {@link KeyCode} with a set of {@link Modifier}s.
 * Use {@link #SHORTCUT} (Cmd on Mac, Ctrl on Win/Linux) rather than
 * platform-specific modifiers to keep shortcuts cross-platform.</p>
 *
 * <p>Suitable as a {@link java.util.Map} key.</p>
 */
public final class KeyBinding {

    /**
     * Platform-aware primary modifier: Cmd on macOS, Ctrl on Windows/Linux.
     * Mirrors {@code KeyCombination.SHORTCUT_DOWN}.
     */
    public enum Modifier {
        /** Cmd on macOS, Ctrl on Windows/Linux. */
        SHORTCUT,
        SHIFT,
        ALT
    }

    private final KeyCode key;
    private final Set<Modifier> modifiers;

    private KeyBinding(KeyCode key, Set<Modifier> modifiers) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.modifiers = modifiers.isEmpty()
                ? EnumSet.noneOf(Modifier.class)
                : EnumSet.copyOf(modifiers);
    }

    /**
     * Creates a binding for a bare key with no modifiers.
     *
     * @param key the key code
     * @return a new {@link KeyBinding}
     */
    public static KeyBinding of(KeyCode key) {
        return new KeyBinding(key, EnumSet.noneOf(Modifier.class));
    }

    /**
     * Creates a binding for a key with one or more modifiers.
     *
     * @param key       the key code
     * @param first     required modifier
     * @param rest      additional modifiers
     * @return a new {@link KeyBinding}
     */
    public static KeyBinding of(KeyCode key, Modifier first, Modifier... rest) {
        EnumSet<Modifier> mods = EnumSet.of(first, rest);
        return new KeyBinding(key, mods);
    }

    /**
     * Returns {@code true} if this binding matches the given {@link KeyEvent}.
     *
     * <p>{@link Modifier#SHORTCUT} is satisfied when the event's shortcut key
     * is down ({@code isShortcutDown()}), ensuring cross-platform behaviour.</p>
     *
     * @param event the key event to test
     * @return {@code true} if the event matches this binding
     */
    public boolean matches(KeyEvent event) {
        if (event.getCode() != key) {
            return false;
        }
        boolean shortcut = modifiers.contains(Modifier.SHORTCUT) == event.isShortcutDown();
        boolean shift    = modifiers.contains(Modifier.SHIFT)    == event.isShiftDown();
        boolean alt      = modifiers.contains(Modifier.ALT)      == event.isAltDown();
        return shortcut && shift && alt;
    }

    /** Returns the key code. */
    public KeyCode getKey() {
        return key;
    }

    /** Returns an unmodifiable view of the modifiers. */
    public Set<Modifier> getModifiers() {
        return modifiers.isEmpty()
                ? Set.of()
                : EnumSet.copyOf(modifiers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof KeyBinding other)) {
            return false;
        }
        return key == other.key && modifiers.equals(other.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, modifiers);
    }

    @Override
    public String toString() {
        return modifiers.isEmpty() ? key.getName() : modifiers + "+" + key.getName();
    }
}
