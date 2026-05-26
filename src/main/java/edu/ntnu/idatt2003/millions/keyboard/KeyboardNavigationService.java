package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.keyboard.registry.ShortcutRegistry;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;

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
 *   <li>The universal {@link ShortcutRegistry}, for shortcuts that must fire
 *       regardless of context (e.g. Enter to fire the focused button).</li>
 *   <li>The global {@link ShortcutRegistry}, for application-wide shortcuts.</li>
 * </ol>
 *
 * <p>Modal dialogs are isolated naturally: each is its own
 * {@link javafx.stage.Stage} with {@code APPLICATION_MODAL} modality, so global
 * shortcuts on the main scene cannot fire while a dialog has focus.</p>
 */
public final class KeyboardNavigationService {

    private final ShortcutRegistry universalRegistry = new ShortcutRegistry();
    private final ShortcutRegistry globalRegistry = new ShortcutRegistry();

    private Node boundNode;
    private ChangeListener<Scene> sceneListener;
    private Scene attachedScene;
    private EventHandler<KeyEvent> eventFilter;

    /**
     * Binds this service to the given node's scene lifecycle.
     * The listener is stored and removed when {@link #detach()} is called.
     *
     * @param node the root node whose scene changes drive attach/detach
     */
    public void bindToNode(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        boundNode = node;
        sceneListener = (obs, oldScene, newScene) -> {
            if (newScene != null) {
                attach(newScene);
            } else {
                detach();
            }
        };
        node.sceneProperty().addListener(sceneListener);
        if (node.getScene() != null) {
            attach(node.getScene());
        }
    }

    /**
     * Attaches this service to the given scene. Must be called once before
     * any shortcuts are active. Calling again with a different
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
     * Detaches this service from its current scene and clears all state,
     * including both shortcut registries.
     * Also removes the {@code sceneProperty} listener added by {@link #bindToNode}.
     * Safe to call even when not attached.
     *
     * <p><strong>Ownership:</strong> the controller that owns this service is
     * responsible for calling {@code detach()} before navigating away from the
     * screen it manages.
     * {@link #bindToNode} calls {@code detach()} automatically whenever the bound node loses its
     * scene (e.g. when the scene graph is torn down), so manual calls are only
     * needed when the controller itself drives the navigation.</p>
     */
    public void detach() {
        if (boundNode != null && sceneListener != null) {
            boundNode.sceneProperty().removeListener(sceneListener);
            boundNode = null;
            sceneListener = null;
        }
        if (attachedScene != null && eventFilter != null) {
            attachedScene.removeEventFilter(KeyEvent.KEY_PRESSED, eventFilter);
        }
        attachedScene = null;
        eventFilter = null;
        universalRegistry.clear();
        globalRegistry.clear();
    }

    /**
     * Returns the universal {@link ShortcutRegistry} for shortcuts that must
     * fire regardless of context (e.g. Enter to fire the focused button).
     *
     * @return the universal shortcut registry
     */
    public ShortcutRegistry universalShortcuts() {
        return universalRegistry;
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
     * Runs the full dispatch pipeline for the given event as if it had been
     * received from the scene's {@code EventFilter}.
     *
     * <p>Package-private so that unit tests in the same package can exercise
     * dispatch behaviour without attaching to a live {@link javafx.scene.Scene}
     * </p>
     *
     * @param event the key event to dispatch
     */
    void fireKeyEvent(KeyEvent event) {
        onKeyPressed(event);
    }

    private void onKeyPressed(KeyEvent event) {
        if (universalRegistry.dispatch(event)) {
            event.consume();
            return;
        }
        if (globalRegistry.dispatch(event)) {
            event.consume();
        }
    }
}
