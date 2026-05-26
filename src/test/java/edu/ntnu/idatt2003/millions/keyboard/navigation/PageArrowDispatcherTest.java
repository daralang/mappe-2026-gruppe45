package edu.ntnu.idatt2003.millions.keyboard.navigation;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PageArrowDispatcher}.
 */
class PageArrowDispatcherTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already running
        }
    }

    private static KeyEvent key(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                false, false, false, false);
    }

    private static void runAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX application thread timed out");
    }

    @Nested
    @DisplayName("Construction and installation")
    class Construction {

        @Test
        @DisplayName("throws NullPointerException when active scroller supplier is null")
        void throwsOnNullScrollerSupplier() {
            assertThrows(NullPointerException.class, () -> new PageArrowDispatcher(null));
        }

        @Test
        @DisplayName("throws NullPointerException when installed on a null root")
        void throwsOnNullRoot() {
            PageArrowDispatcher dispatcher = new PageArrowDispatcher(() -> code -> {});

            assertThrows(NullPointerException.class, () -> dispatcher.install(null));
        }
    }

    @Nested
    @DisplayName("Registered strategies")
    class RegisteredStrategies {

        @Test
        @DisplayName("consumes DOWN without scrolling when focused strategy handles it")
        void handledStrategyTakesTheKey() throws InterruptedException {
            AtomicBoolean handled = new AtomicBoolean(false);
            AtomicInteger scrolls = new AtomicInteger();

            runAndWait(() -> {
                Button focusOwner = new Button();
                focusOwner.getProperties().put(PageArrowDispatcher.ARROW_HANDLER_KEY,
                        (VerticalArrowHandler) event -> {
                            handled.set(true);
                            return true;
                        });
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> code -> scrolls.incrementAndGet());
                KeyEvent event = key(KeyCode.DOWN);

                dispatcher.dispatch(event, focusOwner);

                assertTrue(event.isConsumed());
            });

            assertTrue(handled.get());
            assertEquals(0, scrolls.get());
        }

        @Test
        @DisplayName("scrolls when focused strategy declines DOWN at an edge")
        void declinedStrategyFallsBackToScroll() throws InterruptedException {
            AtomicInteger handlerCalls = new AtomicInteger();
            AtomicReference<KeyCode> scrolledCode = new AtomicReference<>();

            runAndWait(() -> {
                Button focusOwner = new Button();
                focusOwner.getProperties().put(PageArrowDispatcher.ARROW_HANDLER_KEY,
                        (VerticalArrowHandler) event -> {
                            handlerCalls.incrementAndGet();
                            return false;
                        });
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> scrolledCode::set);
                KeyEvent event = key(KeyCode.DOWN);

                dispatcher.dispatch(event, focusOwner);

                assertTrue(event.isConsumed());
            });

            assertEquals(1, handlerCalls.get());
            assertEquals(KeyCode.DOWN, scrolledCode.get());
        }

        @Test
        @DisplayName("finds a registered row strategy on an ancestor of the focused child")
        void findsHandlerOnAncestor() throws InterruptedException {
            AtomicBoolean handled = new AtomicBoolean(false);

            runAndWait(() -> {
                Button focusedChild = new Button();
                StackPane rowAnchor = new StackPane(focusedChild);
                rowAnchor.getProperties().put(PageArrowDispatcher.ARROW_HANDLER_KEY,
                        (VerticalArrowHandler) event -> {
                            handled.set(true);
                            return true;
                        });
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(() -> code -> {});
                KeyEvent event = key(KeyCode.UP);

                dispatcher.dispatch(event, focusedChild);

                assertTrue(event.isConsumed());
            });

            assertTrue(handled.get());
        }
    }

    @Nested
    @DisplayName("Page scrolling")
    class PageScrolling {

        @Test
        @DisplayName("scrolls the active page for DOWN on an ordinary focused control")
        void ordinaryControlScrollsPage() throws InterruptedException {
            AtomicReference<KeyCode> scrolledCode = new AtomicReference<>();

            runAndWait(() -> {
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> scrolledCode::set);
                KeyEvent event = key(KeyCode.DOWN);

                dispatcher.dispatch(event, new Button());

                assertTrue(event.isConsumed());
            });

            assertEquals(KeyCode.DOWN, scrolledCode.get());
        }

        @Test
        @DisplayName("ignores non-vertical keys without consuming or scrolling")
        void nonVerticalKeyIsIgnored() throws InterruptedException {
            AtomicInteger scrolls = new AtomicInteger();

            runAndWait(() -> {
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> code -> scrolls.incrementAndGet());
                KeyEvent event = key(KeyCode.LEFT);

                dispatcher.dispatch(event, new Button());

                assertFalse(event.isConsumed());
            });

            assertEquals(0, scrolls.get());
        }

        @Test
        @DisplayName("leaves a vertical key unconsumed when no active page exists")
        void noActiveScrollerLeavesKeyAvailable() throws InterruptedException {
            runAndWait(() -> {
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(() -> null);
                KeyEvent event = key(KeyCode.UP);

                dispatcher.dispatch(event, new Button());

                assertFalse(event.isConsumed());
            });
        }
    }

    @Nested
    @DisplayName("Native vertical controls")
    class NativeControls {

        @Test
        @DisplayName("does not scroll or consume DOWN while a text area has focus")
        void textAreaKeepsArrowKeys() throws InterruptedException {
            assertNativeControlKeepsArrows(new TextArea());
        }

        @Test
        @DisplayName("does not scroll or consume DOWN while a combo box has focus")
        void comboBoxKeepsArrowKeys() throws InterruptedException {
            assertNativeControlKeepsArrows(new ComboBox<>());
        }

        @Test
        @DisplayName("does not scroll or consume DOWN while a spinner has focus")
        void spinnerKeepsArrowKeys() throws InterruptedException {
            assertNativeControlKeepsArrows(new Spinner<>(1, 10, 1));
        }

        @Test
        @DisplayName("recognises the editable spinner editor as part of the spinner control")
        void spinnerEditorKeepsArrowKeysThroughAncestor() throws InterruptedException {
            AtomicInteger scrolls = new AtomicInteger();

            runAndWait(() -> {
                Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
                spinner.setEditable(true);
                StackPane root = new StackPane(spinner);
                new Scene(root, 100, 100);
                root.applyCss();
                Node editor = spinner.getEditor();
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> code -> scrolls.incrementAndGet());
                KeyEvent event = key(KeyCode.DOWN);

                dispatcher.dispatch(event, editor);

                assertFalse(event.isConsumed());
            });

            assertEquals(0, scrolls.get());
        }

        private void assertNativeControlKeepsArrows(Node focusOwner) throws InterruptedException {
            AtomicInteger scrolls = new AtomicInteger();

            runAndWait(() -> {
                PageArrowDispatcher dispatcher = new PageArrowDispatcher(
                        () -> code -> scrolls.incrementAndGet());
                KeyEvent event = key(KeyCode.DOWN);

                dispatcher.dispatch(event, focusOwner);

                assertFalse(event.isConsumed());
            });

            assertEquals(0, scrolls.get());
        }
    }
}
