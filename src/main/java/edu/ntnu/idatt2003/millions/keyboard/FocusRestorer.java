package edu.ntnu.idatt2003.millions.keyboard;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.function.Supplier;

/**
 * Preserves and restores keyboard focus across UI rebuilds.
 */
public final class FocusRestorer {

    private Node snapshot;

    /**
     * Snapshots the currently focused node from the given scene.
     * Call this immediately before a UI rebuild.
     *
     * @param scene the scene whose focused owner to capture; may be {@code null}
     */
    public void snapshot(Scene scene) {
        snapshot = (scene != null) ? scene.getFocusOwner() : null;
    }

    /**
     * Restores focus after a rebuild. If the snapshotted node is still
     * attached to a scene, it receives focus. Otherwise {@code fallback}
     * is queried and that node receives focus instead.
     *
     * @param fallback supplier for the node to focus when the snapshot is stale;
     *                 may return {@code null}, in which case no focus is set
     */
    public void restore(Supplier<Node> fallback) {
        Node target = (snapshot != null && snapshot.getScene() != null)
                ? snapshot
                : fallback.get();
        if (target != null) {
            Platform.runLater(target::requestFocus);
        }
        snapshot = null;
    }

    /**
     * Convenience method that snapshots focus, runs a rebuild, then restores
     * focus, all in one call.
     *
     * @param scene    the scene to snapshot focus from
     * @param rebuild  the rebuild action (e.g. {@code getChildren().clear()} + repopulate)
     * @param fallback supplier for the fallback focus target after rebuild
     */
    public void wrap(Scene scene, Runnable rebuild, Supplier<Node> fallback) {
        snapshot(scene);
        rebuild.run();
        restore(fallback);
    }
}
