package edu.ntnu.idatt2003.millions.keyboard;

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

    private static ArrowKeyNavigator vertical(int size, IntConsumer onSelect, IntConsumer onConfirm,
                                              boolean wrap) {
        return new ArrowKeyNavigator(
                ArrowKeyNavigator.Orientation.VERTICAL,
                () -> size,
                onSelect,
                onConfirm,
                wrap);
    }

    private static ArrowKeyNavigator horizontal(int size, IntConsumer onSelect, IntConsumer onConfirm,
                                                boolean wrap) {
        return new ArrowKeyNavigator(
                ArrowKeyNavigator.Orientation.HORIZONTAL,
                () -> size,
                onSelect,
                onConfirm,
                wrap);
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("throws NullPointerException when orientation is null")
        void throwsOnNullOrientation() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(null, () -> 1, i -> {}, i -> {}, false));
        }

        @Test
        @DisplayName("throws NullPointerException when sizeSupplier is null")
        void throwsOnNullSizeSupplier() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(
                            ArrowKeyNavigator.Orientation.VERTICAL, null, i -> {}, i -> {}, false));
        }

        @Test
        @DisplayName("throws NullPointerException when onSelect is null")
        void throwsOnNullOnSelect() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(
                            ArrowKeyNavigator.Orientation.VERTICAL, () -> 1, null, i -> {}, false));
        }

        @Test
        @DisplayName("throws NullPointerException when onConfirm is null")
        void throwsOnNullOnConfirm() {
            assertThrows(NullPointerException.class, () ->
                    new ArrowKeyNavigator(
                            ArrowKeyNavigator.Orientation.VERTICAL, () -> 1, i -> {}, null, false));
        }

        @Test
        @DisplayName("initial selected index is 0")
        void initialIndexIsZero() {
            ArrowKeyNavigator nav = vertical(3, i -> {}, i -> {}, false);
            assertEquals(0, nav.getSelectedIndex());
        }
    }

    @Nested
    @DisplayName("navigate() empty list (size = 0)")
    class EmptyList {

        @Test
        @DisplayName("returns false for DOWN when size is 0")
        void returnsFalseForDownWhenEmpty() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(0, selected::set, i -> {}, false);

            assertFalse(nav.navigate(key(KeyCode.DOWN)));
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("returns false for UP when size is 0")
        void returnsFalseForUpWhenEmpty() {
            assertFalse(vertical(0, i -> {}, i -> {}, false).navigate(key(KeyCode.UP)));
        }

        @Test
        @DisplayName("returns false for ENTER when size is 0 and does not fire onConfirm")
        void returnsFalseForEnterWhenEmpty() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(0, i -> {}, confirmed::set, false);

            assertFalse(nav.navigate(key(KeyCode.ENTER)));
            assertEquals(-1, confirmed.get());
        }

        @Test
        @DisplayName("returns false for LEFT when size is 0 (horizontal)")
        void returnsFalseForLeftWhenEmpty() {
            assertFalse(horizontal(0, i -> {}, i -> {}, false).navigate(key(KeyCode.LEFT)));
        }

        @Test
        @DisplayName("returns false for RIGHT when size is 0 (horizontal)")
        void returnsFalseForRightWhenEmpty() {
            assertFalse(horizontal(0, i -> {}, i -> {}, false).navigate(key(KeyCode.RIGHT)));
        }
    }

    @Nested
    @DisplayName("navigate() VERTICAL orientation")
    class VerticalOrientation {

        @Test
        @DisplayName("DOWN moves selection forward by one and returns true")
        void downMovesForward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);

            boolean handled = nav.navigate(key(KeyCode.DOWN));

            assertTrue(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("UP moves selection backward by one and returns true")
        void upMovesBackward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            nav.select(2);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.UP));

            assertTrue(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("LEFT returns false and does not change selection")
        void leftIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            nav.select(1);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.LEFT));

            assertFalse(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("RIGHT returns false and does not change selection")
        void rightIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            nav.select(1);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.RIGHT));

            assertFalse(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() HORIZONTAL orientation")
    class HorizontalOrientation {

        @Test
        @DisplayName("RIGHT moves selection forward by one and returns true")
        void rightMovesForward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);

            boolean handled = nav.navigate(key(KeyCode.RIGHT));

            assertTrue(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("LEFT moves selection backward by one and returns true")
        void leftMovesBackward() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);
            nav.select(2);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.LEFT));

            assertTrue(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(1, selected.get());
        }

        @Test
        @DisplayName("DOWN returns false and does not change selection")
        void downIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);
            nav.select(1);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.DOWN));

            assertFalse(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("UP returns false and does not change selection")
        void upIsIgnored() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);
            nav.select(1);
            selected.set(-1);

            boolean handled = nav.navigate(key(KeyCode.UP));

            assertFalse(handled);
            assertEquals(1, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() — clamping (wrap = false)")
    class ClampingNoWrap {

        @Test
        @DisplayName("DOWN at last index stays at last index (vertical)")
        void downAtLastIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            nav.select(2);
            selected.set(-1);

            nav.navigate(key(KeyCode.DOWN));

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("UP at index 0 stays at 0 (vertical)")
        void upAtFirstIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            selected.set(-1);

            nav.navigate(key(KeyCode.UP));

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("RIGHT at last index stays at last index (horizontal)")
        void rightAtLastIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);
            nav.select(2);
            selected.set(-1);

            nav.navigate(key(KeyCode.RIGHT));

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("LEFT at index 0 stays at 0 (horizontal)")
        void leftAtFirstIndexClamped() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, false);
            selected.set(-1);

            nav.navigate(key(KeyCode.LEFT));

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(0, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() — wrapping (wrap = true)")
    class Wrapping {

        @Test
        @DisplayName("DOWN at last index wraps to 0 (vertical)")
        void downAtLastWrapsToFirst() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, true);
            nav.select(2);

            nav.navigate(key(KeyCode.DOWN));

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("UP at index 0 wraps to last index (vertical)")
        void upAtFirstWrapsToLast() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, true);

            nav.navigate(key(KeyCode.UP));

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("RIGHT at last index wraps to 0 (horizontal)")
        void rightAtLastWrapsToFirst() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, true);
            nav.select(2);

            nav.navigate(key(KeyCode.RIGHT));

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("LEFT at index 0 wraps to last index (horizontal)")
        void leftAtFirstWrapsToLast() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = horizontal(3, selected::set, i -> {}, true);

            nav.navigate(key(KeyCode.LEFT));

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("single-item list: DOWN wraps back to 0")
        void singleItemDownWraps() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(1, selected::set, i -> {}, true);

            nav.navigate(key(KeyCode.DOWN));

            assertEquals(0, nav.getSelectedIndex());
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
            ArrowKeyNavigator nav = vertical(3, i -> {}, confirmed::set, false);
            nav.select(2);

            boolean handled = nav.navigate(key(KeyCode.ENTER));

            assertTrue(handled);
            assertEquals(2, confirmed.get());
        }

        @Test
        @DisplayName("fires onConfirm with index 0 when no movement has occurred")
        void enterAtDefaultIndexFiresZero() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, i -> {}, confirmed::set, false);

            nav.navigate(key(KeyCode.ENTER));

            assertEquals(0, confirmed.get());
        }

        @Test
        @DisplayName("does not call onSelect")
        void enterDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            selected.set(-1);

            nav.navigate(key(KeyCode.ENTER));

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
            ArrowKeyNavigator nav = vertical(3, i -> {}, confirmed::set, false);
            nav.select(2);

            boolean handled = nav.navigate(key(KeyCode.SPACE));

            assertTrue(handled);
            assertEquals(2, confirmed.get());
        }

        @Test
        @DisplayName("does not call onSelect")
        void spaceDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            selected.set(-1);

            nav.navigate(key(KeyCode.SPACE));

            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("navigate() unrelated keys")
    class UnrelatedKeys {

        @Test
        @DisplayName("ESCAPE returns false")
        void escapeReturnsFalse() {
            assertFalse(vertical(3, i -> {}, i -> {}, false).navigate(key(KeyCode.ESCAPE)));
        }

        @Test
        @DisplayName("TAB returns false")
        void tabReturnsFalse() {
            assertFalse(vertical(3, i -> {}, i -> {}, false).navigate(key(KeyCode.TAB)));
        }
    }

    @Nested
    @DisplayName("syncIndex()")
    class SyncIndex {

        @Test
        @DisplayName("updates internal index without calling onSelect")
        void syncDoesNotCallOnSelect() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);
            selected.set(-1);

            nav.syncIndex(2);

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }

        @Test
        @DisplayName("clamps negative value to 0")
        void clampsNegativeToZero() {
            ArrowKeyNavigator nav = vertical(3, i -> {}, i -> {}, false);

            nav.syncIndex(-5);

            assertEquals(0, nav.getSelectedIndex());
        }

        @Test
        @DisplayName("clamps value above last index to last index")
        void clampsOverMaxToLast() {
            ArrowKeyNavigator nav = vertical(3, i -> {}, i -> {}, false);

            nav.syncIndex(10);

            assertEquals(2, nav.getSelectedIndex());
        }

        @Test
        @DisplayName("is a no-op when size is 0")
        void noOpWhenEmpty() {
            ArrowKeyNavigator nav = vertical(0, i -> {}, i -> {}, false);

            nav.syncIndex(2);

            assertEquals(0, nav.getSelectedIndex());
        }

        @Test
        @DisplayName("subsequent navigation starts from synced index")
        void navigationStartsFromSyncedIndex() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(5, selected::set, i -> {}, false);

            nav.syncIndex(3);
            nav.navigate(key(KeyCode.DOWN));

            assertEquals(4, nav.getSelectedIndex());
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
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);

            nav.select(2);

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("does not call onConfirm")
        void doesNotCallOnConfirm() {
            AtomicInteger confirmed = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, i -> {}, confirmed::set, false);

            nav.select(2);

            assertEquals(-1, confirmed.get());
        }

        @Test
        @DisplayName("clamps negative value to 0 and calls onSelect with 0")
        void clampsNegativeToZero() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);

            nav.select(-5);

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(0, selected.get());
        }

        @Test
        @DisplayName("clamps value above last index to last index and calls onSelect with last index")
        void clampsOverMaxToLast() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(3, selected::set, i -> {}, false);

            nav.select(10);

            assertEquals(2, nav.getSelectedIndex());
            assertEquals(2, selected.get());
        }

        @Test
        @DisplayName("is a no-op when size is 0")
        void noOpWhenEmpty() {
            AtomicInteger selected = new AtomicInteger(-1);
            ArrowKeyNavigator nav = vertical(0, selected::set, i -> {}, false);

            nav.select(0);

            assertEquals(0, nav.getSelectedIndex());
            assertEquals(-1, selected.get());
        }
    }

    @Nested
    @DisplayName("getSelectedIndex()")
    class GetSelectedIndex {

        @Test
        @DisplayName("returns 0 before any navigation")
        void initialIndexIsZero() {
            ArrowKeyNavigator nav = vertical(3, i -> {}, i -> {}, false);
            assertEquals(0, nav.getSelectedIndex());
        }

        @Test
        @DisplayName("reflects index after multiple DOWN presses")
        void reflectsIndexAfterMultipleDownPresses() {
            ArrowKeyNavigator nav = vertical(5, i -> {}, i -> {}, false);

            nav.navigate(key(KeyCode.DOWN));
            nav.navigate(key(KeyCode.DOWN));

            assertEquals(2, nav.getSelectedIndex());
        }

        @Test
        @DisplayName("reflects index after mixed UP and DOWN navigation")
        void reflectsIndexAfterMixedNavigation() {
            ArrowKeyNavigator nav = vertical(5, i -> {}, i -> {}, false);

            nav.navigate(key(KeyCode.DOWN));
            nav.navigate(key(KeyCode.DOWN));
            nav.navigate(key(KeyCode.DOWN));
            nav.navigate(key(KeyCode.UP));

            assertEquals(2, nav.getSelectedIndex());
        }
    }
}
