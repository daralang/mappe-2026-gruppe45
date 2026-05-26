package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.keyboard.navigation.FocusRestorer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FocusRestorer}.
 */
class FocusRestorerTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already running
        }
    }

    private FocusRestorer restorer;

    @BeforeEach
    void setUp() {
        restorer = new FocusRestorer();
    }

    /**
     * Runs the given action on the JavaFX application thread and waits for it
     * to complete, failing the test if the thread does not respond in time.
     */
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

    /**
     * Flushes any pending tasks by scheduling a no-op and waiting for it to complete.
     */
    private static void flushFxQueue() throws InterruptedException {
        runAndWait(() -> {});
    }

    /**
     * Injects node directly into the private snapshot field of
     * restorer so tests can exercise the restore logic without relying
     * on getFocusOwner returning a non-null value
     * (which requires a showing {@link javafx.stage.Stage}).
     */
    private static void injectSnapshot(FocusRestorer restorer, javafx.scene.Node node)
            throws ReflectiveOperationException {
        Field field = FocusRestorer.class.getDeclaredField("snapshot");
        field.setAccessible(true);
        field.set(restorer, node);
    }

    @Nested
    @DisplayName("snapshot()")
    class Snapshot {

        @Test
        @DisplayName("stores null when scene is null")
        void storesNullWhenSceneIsNull() {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            restorer.snapshot(null);
            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get());
        }

        @Test
        @DisplayName("stores null when scene has no focus owner")
        void storesNullWhenSceneHasNoFocusOwner() throws InterruptedException {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            runAndWait(() -> {
                Scene scene = new Scene(new StackPane(), 100, 100);
                restorer.snapshot(scene);
            });

            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get());
        }
    }

    @Nested
    @DisplayName("restore()")
    class Restore {

        @Test
        @DisplayName("calls fallback when no snapshot was taken")
        void callsFallbackWhenNoSnapshot() {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get());
        }

        @Test
        @DisplayName("does not throw when fallback returns null")
        void doesNotThrowWhenFallbackReturnsNull() {
            assertDoesNotThrow(() -> restorer.restore(() -> null));
        }

        @Test
        @DisplayName("clears snapshot after restore — second restore also calls fallback")
        void clearsSnapshotAfterRestore() {
            AtomicBoolean secondFallbackCalled = new AtomicBoolean(false);

            restorer.snapshot(null);
            restorer.restore(() -> null);
            restorer.restore(() -> {
                secondFallbackCalled.set(true);
                return null;
            });

            assertTrue(secondFallbackCalled.get());
        }

        @Test
        @DisplayName("uses snapshot node when it is still attached to a scene")
        void usesSnapshotWhenNodeIsInScene() throws Exception {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            runAndWait(() -> {
                Button button = new Button();
                new Scene(new StackPane(button), 100, 100);
                // button.getScene() is now non-null
                try {
                    injectSnapshot(restorer, button);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });

            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            flushFxQueue();

            assertFalse(fallbackCalled.get(),
                    "fallback should not be called when snapshot node is still in a scene");
        }

        @Test
        @DisplayName("uses fallback when snapshot node is no longer attached to any scene")
        void usesFallbackWhenSnapshotNodeSceneIsNull() throws Exception {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);
            Button staleButton = new Button();
            // staleButton has never been added to a scene, so getScene() == null

            injectSnapshot(restorer, staleButton);
            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get(),
                    "fallback should be called when snapshot node has no scene");
        }

        @Test
        @DisplayName("uses fallback when snapshot node is removed from its scene")
        void usesFallbackWhenSnapshotNodeIsRemovedFromScene() throws Exception {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            runAndWait(() -> {
                Button button = new Button();
                StackPane root = new StackPane(button);
                new Scene(root, 100, 100);

                // Remove button from the scene tree so button.getScene() becomes null
                root.getChildren().remove(button);

                try {
                    injectSnapshot(restorer, button);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });

            restorer.restore(() -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get(),
                    "fallback should be called when snapshot node was removed from its scene");
        }

        @Test
        @DisplayName("calls fallback exactly once when snapshot is null")
        void callsFallbackExactlyOnce() {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);
            int[] callCount = {0};

            restorer.restore(() -> {
                callCount[0]++;
                fallbackCalled.set(true);
                return null;
            });

            assertEquals(1, callCount[0]);
        }
    }

    @Nested
    @DisplayName("wrap()")
    class Wrap {

        @Test
        @DisplayName("calls rebuild during wrap")
        void callsRebuild() {
            AtomicBoolean rebuildCalled = new AtomicBoolean(false);

            restorer.wrap(null, () -> rebuildCalled.set(true), () -> null);

            assertTrue(rebuildCalled.get());
        }

        @Test
        @DisplayName("calls fallback after rebuild when scene is null")
        void callsFallbackAfterRebuildWhenSceneIsNull() {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            restorer.wrap(null, () -> {}, () -> {
                fallbackCalled.set(true);
                return null;
            });

            assertTrue(fallbackCalled.get());
        }

        @Test
        @DisplayName("calls rebuild before restore — rebuild runs between snapshot and restore")
        void rebuildsBeforeRestore() {
            int[] order = {0};
            int[] rebuildOrder = {-1};
            int[] fallbackOrder = {-1};

            restorer.wrap(null, () -> rebuildOrder[0] = order[0]++, () -> {
                fallbackOrder[0] = order[0]++;
                return null;
            });

            assertTrue(rebuildOrder[0] < fallbackOrder[0],
                    "rebuild should happen before restore/fallback");
        }

        @Test
        @DisplayName("does not throw when scene, rebuild and fallback all produce nulls")
        void doesNotThrowWithAllNulls() {
            assertDoesNotThrow(() -> restorer.wrap(null, () -> {}, () -> null));
        }

        @Test
        @DisplayName("uses snapshot node from scene when scene has a focus owner at wrap time")
        void usesSnapshotNodeFromSceneWhenFocusOwnerIsPresent() throws Exception {
            AtomicBoolean fallbackCalled = new AtomicBoolean(false);

            runAndWait(() -> {
                Button button = new Button();
                StackPane root = new StackPane(button);
                Scene scene = new Scene(root, 100, 100);

                try {
                    injectSnapshot(restorer, button);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }

                // Simulate wrap: snapshot is already set, run rebuild, then restore
                restorer.restore(() -> {
                    fallbackCalled.set(true);
                    return null;
                });
            });

            flushFxQueue();

            assertFalse(fallbackCalled.get(),
                    "fallback should not be called when snapshot node is still in a scene");
        }
    }
}
