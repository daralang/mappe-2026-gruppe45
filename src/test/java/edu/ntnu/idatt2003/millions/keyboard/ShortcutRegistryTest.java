package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.keyboard.registry.ShortcutRegistry;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShortcutRegistry}.
 */
class ShortcutRegistryTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already running
        }
    }

    private ShortcutRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ShortcutRegistry();
    }

    private static KeyEvent shift(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                true, false, false, false);
    }

    private static KeyEvent plain(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                false, false, false, false);
    }

    private static KeyCodeCombination shiftCombo(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }


    @Nested
    @DisplayName("register(KeyCombination, Runnable)")
    class RegisterRunnable {

        @Test
        @DisplayName("runs the action and returns true when the combination matches")
        void dispatchesMatchingRunnable() {
            AtomicInteger count = new AtomicInteger();
            registry.register(shiftCombo(KeyCode.DIGIT1), count::incrementAndGet);

            boolean handled = registry.dispatch(shift(KeyCode.DIGIT1));

            assertTrue(handled);
            assertEquals(1, count.get());
        }

        @Test
        @DisplayName("does not run the action when a different key is pressed")
        void doesNotDispatchNonMatchingKey() {
            AtomicInteger count = new AtomicInteger();
            registry.register(shiftCombo(KeyCode.DIGIT1), count::incrementAndGet);

            boolean handled = registry.dispatch(shift(KeyCode.DIGIT2));

            assertFalse(handled);
            assertEquals(0, count.get());
        }

        @Test
        @DisplayName("does not run the action when modifier is missing")
        void doesNotDispatchWithoutModifier() {
            AtomicInteger count = new AtomicInteger();
            registry.register(shiftCombo(KeyCode.DIGIT1), count::incrementAndGet);

            boolean handled = registry.dispatch(plain(KeyCode.DIGIT1));

            assertFalse(handled);
            assertEquals(0, count.get());
        }
    }


    @Nested
    @DisplayName("register(KeyCombination, BooleanSupplier)")
    class RegisterBooleanSupplier {

        @Test
        @DisplayName("returns true when the supplier returns true")
        void returnsTrueWhenSupplierReturnsTrue() {
            registry.register(shiftCombo(KeyCode.A), () -> true);

            assertTrue(registry.dispatch(shift(KeyCode.A)));
        }

        @Test
        @DisplayName("returns false when the supplier returns false")
        void returnsFalseWhenSupplierReturnsFalse() {
            registry.register(shiftCombo(KeyCode.A), () -> false);

            assertFalse(registry.dispatch(shift(KeyCode.A)));
        }
    }


    @Nested
    @DisplayName("unregister()")
    class Unregister {

        @Test
        @DisplayName("removes a registered shortcut so it no longer fires")
        void removesExistingShortcut() {
            AtomicInteger count = new AtomicInteger();
            registry.register(shiftCombo(KeyCode.DIGIT1), count::incrementAndGet);
            registry.unregister(shiftCombo(KeyCode.DIGIT1));

            boolean handled = registry.dispatch(shift(KeyCode.DIGIT1));

            assertFalse(handled);
            assertEquals(0, count.get());
        }

        @Test
        @DisplayName("is a no-op when the combination was never registered")
        void noOpForUnknownCombination() {
            assertDoesNotThrow(() -> registry.unregister(shiftCombo(KeyCode.Z)));
        }
    }


    @Nested
    @DisplayName("clear()")
    class Clear {

        @Test
        @DisplayName("removes all shortcuts so dispatch always returns false afterwards")
        void removesAllShortcuts() {
            registry.register(shiftCombo(KeyCode.DIGIT1), () -> {});
            registry.register(shiftCombo(KeyCode.DIGIT2), () -> {});
            registry.clear();

            assertFalse(registry.dispatch(shift(KeyCode.DIGIT1)));
            assertFalse(registry.dispatch(shift(KeyCode.DIGIT2)));
        }
    }


    @Nested
    @DisplayName("registerTabShortcuts()")
    class RegisterTabShortcuts {

        @Test
        @DisplayName("Shift+1 fires the first action")
        void shift1FiresFirstAction() {
            AtomicInteger tab = new AtomicInteger(-1);
            registry.registerTabShortcuts(
                    () -> tab.set(0),
                    () -> tab.set(1),
                    () -> tab.set(2),
                    () -> tab.set(3)
            );

            registry.dispatch(shift(KeyCode.DIGIT1));

            assertEquals(0, tab.get());
        }

        @Test
        @DisplayName("Shift+4 fires the fourth action")
        void shift4FiresFourthAction() {
            AtomicInteger tab = new AtomicInteger(-1);
            registry.registerTabShortcuts(
                    () -> tab.set(0),
                    () -> tab.set(1),
                    () -> tab.set(2),
                    () -> tab.set(3)
            );

            registry.dispatch(shift(KeyCode.DIGIT4));

            assertEquals(3, tab.get());
        }

        @Test
        @DisplayName("empty varargs registers nothing")
        void emptyVarargsRegistersNothing() {
            registry.registerTabShortcuts();

            assertFalse(registry.dispatch(shift(KeyCode.DIGIT1)));
        }

        @Test
        @DisplayName("more than four actions: only the first four are registered")
        void moreThanFourActionsOnlyFirstFourRegistered() {
            AtomicInteger extra = new AtomicInteger(0);
            registry.registerTabShortcuts(
                    () -> {}, () -> {}, () -> {}, () -> {},
                    extra::incrementAndGet
            );

            // Shift+5 is not registered by registerTabShortcuts
            registry.dispatch(shift(KeyCode.DIGIT5));

            assertEquals(0, extra.get());
        }
    }


    @Nested
    @DisplayName("dispatch() — ordering")
    class DispatchOrdering {

        @Test
        @DisplayName("last registered action wins when the same combination is registered twice")
        void lastRegistrationWins() {
            AtomicInteger first = new AtomicInteger();
            AtomicInteger second = new AtomicInteger();
            registry.register(shiftCombo(KeyCode.DIGIT1), first::incrementAndGet);
            registry.register(shiftCombo(KeyCode.DIGIT1), second::incrementAndGet);

            registry.dispatch(shift(KeyCode.DIGIT1));

            assertEquals(0, first.get());
            assertEquals(1, second.get());
        }

        @Test
        @DisplayName("returns false when no shortcut is registered at all")
        void returnsFalseWithEmptyRegistry() {
            assertFalse(registry.dispatch(shift(KeyCode.A)));
        }
    }
}
