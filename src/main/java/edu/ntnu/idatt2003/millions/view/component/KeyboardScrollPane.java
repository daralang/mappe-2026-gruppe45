package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.keyboard.PageScroller;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A vertically scrollable {@link ScrollPane} that scrolls on UP/DOWN no matter
 * which descendant currently holds keyboard focus.
 *
 * <p>JavaFX only scrolls a {@link ScrollPane} with the arrow keys when the pane
 * itself is the focus owner; when focus sits on a child, UP/DOWN otherwise trigger
 * directional focus traversal. This pane installs a bubble-phase
 * {@link KeyEvent#KEY_PRESSED} handler: the focused node gets the event first, so a
 * control that uses UP/DOWN itself (a navigable table row, a combo box, a text
 * area) can consume it before it reaches this pane. Any UP/DOWN that is left
 * unconsumed bubbles up here, scrolls the viewport, and is consumed so it never
 * falls through to focus traversal.</p>
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
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleVerticalScroll);
    }

    /**
     * Scrolls the viewport on any UP/DOWN that bubbled up unconsumed, and consumes
     * the event so it does not fall through to focus traversal.
     *
     * @param event the key event
     */
    private void handleVerticalScroll(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != KeyCode.UP && code != KeyCode.DOWN) {
            return;
        }
        scrollByArrow(code);
        event.consume();
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
