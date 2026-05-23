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

import java.lang.reflect.Method;
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

    @BeforeEach
    void setUp() {
        service = new KeyboardNavigationService();
    }

    /**
     * Invokes the private onKeyPressed method directly so dispatch
     * logic can be tested without attaching to a live {@link javafx.scene.Scene}.
     */
    private static void dispatch(KeyboardNavigationService service, KeyEvent event) {
        try {
            Method method = KeyboardNavigationService.class
                    .getDeclaredMethod("onKeyPressed", KeyEvent.class);
            method.setAccessible(true);
            method.invoke(service, event);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyEvent shiftKey(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                true, false, false, false);
    }

    private static KeyCombination shiftCombo(KeyCode code) {
        return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
    }

    /**
     * Test double that records lifecycle calls and returns a configurable value
     * from {@link #handleKeyPressed}.
     */
    private static class RecordingContext implements KeyboardContext {

        int activations = 0;
        int deactivations = 0;
        final boolean handlesEvents;

        RecordingContext(boolean handlesEvents) {
            this.handlesEvents = handlesEvents;
        }

        @Override
        public boolean handleKeyPressed(KeyEvent event) {
            return handlesEvents;
        }

        @Override
        public void onContextActivated() {
            activations++;
        }

        @Override
        public void onContextDeactivated() {
            deactivations++;
        }
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
            dispatch(service, shiftKey(KeyCode.A));

            assertFalse(fired.get());
        }

        @Test
        @DisplayName("clears global shortcuts so they no longer fire after detach")
        void clearsGlobalShortcuts() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));

            service.detach();
            dispatch(service, shiftKey(KeyCode.A));

            assertFalse(fired.get());
        }

        @Test
        @DisplayName("clears the context stack so hasActiveContext returns false after detach")
        void clearsContextStack() {
            service.pushContext(new RecordingContext(false));

            service.detach();

            assertFalse(service.hasActiveContext());
        }
    }

    @Nested
    @DisplayName("pushContext()")
    class PushContext {

        @Test
        @DisplayName("throws NullPointerException when context is null")
        void throwsOnNullContext() {
            assertThrows(NullPointerException.class, () -> service.pushContext(null));
        }

        @Test
        @DisplayName("calls onContextActivated on the pushed context")
        void callsActivatedOnPushed() {
            RecordingContext ctx = new RecordingContext(false);

            service.pushContext(ctx);

            assertEquals(1, ctx.activations);
        }

        @Test
        @DisplayName("calls onContextDeactivated on the previous top context when a new one is pushed")
        void deactivatesPreviousTopOnPush() {
            RecordingContext first = new RecordingContext(false);
            RecordingContext second = new RecordingContext(false);
            service.pushContext(first);

            service.pushContext(second);

            assertEquals(1, first.deactivations);
        }

        @Test
        @DisplayName("does not call onContextDeactivated when the stack is empty before push")
        void noDeactivationOnFirstPush() {
            RecordingContext ctx = new RecordingContext(false);

            service.pushContext(ctx);

            assertEquals(0, ctx.deactivations);
        }

        @Test
        @DisplayName("hasActiveContext returns true after push")
        void hasActiveContextAfterPush() {
            service.pushContext(new RecordingContext(false));

            assertTrue(service.hasActiveContext());
        }
    }

    @Nested
    @DisplayName("popContext()")
    class PopContext {

        @Test
        @DisplayName("throws NullPointerException when context is null")
        void throwsOnNullContext() {
            assertThrows(NullPointerException.class, () -> service.popContext(null));
        }

        @Test
        @DisplayName("calls onContextDeactivated on the popped context")
        void callsDeactivatedOnPopped() {
            RecordingContext ctx = new RecordingContext(false);
            service.pushContext(ctx);

            service.popContext(ctx);

            assertEquals(1, ctx.deactivations);
        }

        @Test
        @DisplayName("calls onContextActivated on the context below the popped one")
        void reactivatesContextBelowPopped() {
            RecordingContext first = new RecordingContext(false);
            RecordingContext second = new RecordingContext(false);
            service.pushContext(first);
            service.pushContext(second);

            service.popContext(second);

            assertEquals(2, first.activations); // once on push, once on re-activation
        }

        @Test
        @DisplayName("does not call onContextActivated on anything when popping the last context")
        void noReactivationWhenStackBecomesEmpty() {
            RecordingContext ctx = new RecordingContext(false);
            service.pushContext(ctx);

            service.popContext(ctx);

            assertEquals(1, ctx.activations); // only the initial push activation
        }

        @Test
        @DisplayName("hasActiveContext returns false when the last context is popped")
        void hasActiveContextFalseAfterLastPop() {
            RecordingContext ctx = new RecordingContext(false);
            service.pushContext(ctx);

            service.popContext(ctx);

            assertFalse(service.hasActiveContext());
        }

        @Test
        @DisplayName("is a no-op when the stack is empty")
        void noOpWhenStackIsEmpty() {
            RecordingContext ctx = new RecordingContext(false);

            assertDoesNotThrow(() -> service.popContext(ctx));
            assertEquals(0, ctx.deactivations);
        }

        @Test
        @DisplayName("is a no-op when the context is not at the top of the stack")
        void noOpWhenContextIsNotAtTop() {
            RecordingContext first = new RecordingContext(false);
            RecordingContext second = new RecordingContext(false);
            service.pushContext(first);
            service.pushContext(second);
            int deactivationsBefore = first.deactivations;

            service.popContext(first);

            assertEquals(deactivationsBefore, first.deactivations,
                    "popContext should not deactivate a context that is not at the top");
            assertTrue(service.hasActiveContext());
        }

        @Test
        @DisplayName("stack remains intact after a failed pop (wrong context)")
        void stackIntactAfterFailedPop() {
            RecordingContext first = new RecordingContext(false);
            RecordingContext second = new RecordingContext(false);
            RecordingContext outsider = new RecordingContext(false);
            service.pushContext(first);
            service.pushContext(second);

            service.popContext(outsider);

            assertEquals(1, second.activations);
            assertEquals(0, second.deactivations);
        }
    }

    @Nested
    @DisplayName("hasActiveContext()")
    class HasActiveContext {

        @Test
        @DisplayName("returns false when no context has been pushed")
        void falseInitially() {
            assertFalse(service.hasActiveContext());
        }

        @Test
        @DisplayName("returns true after a push and false after all contexts are popped")
        void trueAfterPushFalseAfterPop() {
            RecordingContext ctx = new RecordingContext(false);
            service.pushContext(ctx);
            assertTrue(service.hasActiveContext());

            service.popContext(ctx);
            assertFalse(service.hasActiveContext());
        }

        @Test
        @DisplayName("remains true when only one of two stacked contexts is popped")
        void remainsTrueAfterPartialPop() {
            RecordingContext first = new RecordingContext(false);
            RecordingContext second = new RecordingContext(false);
            service.pushContext(first);
            service.pushContext(second);

            service.popContext(second);

            assertTrue(service.hasActiveContext());
        }
    }

    @Nested
    @DisplayName("dispatch order — onKeyPressed()")
    class DispatchOrder {

        @Test
        @DisplayName("universal shortcut fires when no context is active")
        void universalFiresWithNoContext() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));

            dispatch(service, shiftKey(KeyCode.A));

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("universal shortcut fires even when a context is active")
        void universalFiresWithActiveContext() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> fired.set(true));
            service.pushContext(new RecordingContext(true));

            dispatch(service, shiftKey(KeyCode.A));

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("context does not receive the event when universal shortcut handles it first")
        void contextNotCalledWhenUniversalHandles() {
            AtomicBoolean contextCalled = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> {});
            service.pushContext(event -> {
                contextCalled.set(true);
                return true;
            });

            dispatch(service, shiftKey(KeyCode.A));

            assertFalse(contextCalled.get());
        }

        @Test
        @DisplayName("global shortcut fires when no context is active")
        void globalFiresWithNoContext() {
            AtomicBoolean fired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> fired.set(true));

            dispatch(service, shiftKey(KeyCode.B));

            assertTrue(fired.get());
        }

        @Test
        @DisplayName("global shortcut does not fire when an active context handles the event")
        void globalBlockedByHandlingContext() {
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));
            service.pushContext(new RecordingContext(true));

            dispatch(service, shiftKey(KeyCode.B));

            assertFalse(globalFired.get());
        }

        @Test
        @DisplayName("global shortcut fires when an active context passes the event through")
        void globalFiresWhenContextPassesThrough() {
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));
            service.pushContext(new RecordingContext(false));

            dispatch(service, shiftKey(KeyCode.B));

            assertTrue(globalFired.get());
        }

        @Test
        @DisplayName("no handler fires for a key combination that is not registered")
        void nothingFiresForUnregisteredKey() {
            AtomicBoolean universalFired = new AtomicBoolean(false);
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> universalFired.set(true));
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));

            dispatch(service, shiftKey(KeyCode.C));

            assertFalse(universalFired.get());
            assertFalse(globalFired.get());
        }

        @Test
        @DisplayName("only the top context receives events when multiple contexts are stacked")
        void onlyTopContextReceivesEvents() {
            AtomicBoolean bottomCalled = new AtomicBoolean(false);
            AtomicBoolean topCalled = new AtomicBoolean(false);
            service.pushContext(event -> { bottomCalled.set(true); return true; });
            service.pushContext(event -> { topCalled.set(true); return true; });

            dispatch(service, shiftKey(KeyCode.A));

            assertTrue(topCalled.get());
            assertFalse(bottomCalled.get());
        }

        @Test
        @DisplayName("bottom context receives events again after the top context is popped")
        void bottomContextReceivesEventsAfterTopPopped() {
            AtomicBoolean bottomCalled = new AtomicBoolean(false);
            KeyboardContext bottom = event -> { bottomCalled.set(true); return true; };
            KeyboardContext top = event -> true;
            service.pushContext(bottom);
            service.pushContext(top);

            service.popContext(top);
            dispatch(service, shiftKey(KeyCode.A));

            assertTrue(bottomCalled.get());
        }

        @Test
        @DisplayName("universal and global both fire when no context is active and both match")
        void universalAndGlobalBothFire() {
            AtomicBoolean universalFired = new AtomicBoolean(false);
            AtomicBoolean globalFired = new AtomicBoolean(false);
            service.universalShortcuts().register(shiftCombo(KeyCode.A), () -> universalFired.set(true));
            service.globalShortcuts().register(shiftCombo(KeyCode.B), () -> globalFired.set(true));

            dispatch(service, shiftKey(KeyCode.A));
            dispatch(service, shiftKey(KeyCode.B));

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

            dispatch(service, shiftKey(KeyCode.A));

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
