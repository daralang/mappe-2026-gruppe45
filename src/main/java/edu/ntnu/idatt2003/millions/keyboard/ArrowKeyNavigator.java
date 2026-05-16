package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyEvent;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Reusable keyboard-navigation logic for linearly indexed UI elements.
 *
 * <p>Handles arrow-key movement and Enter confirmation over a list of items
 * identified by integer indices. The navigator is orientation-aware:
 * {@link Orientation#HORIZONTAL} responds to LEFT/RIGHT, while
 * {@link Orientation#VERTICAL} responds to UP/DOWN.</p>
 *
 * <p>The caller is responsible for visual selection feedback; this class
 * only tracks and moves the index and fires the supplied callbacks.</p>
 *
 * <p>Usage example (horizontal, no wrap):</p>
 * <pre>{@code
 * ArrowKeyNavigator nav = new ArrowKeyNavigator(
 *         Orientation.HORIZONTAL,
 *         offers::size,
 *         this::select,
 *         index -> applyButtons.get(index).fire(),
 *         false
 * );
 * // in KeyboardContext.handleKeyPressed:
 * return nav.navigate(event);
 * }</pre>
 */
public final class ArrowKeyNavigator {

    /** Determines which arrow keys move the selection. */
    public enum Orientation {
        /** LEFT decrements, RIGHT increments. */
        HORIZONTAL,
        /** UP decrements, DOWN increments. */
        VERTICAL
    }

    private final Orientation orientation;
    private final IntSupplier sizeSupplier;
    private final IntConsumer onSelect;
    private final IntConsumer onConfirm;
    private final boolean wrap;

    private int selectedIndex = 0;

    /**
     * Creates a navigator.
     *
     * @param orientation  which arrow keys to respond to
     * @param sizeSupplier supplies the current number of items; queried on each event
     * @param onSelect     called with the new index whenever selection moves
     * @param onConfirm    called with the current index when Enter is pressed
     * @param wrap         whether navigation wraps around at the ends
     */
    public ArrowKeyNavigator(
            Orientation orientation,
            IntSupplier sizeSupplier,
            IntConsumer onSelect,
            IntConsumer onConfirm,
            boolean wrap) {
        this.orientation = Objects.requireNonNull(orientation, "orientation must not be null");
        this.sizeSupplier = Objects.requireNonNull(sizeSupplier, "sizeSupplier must not be null");
        this.onSelect = Objects.requireNonNull(onSelect, "onSelect must not be null");
        this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm must not be null");
        this.wrap = wrap;
    }

    /**
     * Processes a key event. Call this from
     * {@link KeyboardContext#handleKeyPressed(KeyEvent)}.
     *
     * @param event the key event to process
     * @return {@code true} if the event was consumed by this navigator
     */
    public boolean navigate(KeyEvent event) {
        int size = sizeSupplier.getAsInt();
        if (size == 0) {
            return false;
        }
        return switch (event.getCode()) {
            case LEFT, UP   when isDecrement(event.getCode()) -> { moveTo(selectedIndex - 1, size); yield true; }
            case RIGHT, DOWN when isIncrement(event.getCode()) -> { moveTo(selectedIndex + 1, size); yield true; }
            case ENTER -> { onConfirm.accept(selectedIndex); yield true; }
            default -> false;
        };
    }

    /**
     * Programmatically selects the given index without firing {@code onConfirm}.
     * Clamps to valid range.
     *
     * @param index the index to select
     */
    public void select(int index) {
        int size = sizeSupplier.getAsInt();
        if (size == 0) {
            return;
        }
        selectedIndex = Math.clamp(index, 0, size - 1);
        onSelect.accept(selectedIndex);
    }

    /** Returns the currently selected index. */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    private void moveTo(int next, int size) {
        if (wrap) {
            selectedIndex = Math.floorMod(next, size);
        } else {
            selectedIndex = Math.clamp(next, 0, size - 1);
        }
        onSelect.accept(selectedIndex);
    }

    private boolean isDecrement(javafx.scene.input.KeyCode code) {
        return switch (orientation) {
            case HORIZONTAL -> code == javafx.scene.input.KeyCode.LEFT;
            case VERTICAL   -> code == javafx.scene.input.KeyCode.UP;
        };
    }

    private boolean isIncrement(javafx.scene.input.KeyCode code) {
        return switch (orientation) {
            case HORIZONTAL -> code == javafx.scene.input.KeyCode.RIGHT;
            case VERTICAL   -> code == javafx.scene.input.KeyCode.DOWN;
        };
    }
}
