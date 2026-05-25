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
    @DisplayName("GameSaveCorruptException(String i18nKey, Object[] args)")
    class NoCauseConstructor {

        @Test
        @DisplayName("getI18nKey() returns the supplied key")
        void keyIsPreserved() {
            GameSaveCorruptException ex =
                    new GameSaveCorruptException("error.save.load.missingFields", new Object[]{"save.json"});
            assertEquals("error.save.load.missingFields", ex.getI18nKey());
        }

        @Test
        @DisplayName("getArgs() returns a copy of the supplied args")
        void argsArePreserved() {
            Object[] args = {"save.json"};
            GameSaveCorruptException ex = new GameSaveCorruptException("some.key", args);
            assertArrayEquals(args, ex.getArgs());
        }

        @Test
        @DisplayName("getCause() is null when no cause is supplied")
        void causeIsNullByDefault() {
            GameSaveCorruptException ex = new GameSaveCorruptException("some.key", new Object[0]);
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
    @DisplayName("GameSaveCorruptException(String i18nKey, Object[] args, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getI18nKey() returns the supplied key")
        void keyIsPreserved() {
            GameSaveCorruptException ex = new GameSaveCorruptException(
                    "error.save.load.invalidJson", new Object[]{"corrupt.json"}, new RuntimeException("root"));
            assertEquals("error.save.load.invalidJson", ex.getI18nKey());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            GameSaveCorruptException ex = new GameSaveCorruptException("some.key", new Object[0], root);
            assertSame(root, ex.getCause());
        }
    }
}
