package edu.ntnu.idatt2003.millions.file.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GameSaveCorruptException}.
 */
class GameSaveCorruptExceptionTest {

    @Nested
    @DisplayName("GameSaveCorruptException(String message)")
    class MessageConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            GameSaveCorruptException ex =
                    new GameSaveCorruptException("save file is missing 'player' field");
            assertEquals("save file is missing 'player' field", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() is null when no cause is supplied")
        void causeIsNullByDefault() {
            GameSaveCorruptException ex = new GameSaveCorruptException("msg");
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(GameSaveCorruptException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(GameSaveCorruptException.class));
        }
    }

    @Nested
    @DisplayName("GameSaveCorruptException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            GameSaveCorruptException ex =
                    new GameSaveCorruptException("corrupt", new RuntimeException("root"));
            assertEquals("corrupt", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            GameSaveCorruptException ex = new GameSaveCorruptException("msg", root);
            assertSame(root, ex.getCause());
        }
    }
}
