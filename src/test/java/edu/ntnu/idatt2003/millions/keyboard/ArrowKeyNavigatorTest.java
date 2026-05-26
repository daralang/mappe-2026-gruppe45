package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.keyboard.navigation.ArrowKeyNavigator;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ArrowKeyNavigator}.
 */
class ArrowKeyNavigatorTest {

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

    private static ArrowKeyNavigator nav(int size, IntConsumer onSelect, IntConsumer onConfirm,
                                         boolean wrap) {
        return new ArrowKeyNavigator(() -> size, onSelect, onConfirm, wrap);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("throws NullPointerException when sizeSupplier is null")
        void throwsOnNullSizeSupplier() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(null, i -> {}, i -> {}, false));
        }

        @Test
        @DisplayName("throws NullPointerException when onSelect is null")
        void throwsOnNullOnSelect() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(() -> 1, null, i -> {}, false));
        }

        @Test
        @DisplayName("throws NullPointerException when onConfirm is null")
        void throwsOnNullOnConfirm() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(() -> 1, i -> {}, null, false));
        }

        @Test
        @DisplayName("initial selected index is 0")
        void initialIndexIsZero() {
            ArrowKeyNavigator navigator = nav(3, i -> {}, i -> {}, false);
            assertEquals(0, navigator.getSelectedIndex());
        }
    }

    @Nested
    @DisplayName("navigate() empty list (size = 0)")
    class EmptyList {

        @Test
        @DisplayName("returns false for DOWN when size is 0 and does not fire onSelect")
        void returnsFalseForDownWhenEmpty() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(0, selected::set, i -> {}, false);

            assertFalse(navigator.navigate(key(KeyCode.DOWN)));
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("returns false for UP when size is 0")
        void returnsFalseForUpWhenEmpty() {
            assertFalse(nav(0, i -> {}, i -> {}, false).navigate(key(KeyCode.UP)));
        }

        @Test
        @DisplayName("returns false for ENTER when size is 0 and does not fire onConfirm")
        void returnsFalseForEnterWhenEmpty() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(0, i -> {}, confirmed::set, false);

            assertFalse(navigator.navigate(key(KeyCode.ENTER)));
            assertEquals(-1, confirmed.get());
        }
    }

    @Nested
    @DisplayName("navigate() UP/DOWN movement")
    class Movement {

        @Test
        @DisplayName("DOWN moves selection forward by one and returns true")
        void downMovesForward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);

            boolean handled = navigator.navigate(key(KeyCode.DOWN));

            assertTrue(handled);
            assertEquals(1, navigator.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("UP moves selection backward by one and returns true")
        void upMovesBackward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            navigator.select(2);
            selected.set(-1);

            boolean handled = navigator.navigate(key(KeyCode.UP));

            assertTrue(handled);
            assertEquals(1, navigator.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("LEFT returns false and does not change selection")
        void leftIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            navigator.select(1);
            selected.set(-1);

            boolean handled = navigator.navigate(key(KeyCode.LEFT));

            assertFalse(handled);
            assertEquals(1, navigator.getSelectedIndex());
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("RIGHT returns false and does not change selection")
        void rightIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            navigator.select(1);
            selected.set(-1);

            boolean handled = navigator.navigate(key(KeyCode.RIGHT));

            assertFalse(handled);
            assertEquals(1, navigator.getSelectedIndex());
            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() — clamping (wrap = false)")
    class ClampingNoWrap {

        @Test
        @DisplayName("DOWN at last index stays at last index")
        void downAtLastIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            navigator.select(2);
            selected.set(-1);

            navigator.navigate(key(KeyCode.DOWN));

            assertEquals(2, navigator.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("UP at index 0 stays at 0")
        void upAtFirstIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            selected.set(-1);

            navigator.navigate(key(KeyCode.UP));

            assertEquals(0, navigator.getSelectedIndex());
            assertEquals(0, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() — wrapping (wrap = true)")
    class Wrapping {

        @Test
        @DisplayName("DOWN at last index wraps to 0")
        void downAtLastWrapsToFirst() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, true);
            navigator.select(2);

            navigator.navigate(key(KeyCode.DOWN));

            assertEquals(0, navigator.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("UP at index 0 wraps to last index")
        void upAtFirstWrapsToLast() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, true);

            navigator.navigate(key(KeyCode.UP));

            assertEquals(2, navigator.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("single-item list: DOWN wraps back to 0")
        void singleItemDownWraps() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(1, selected::set, i -> {}, true);

            navigator.navigate(key(KeyCode.DOWN));

            assertEquals(0, navigator.getSelectedIndex());
            assertEquals(0, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() ENTER key")
    class EnterKey {

        @Test
        @DisplayName("fires onConfirm with current index and returns true")
        void enterFiresConfirmWithCurrentIndex() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, i -> {}, confirmed::set, false);
            navigator.select(2);

            boolean handled = navigator.navigate(key(KeyCode.ENTER));

            assertTrue(handled);
            assertEquals(2, confirmed.get());
        }

        @Test
        @DisplayName("fires onConfirm with index 0 when no movement has occurred")
        void enterAtDefaultIndexFiresZero() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, i -> {}, confirmed::set, false);

            navigator.navigate(key(KeyCode.ENTER));

            assertEquals(0, confirmed.get());
        }

        @Test
        @DisplayName("does not call onSelect")
        void enterDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            selected.set(-1);

            navigator.navigate(key(KeyCode.ENTER));

            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() SPACE key")
    class SpaceKey {

        @Test
        @DisplayName("fires onConfirm with current index and returns true")
        void spaceFiresConfirmWithCurrentIndex() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, i -> {}, confirmed::set, false);
            navigator.select(2);

            boolean handled = navigator.navigate(key(KeyCode.SPACE));

            assertTrue(handled);
            assertEquals(2, confirmed.get());
        }

        @Test
        @DisplayName("does not call onSelect")
        void spaceDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            selected.set(-1);

            navigator.navigate(key(KeyCode.SPACE));

            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() unrelated keys")
    class UnrelatedKeys {

        @Test
        @DisplayName("ESCAPE returns false")
        void escapeReturnsFalse() {
            assertFalse(nav(3, i -> {}, i -> {}, false).navigate(key(KeyCode.ESCAPE)));
        }

        @Test
        @DisplayName("TAB returns false")
        void tabReturnsFalse() {
            assertFalse(nav(3, i -> {}, i -> {}, false).navigate(key(KeyCode.TAB)));
        }
    }

    @Nested
    @DisplayName("syncIndex()")
    class SyncIndex {

        @Test
        @DisplayName("updates internal index without calling onSelect")
        void syncDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);
            selected.set(-1);

            navigator.syncIndex(2);

            assertEquals(2, navigator.getSelectedIndex());
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("clamps negative value to 0")
        void clampsNegativeToZero() {
            ArrowKeyNavigator navigator = nav(3, i -> {}, i -> {}, false);

            navigator.syncIndex(-5);

            assertEquals(0, navigator.getSelectedIndex());
        }

        @Test
        @DisplayName("clamps value above last index to last index")
        void clampsOverMaxToLast() {
            ArrowKeyNavigator navigator = nav(3, i -> {}, i -> {}, false);

            navigator.syncIndex(10);

            assertEquals(2, navigator.getSelectedIndex());
        }

        @Test
        @DisplayName("is a no-op when size is 0")
        void noOpWhenEmpty() {
            ArrowKeyNavigator navigator = nav(0, i -> {}, i -> {}, false);

            navigator.syncIndex(2);

            assertEquals(0, navigator.getSelectedIndex());
        }

        @Test
        @DisplayName("subsequent navigation starts from synced index")
        void navigationStartsFromSyncedIndex() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(5, selected::set, i -> {}, false);

            navigator.syncIndex(3);
            navigator.navigate(key(KeyCode.DOWN));

            assertEquals(4, navigator.getSelectedIndex());
            assertEquals(4, selected.get());
        }
    }

    @Nested
    @DisplayName("select()")
    class Select {

        @Test
        @DisplayName("updates index and calls onSelect with new index")
        void updatesIndexAndCallsOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);

            navigator.select(2);

            assertEquals(2, navigator.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("does not call onConfirm")
        void doesNotCallOnConfirm() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, i -> {}, confirmed::set, false);

            navigator.select(2);

            assertEquals(-1, confirmed.get());
        }

        @Test
        @DisplayName("clamps negative value to 0 and calls onSelect with 0")
        void clampsNegativeToZero() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);

            navigator.select(-5);

            assertEquals(0, navigator.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("clamps value above last index to last index and calls onSelect with last index")
        void clampsOverMaxToLast() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(3, selected::set, i -> {}, false);

            navigator.select(10);

            assertEquals(2, navigator.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("is a no-op when size is 0")
        void noOpWhenEmpty() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator navigator = nav(0, selected::set, i -> {}, false);

            navigator.select(0);

            assertEquals(0, navigator.getSelectedIndex());
            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("getSelectedIndex()")
    class GetSelectedIndex {

        @Test
        @DisplayName("returns 0 before any navigation")
        void initialIndexIsZero() {
            ArrowKeyNavigator navigator = nav(3, i -> {}, i -> {}, false);
            assertEquals(0, navigator.getSelectedIndex());
        }

        @Test
        @DisplayName("reflects index after multiple DOWN presses")
        void reflectsIndexAfterMultipleDownPresses() {
            ArrowKeyNavigator navigator = nav(5, i -> {}, i -> {}, false);

            navigator.navigate(key(KeyCode.DOWN));
            navigator.navigate(key(KeyCode.DOWN));

            assertEquals(2, navigator.getSelectedIndex());
        }

        @Test
        @DisplayName("reflects index after mixed UP and DOWN navigation")
        void reflectsIndexAfterMixedNavigation() {
            ArrowKeyNavigator navigator = nav(5, i -> {}, i -> {}, false);

            navigator.navigate(key(KeyCode.DOWN));
            navigator.navigate(key(KeyCode.DOWN));
            navigator.navigate(key(KeyCode.DOWN));
            navigator.navigate(key(KeyCode.UP));

            assertEquals(2, navigator.getSelectedIndex());
        }
    }
}
