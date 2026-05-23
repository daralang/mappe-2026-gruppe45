package edu.ntnu.idatt2003.millions.file.leaderboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LeaderboardCorruptException}.
 */
class LeaderboardCorruptExceptionTest {

    @Nested
    @DisplayName("LeaderboardCorruptException(String message)")
    class MessageConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            LeaderboardCorruptException ex =
                    new LeaderboardCorruptException("leaderboard JSON is not valid");
            assertEquals("leaderboard JSON is not valid", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() is null when no cause is supplied")
        void causeIsNullByDefault() {
            LeaderboardCorruptException ex = new LeaderboardCorruptException("msg");
            assertNull(ex.getCause());
        }

        @Test
        @DisplayName("is a checked exception")
        void isChecked() {
            assertTrue(Exception.class.isAssignableFrom(LeaderboardCorruptException.class));
            assertFalse(RuntimeException.class.isAssignableFrom(LeaderboardCorruptException.class));
        }
    }

    @Nested
    @DisplayName("LeaderboardCorruptException(String message, Throwable cause)")
    class ChainingConstructor {

        @Test
        @DisplayName("getMessage() returns the supplied message")
        void messageIsPreserved() {
            LeaderboardCorruptException ex =
                    new LeaderboardCorruptException("corrupt", new RuntimeException("root"));
            assertEquals("corrupt", ex.getMessage());
        }

        @Test
        @DisplayName("getCause() returns the supplied cause")
        void causeIsPreserved() {
            RuntimeException root = new RuntimeException("root");
            LeaderboardCorruptException ex = new LeaderboardCorruptException("msg", root);
            assertSame(root, ex.getCause());
        }
    }
}
