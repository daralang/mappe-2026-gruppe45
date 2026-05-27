package edu.ntnu.idatt2003.millions.keyboard;

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

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link KeyboardNavigationService}.
 */
class KeyboardNavigationServiceTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already running
        }
    }

    private KeyboardNavigationService service;

    /**
     * Initializes test fixtures before each test.
     * Test data values were generated with AI assistance and reviewed manually.
     */
    @BeforeEach
    void setUp() {
        service = new KeyboardNavigationService();
    }

    private static KeyEvent shiftKey(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                true, false, false, false);
    }

    private static KeyCombination shiftCombo(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }

    @Nested
    @DisplayName("attach()")
    class Attach {

        @Test
        @DisplayName("throws NullPointerException when scene is null")
        void throwsOnNullScene() {
            assertThrows(NullPointerException.class, () -> service.attach(null));
        }
    }

    @Nested
    @DisplayName("bindToNode()")
    class BindToNode {

        @Test
        @DisplayName("throws NullPointerException when node is null")
        void throwsOnNullNode() {
            assertThrows(NullPointerException.class, () -> service.bindToNode(null));
        }
    }

    @Nested
    @DisplayName("detach()")
    class Detach {

        @Test
        @DisplayName("can be called multiple times without throwing")
        void safeToCallMultipleTimes() {
            assertDoesNotThrow(() -> {
                service.detach();
                service.detach();
                service.detach();
            });
        }

        @Test
        @DisplayName("clears universal shortcuts so they no longer fire after detach")
        void clearsUniversalShortcuts() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));

            service.detach();
            service.fireKeyEvent(shiftKey(KeyCode.A));

            assertFalse(fired.get());
        }

        @Test
        @DisplayName("clears global shortcuts so they no longer fire after detach")
        void clearsGlobalShortcuts() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));

            service.detach();
            service.fireKeyEvent(shiftKey(KeyCode.A));

            assertFalse(fired.get());
        }
    }

    @Nested
    @DisplayName("dispatch order fireKeyEvent()")
    class DispatchOrder {

        @Test
        @DisplayName("universal shortcut fires when no context is active")
        void universalFiresWithNoContext() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));

            service.fireKeyEvent(shiftKey(KeyCode.A));

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("global shortcut fires when no context is active")
        void globalFiresWithNoContext() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> fired.set(true));

            service.fireKeyEvent(shiftKey(KeyCode.B));

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("no handler fires for a key combination that is not registered")
        void nothingFiresForUnregisteredKey() {
            AtomicBoolean universalFired = new AtomicBoolean(false);
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> universalFired.set(true));
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));

            service.fireKeyEvent(shiftKey(KeyCode.C));

            assertFalse(universalFired.get());
            assertFalse(globalFired.get());
        }

        @Test
        @DisplayName("universal and global both fire when both match on separate keys")
        void universalAndGlobalBothFire() {
            AtomicBoolean universalFired = new AtomicBoolean(false);
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> universalFired.set(true));
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));

            service.fireKeyEvent(shiftKey(KeyCode.A));
            service.fireKeyEvent(shiftKey(KeyCode.B));

            assertTrue(universalFired.get());
            assertTrue(globalFired.get());
        }

        @Test
        @DisplayName("same key registered in both universal and global: only universal fires")
        void sameKeyUniversalTakesPrecedenceOverGlobal() {
            AtomicBoolean universalFired = new AtomicBoolean(false);
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> universalFired.set(true));
            service.globalShortcuts().register(shiftCombo(KeyCode.A), () -> globalFired.set(true));

            service.fireKeyEvent(shiftKey(KeyCode.A));

            assertTrue(universalFired.get());
            assertFalse(globalFired.get());
        }
    }

    @Nested
    @DisplayName("universalShortcuts() and globalShortcuts()")
    class Registries {

        @Test
        @DisplayName("universalShortcuts() returns a non-null registry")
        void universalShortcutsNotNull() {
            assertNotNull(service.universalShortcuts());
        }

        @Test
        @DisplayName("globalShortcuts() returns a non-null registry")
        void globalShortcutsNotNull() {
            assertNotNull(service.globalShortcuts());
        }

        @Test
        @DisplayName("universalShortcuts() and globalShortcuts() are different instances")
        void universalAndGlobalAreDifferentInstances() {
            assertNotSame(service.universalShortcuts(), service.globalShortcuts());
        }

        @Test
        @DisplayName("universalShortcuts() returns the same instance on repeated calls")
        void universalShortcutsReturnsSameInstance() {
            assertSame(service.universalShortcuts(), service.universalShortcuts());
        }

        @Test
        @DisplayName("globalShortcuts() returns the same instance on repeated calls")
        void globalShortcutsReturnsSameInstance() {
            assertSame(service.globalShortcuts(), service.globalShortcuts());
        }
    }
}
