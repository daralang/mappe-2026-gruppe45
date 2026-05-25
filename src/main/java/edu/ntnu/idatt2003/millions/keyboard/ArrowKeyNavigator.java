package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyEvent;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Reusable keyboard-navigation logic for a vertical list of linearly indexed items.
 *
 * <p>Handles UP/DOWN movement and Enter/Space confirmation over a list of items
 * identified by integer indices.</p>
 *
 * <p>The caller is responsible for visual selection feedback; this class
 * only tracks and moves the index and fires the supplied callbacks.</p>
 */
public final class ArrowKeyNavigator {

    private final IntSupplier sizeSupplier;
    private final IntConsumer onSelect;
    private final IntConsumer onConfirm;
    private final boolean wrap;

    private int selectedIndex = 0;

    /**
     * Creates a navigator.
     *
     * @param sizeSupplier supplies the current number of items; queried on each event
     * @param onSelect     called with the new index whenever selection moves
     * @param onConfirm    called with the current index when Enter or Space is pressed
     * @param wrap         whether navigation wraps around at the ends
     */
    public ArrowKeyNavigator(
            IntSupplier sizeSupplier,
            IntConsumer onSelect,
            IntConsumer onConfirm,
            boolean wrap) {
        this.sizeSupplier = Objects.requireNonNull(sizeSupplier, "sizeSupplier must not be null");
        this.onSelect = Objects.requireNonNull(onSelect, "onSelect must not be null");
        this.onConfirm = Objects.requireNonNull(onConfirm, "onConfirm must not be null");
        this.wrap = wrap;
    }

    /**
     * Processes a key event. Call this from a {@code KEY_PRESSED} event handler
     * registered on the node that owns the navigable items.
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
            case UP -> { moveTo(selectedIndex - 1, size); yield true; }
            case DOWN -> { moveTo(selectedIndex + 1, size); yield true; }
            case ENTER, SPACE -> { onConfirm.accept(selectedIndex); yield true; }
            default -> false;
        };
    }

    /**
     * Silently updates the selected index without firing {@code onSelect}.
     *
     * <p>Use this to synchronise the navigator's internal index with an externally
     * determined position — for example when the user arrives at an item via Tab
     * rather than arrow keys — before delegating the first key event to
     * {@link #navigate}. This prevents the navigator from jumping back to its
     * previous position on the first arrow-key press.</p>
     *
     * @param index the index to synchronise to; clamped to valid range
     */
    public void syncIndex(int index) {
        int size = sizeSupplier.getAsInt();
        if (size == 0) {
            return;
        }
        selectedIndex = Math.clamp(index, 0, size - 1);
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

}
