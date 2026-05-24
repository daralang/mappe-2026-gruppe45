package edu.ntnu.idatt2003.millions.view.component.table;

import edu.ntnu.idatt2003.millions.keyboard.ArrowKeyNavigator;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages hover and focus row highlighting for a {@link SortColumnTable}.
 *
 * <p>Each data row has a full-width background {@link Region} (style class
 * {@code table-row-bg}) inserted behind its cells by the table. This class owns
 * which row is highlighted and toggles the {@code is-hover} and {@code is-focus}
 * style classes. The owning table feeds it grid mouse events and registers each
 * row's background; this class never touches the grid, keeping grid ownership with
 * the table - mirroring how {@link ArrowKeyNavigator}
 * owns only navigation logic.</p>
 */
final class RowHighlighter {

    private static final String HOVER_CLASS = "is-hover";
    private static final String FOCUS_CLASS = "is-focus";

    private final Map<Integer, Region> rowBackgrounds = new HashMap<>();
    private Region hoveredRow;

    /**
     * Registers a row's background region so it can be highlighted.
     *
     * @param gridRow    the grid row the background occupies
     * @param background the full-width background region for that row
     */
    void register(int gridRow, Region background) {
        rowBackgrounds.put(gridRow, background);
    }

    /**
     * Binds an anchor's focus state to the {@code is-focus} highlight of its row.
     *
     * @param gridRow the grid row the anchor belongs to
     * @param anchor  the node focused when the row is navigated to
     */
    void bindFocus(int gridRow, Node anchor) {
        Region background = rowBackgrounds.get(gridRow);
        if (background == null) {
            return;
        }
        anchor.focusedProperty().addListener((obs, was, isFocused) -> {
            if (isFocused) {
                addClass(background, FOCUS_CLASS);
            } else {
                removeClass(background, FOCUS_CLASS);
            }
        });
    }

    /**
     * Highlights the row whose background contains the given grid-local point,
     * or clears the hover highlight if none matches.
     *
     * @param x the x coordinate within the grid
     * @param y the y coordinate within the grid
     */
    void onMouseMoved(double x, double y) {
        for (Region background : rowBackgrounds.values()) {
            if (background.getBoundsInParent().contains(x, y)) {
                hover(background);
                return;
            }
        }
        clearHover();
    }

    /** Clears the current hover highlight, if any. */
    void clearHover() {
        if (hoveredRow != null) {
            removeClass(hoveredRow, HOVER_CLASS);
            hoveredRow = null;
        }
    }

    /** Forgets all registered rows and clears hover. Call on table refresh. */
    void clear() {
        clearHover();
        rowBackgrounds.clear();
    }

    private void hover(Region background) {
        if (hoveredRow == background) {
            return;
        }
        clearHover();
        addClass(background, HOVER_CLASS);
        hoveredRow = background;
    }

    private static void addClass(Region region, String styleClass) {
        if (!region.getStyleClass().contains(styleClass)) {
            region.getStyleClass().add(styleClass);
        }
    }

    private static void removeClass(Region region, String styleClass) {
        region.getStyleClass().remove(styleClass);
    }
}
