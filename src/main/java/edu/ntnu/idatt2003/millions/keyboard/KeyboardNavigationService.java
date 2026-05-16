package edu.ntnu.idatt2003.millions.keyboard;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Central dispatcher for keyboard navigation across the application.
 *
 * <p>Attach this service to the app-lifetime {@link Scene} once via
 * {@link #attach(Scene)}. It installs a single {@code EventFilter} that
 * intercepts all key-pressed events before they reach individual nodes.</p>
 *
 * <p>Dispatch order on each key event:</p>
 * <ol>
 *   <li>The top {@link KeyboardContext} on the stack (e.g. an open modal).</li>
 *   <li>If no context is active or the context did not handle the event,
 *       the global {@link ShortcutRegistry} is consulted.</li>
 * </ol>
 *
 * <p>This ensures that global shortcuts (Cmd+1, Cmd+S, …) are automatically
 * suppressed whenever a modal dialog is on top of the stack.</p>
 *
 * <p>Components push themselves when shown and pop themselves when closed:</p>
 * <pre>{@code
 * keyboardService.pushContext(this);  // on show
 * keyboardService.popContext(this);   // on close
 * }</pre>
 */
public final class KeyboardNavigationService {

    private final Deque<KeyboardContext> contextStack = new ArrayDeque<>();
    private final ShortcutRegistry globalRegistry = new ShortcutRegistry();

    private Scene attachedScene;
    private EventHandler<KeyEvent> eventFilter;

    /**
     * Attaches this service to the given scene. Must be called once before
     * any shortcuts or contexts are active. Calling again with a different
     * scene detaches from the previous one first.
     *
     * @param scene the app-lifetime scene to listen on
     */
    public void attach(Scene scene) {
        Objects.requireNonNull(scene, "scene must not be null");
        detach();
        attachedScene = scene;
        eventFilter = this::onKeyPressed;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
    }

    /**
     * Detaches this service from its current scene and clears all state.
     * Safe to call even when not attached.
     */
    public void detach() {
        if (attachedScene != null && eventFilter != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
        }
        attachedScene = null;
        eventFilter = null;
        contextStack.clear();
        globalRegistry.clear();
    }

    /**
     * Pushes a context onto the stack, making it the active receiver of
     * key events. Notifies the previous top context that it is deactivated,
     * and notifies the new context that it is activated.
     *
     * @param context the context to push
     */
    public void pushContext(KeyboardContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (!contextStack.isEmpty()) {
            contextStack.peek().onContextDeactivated();
        }
        contextStack.push(context);
        context.onContextActivated();
    }

    /**
     * Removes the given context from the top of the stack. If the context
     * is not at the top, this is a no-op to guard against double-pop bugs.
     * Notifies the context being removed and activates the one below it.
     *
     * @param context the context to remove
     */
    public void popContext(KeyboardContext context) {
        Objects.requireNonNull(context, "context must not be null");
        if (contextStack.isEmpty() || contextStack.peek() != context) {
            return;
        }
        contextStack.pop().onContextDeactivated();
        if (!contextStack.isEmpty()) {
            contextStack.peek().onContextActivated();
        }
    }

    /**
     * Returns the global {@link ShortcutRegistry} for registering and
     * unregistering application-wide keyboard shortcuts.
     *
     * @return the global shortcut registry
     */
    public ShortcutRegistry globalShortcuts() {
        return globalRegistry;
    }

    /**
     * Returns {@code true} if at least one context is currently on the stack.
     *
     * @return {@code true} when a modal context is active
     */
    public boolean hasActiveContext() {
        return !contextStack.isEmpty();
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && firesFocusedButton()) {
            event.consume();
            return;
        }
        if (!contextStack.isEmpty()) {
            boolean handled = contextStack.peek().handleKeyPressed(event);
            if (handled) {
                event.consume();
                return;
            }
        }
        if (globalRegistry.dispatch(event)) {
            event.consume();
        }
    }

    /**
     * If the scene's current focus owner is a {@link Button}, fires it and
     * returns {@code true}. Returns {@code false} for any other node type.
     *
     * <p>Called before context-stack and global-shortcut dispatch so that
     * Enter always activates the focused button, matching standard desktop
     * keyboard conventions.</p>
     *
     * @return {@code true} if a button was fired
     */
    private boolean firesFocusedButton() {
        if (attachedScene == null) {
            return false;
        }
        Node focused = attachedScene.getFocusOwner();
        if (focused instanceof Button button) {
            button.fire();
            return true;
        }
        return false;
    }
}
