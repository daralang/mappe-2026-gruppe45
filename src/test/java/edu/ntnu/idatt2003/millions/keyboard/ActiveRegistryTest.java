package edu.ntnu.idatt2003.millions.keyboard;

import edu.ntnu.idatt2003.millions.keyboard.registry.ActiveRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ActiveRegistry}.
 */
class ActiveRegistryTest {

    private ActiveRegistry<Runnable> registry;

    @BeforeEach
    void setUp() {
        registry = new ActiveRegistry<>();
    }


    @Nested
    @DisplayName("ifActive()")
    class IfActive {

        @Test
        @DisplayName("invokes the action on the active provider")
        void invokesActionOnActiveProvider() {
            AtomicInteger count = new AtomicInteger();
            registry.setActive(count::incrementAndGet);

            registry.ifActive(Runnable::run);

            assertEquals(1, count.get());
        }

        @Test
        @DisplayName("does nothing when no provider is active")
        void doesNothingWhenNoProviderIsActive() {
            AtomicInteger count = new AtomicInteger();

            registry.ifActive(r -> count.incrementAndGet());

            assertEquals(0, count.get());
        }

        @Test
        @DisplayName("does nothing after active provider is cleared with null")
        void doesNothingAfterProviderClearedWithNull() {
            AtomicInteger count = new AtomicInteger();
            registry.setActive(count::incrementAndGet);
            registry.setActive(null);

            registry.ifActive(Runnable::run);

            assertEquals(0, count.get());
        }
    }


    @Nested
    @DisplayName("setActive()")
    class SetActive {

        @Test
        @DisplayName("replaces the previous provider when a new one is registered")
        void replacesExistingProvider() {
            AtomicInteger first = new AtomicInteger();
            AtomicInteger second = new AtomicInteger();
            registry.setActive(first::incrementAndGet);
            registry.setActive(second::incrementAndGet);

            registry.ifActive(Runnable::run);

            assertEquals(0, first.get());
            assertEquals(1, second.get());
        }
    }
}
