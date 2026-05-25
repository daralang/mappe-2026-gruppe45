package edu.ntnu.idatt2003.millions.view.component.table;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

/**
 * Scrolls the enclosing {@link ScrollPane} so a focused node is brought fully
 * into the viewport.
 *
 * <p>Used by {@link SortColumnTable} when a row gains focus (via Tab), so the
 * viewport follows the focused row instead of leaving it off-screen. This class owns
 * only the scroll calculation and never touches the table grid, keeping grid ownership
 * with the table, mirroring how {@link RowHighlighter} owns only highlight state.</p>
 */
final class RowScroller {

    /**
     * Scrolls the nearest enclosing {@link ScrollPane}, if any, just far enough to bring
     * the given node fully into the viewport.
     *
     * @param node the focused node to reveal
     */
    void ensureVisible(Node node) {
        ScrollPane scrollPane = findScrollPaneAncestor(node);
        if (scrollPane == null || scrollPane.getContent() == null) {
            return;
        }
        Node content = scrollPane.getContent();
        double contentHeight = content.getBoundsInLocal().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();
        double scrollable = contentHeight - viewportHeight;
        if (scrollable <= 0) {
            return;
        }
        Bounds nodeInContent = content.sceneToLocal(node.localToScene(node.getBoundsInLocal()));
        double currentTop = scrollPane.getVvalue() * scrollable;
        double newTop = currentTop;
        if (nodeInContent.getMinY() < currentTop) {
            newTop = nodeInContent.getMinY();
        } else if (nodeInContent.getMaxY() > currentTop + viewportHeight) {
            newTop = nodeInContent.getMaxY() - viewportHeight;
        }
        scrollPane.setVvalue(Math.clamp(newTop / scrollable, 0, 1));
    }

    /**
     * Walks up the scene graph from {@code node} to the nearest {@link ScrollPane}.
     *
     * @param node the node to start from
     * @return the enclosing {@link ScrollPane}, or {@code null} if there is none
     */
    private static ScrollPane findScrollPaneAncestor(Node node) {
        Node current = node.getParent();
        while (current != null) {
            if (current instanceof ScrollPane sp) {
                return sp;
            }
            current = current.getParent();
        }
        return null;
    }
}
