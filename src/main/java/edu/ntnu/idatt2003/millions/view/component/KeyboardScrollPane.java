package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A vertically scrollable {@link ScrollPane} that scrolls on UP/DOWN no matter
 * which descendant currently holds keyboard focus.
 *
 * <p>JavaFX only scrolls a {@link ScrollPane} with the arrow keys when the pane
 * itself is the focus owner; when focus sits on a child, UP/DOWN trigger
 * directional focus traversal instead. This pane installs a capture-phase
 * {@link KeyEvent#KEY_PRESSED} filter so UP/DOWN scroll the viewport and are
 * consumed before traversal can move focus.</p>
 *
 * <p>A focusable node that handles UP/DOWN itself (e.g. a navigable table row)
 * can opt out of scrolling by putting {@link #VERTICAL_KEYS_HANDLED} in its
 * {@link Node#getProperties()}; this pane then leaves the keys for that node.</p>
 */
public final class KeyboardScrollPane extends ScrollPane {

    /**
     * Property key a focus owner can set to {@code Boolean.TRUE} in its
     * {@link Node#getProperties()} to signal that it handles UP/DOWN itself,
     * so this pane should not scroll on those keys while it is focused.
     */
    public static final String VERTICAL_KEYS_HANDLED = "millions.verticalKeysHandled";

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
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleVerticalScroll);
    }

    /**
     * Scrolls on UP/DOWN unless focus is in a text or combo input, consuming the
     * event so it does not fall through to focus traversal.
     *
     * @param event the key event
     */
    private void handleVerticalScroll(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != KeyCode.UP && code != KeyCode.DOWN) {
            return;
        }
        if (focusOwnerHandlesVerticalKeys()) {
            return;
        }
        scrollBy(code == KeyCode.UP ? -SCROLL_STEP_PX : SCROLL_STEP_PX);
        event.consume();
    }

    /**
     * Returns {@code true} when the current focus owner uses UP/DOWN itself, so
     * this pane should not steal the keys: text inputs, combo boxes, and any node
     * that has opted out via {@link #VERTICAL_KEYS_HANDLED}.
     *
     * @return whether the focus owner handles vertical keys
     */
    private boolean focusOwnerHandlesVerticalKeys() {
        if (getScene() == null) {
            return false;
        }
        Node focused = getScene().getFocusOwner();
        if (focused == null) {
            return false;
        }
        return focused instanceof TextInputControl
                || focused instanceof ComboBoxBase<?>
                || Boolean.TRUE.equals(focused.getProperties().get(VERTICAL_KEYS_HANDLED));
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
