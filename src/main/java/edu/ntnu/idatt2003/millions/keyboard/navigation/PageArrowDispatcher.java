package edu.ntnu.idatt2003.millions.keyboard.navigation;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Single owner of the vertical arrow keys for page navigation.
 *
 * <p>Installed once on the window root, in the capture phase, so it sees every
 * {@code KEY_PRESSED} before any focused control can act. Registered
 * {@link VerticalArrowHandler} strategies take priority; otherwise controls that
 * natively use UP/DOWN, such as text areas, combo boxes and spinners, retain the
 * keys before page scrolling is considered.</p>
 */
public final class PageArrowDispatcher {

    /**
     * Property-map key under which a {@link Node} stores its {@link VerticalArrowHandler}.
     */
    public static final String ARROW_HANDLER_KEY = "millions.verticalArrowHandler";

    private final Supplier<PageScroller> activeScroller;
    private Node root;

    /**
     * Creates a dispatcher that scrolls whichever {@link PageScroller} the supplier
     * currently returns.
     *
     * @param activeScroller supplies the page to scroll; queried on each key event and
     *                       may return {@code null} when nothing is scrollable
     * @throws NullPointerException if {@code activeScroller} is null
     */
    public PageArrowDispatcher(Supplier<PageScroller> activeScroller) {
        this.activeScroller = Objects.requireNonNull(activeScroller, "activeScroller must not be null");
    }

    /**
     * Installs a capture-phase {@code KEY_PRESSED} filter on the given root so this
     * dispatcher sees arrow keys before any focused control.
     *
     * @param root the window root to listen on
     * @throws NullPointerException if {@code root} is null
     */
    public void install(Node root) {
        this.root = Objects.requireNonNull(root, "root must not be null");
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyPressed);
    }

    private void onKeyPressed(KeyEvent event) {
        dispatch(event, focusOwner());
    }

    /**
     * Routes a key event for the supplied focus owner. Package-private so unit tests
     * can exercise the dispatch policy without requiring a shown window.
     *
     * @param event      the key event to route
     * @param focusOwner the currently focused node; may be {@code null}
     */
    void dispatch(KeyEvent event, Node focusOwner) {
        KeyCode code = event.getCode();
        if (code != KeyCode.UP && code != KeyCode.DOWN) {
            return;
        }
        VerticalArrowHandler handler = findHandler(focusOwner);
        if (handler != null) {
            if (handler.handle(event)) {
                event.consume();
            } else {
                scrollPage(code, event);
            }
            return;
        }
        if (keepsArrowsNatively(focusOwner)) {
            return;
        }
        scrollPage(code, event);
    }

    private void scrollPage(KeyCode code, KeyEvent event) {
        PageScroller scroller = activeScroller.get();
        if (scroller != null) {
            scroller.scrollByArrow(code);
            event.consume();
        }
    }

    private Node focusOwner() {
        Scene scene = root == null ? null : root.getScene();
        return scene == null ? null : scene.getFocusOwner();
    }

    private static VerticalArrowHandler findHandler(Node focusOwner) {
        for (Node node = focusOwner; node != null; node = node.getParent()) {
            if (node.getProperties().get(ARROW_HANDLER_KEY) instanceof VerticalArrowHandler handler) {
                return handler;
            }
        }
        return null;
    }

    private static boolean keepsArrowsNatively(Node focusOwner) {
        for (Node node = focusOwner; node != null; node = node.getParent()) {
            if (node instanceof TextArea
                    || node instanceof ComboBoxBase<?>
                    || node instanceof Spinner<?>) {
                return true;
            }
        }
        return false;
    }
}
