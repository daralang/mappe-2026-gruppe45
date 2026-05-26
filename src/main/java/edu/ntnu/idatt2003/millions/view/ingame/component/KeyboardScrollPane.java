package edu.ntnu.idatt2003.millions.view.ingame.component;

import edu.ntnu.idatt2003.millions.keyboard.navigation.PageArrowDispatcher;
import edu.ntnu.idatt2003.millions.keyboard.navigation.PageScroller;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;

/**
 * A vertically scrollable {@link ScrollPane} controlled through the
 * {@link PageScroller} contract.
 *
 * <p>{@link PageArrowDispatcher} owns vertical-arrow routing and
 * invokes this component only when the active page should scroll.
 * This component owns only viewport movement and styling.</p>
 */
public final class KeyboardScrollPane extends ScrollPane implements PageScroller {

    private static final double SCROLL_STEP_PX = 40;

    /**
     * Creates a vertically scrollable pane around {@code content} with
     * keyboard scrolling enabled.
     *
     * @param content the node to make scrollable
     */
    public KeyboardScrollPane(Node content) {
        super(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        getStyleClass().add("content-scroll");
    }

    /**
     * Scrolls the viewport one step up or down for the given arrow key.
     *
     * @param code the pressed key; only {@link KeyCode#UP} and {@link KeyCode#DOWN}
     *             have an effect
     */
    @Override
    public void scrollByArrow(KeyCode code) {
        if (code == KeyCode.UP) {
            scrollBy(-SCROLL_STEP_PX);
        } else if (code == KeyCode.DOWN) {
            scrollBy(SCROLL_STEP_PX);
        }
    }

    /**
     * Scrolls the viewport by the given pixel delta, clamped to the scrollable range.
     * A no-op when the content already fits.
     *
     * @param deltaPx pixels to scroll; negative scrolls up, positive scrolls down
     */
    private void scrollBy(double deltaPx) {
        Node content = getContent();
        if (content == null) {
            return;
        }
        double scrollable = content.getBoundsInLocal().getHeight() - getViewportBounds().getHeight();
        if (scrollable <= 0) {
            return;
        }
        double currentTop = getVvalue() * scrollable;
        setVvalue(Math.clamp((currentTop + deltaPx) / scrollable, 0, 1));
    }
}
