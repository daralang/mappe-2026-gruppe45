package edu.ntnu.idatt2003.millions.keyboard;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.util.function.Supplier;

/**
 * Preserves and restores keyboard focus across UI rebuilds.
 *
 * <p>After a {@code getChildren().clear()} the previously focused node is
 * detached from the scene and loses focus. Use {@link #wrap} to snapshot
 * focus before the rebuild and restore it afterward:</p>
 *
 * <p>If the focused node survives the rebuild (i.e. it is still part of a
 * scene), focus is restored to that exact node. Otherwise the fallback
 * supplier is used. The focus request is deferred with
 * {@link Platform#runLater} so it runs after the layout pass.</p>
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
     * focus — all in one call.
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
