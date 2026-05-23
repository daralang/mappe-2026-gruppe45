package edu.ntnu.idatt2003.millions.keyboard;

import javafx.scene.input.KeyEvent;

/**
 * Contract for UI components that participate in keyboard navigation.
 *
 * <p>Implementations are pushed onto the {@link KeyboardNavigationService}
 * context stack when they become active and popped when they are dismissed.
 * The service forwards {@link KeyEvent}s only to the top-most context,
 * so modal dialogs automatically suppress global shortcuts while open.</p>
 *
 * <p>Lifecycle example for a modal:</p>
 * <pre>{@code
 * // on show
 * keyboardService.pushContext(this);
 *
 * // on close
 * keyboardService.popContext(this);
 * }</pre>
 */
public interface KeyboardContext {

    /**
     * Handles a key-pressed event for this context.
     *
     * @param event the key event; call {@link KeyEvent#consume()} to stop propagation
     * @return {@code true} if the event was handled and should not propagate further
     */
    boolean handleKeyPressed(KeyEvent event);

    /**
     * Called when this context becomes the active (top-of-stack) context.
     * Default is a no-op.
     */
    default void onContextActivated() {}

    /**
     * Called when this context is no longer the active context.
     * Default is a no-op.
     */
    default void onContextDeactivated() {}
}
