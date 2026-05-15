package edu.ntnu.idatt2003.millions.model.watchlist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link WatchlistEntry} record.
 */
class WatchlistEntryTest {

    @Nested
    @DisplayName("WatchlistEntry()")
    class Constructor {

        @Test
        @DisplayName("Should create entry with valid arguments")
        void createsEntryWithValidArguments() {
            // Act
            WatchlistEntry entry = new WatchlistEntry("AAPL", 3, "buy the dip");
            // Assert
            assertEquals("AAPL", entry.symbol());
            assertEquals(3, entry.addedAtWeek());
            assertEquals("buy the dip", entry.note());
        }

        @Test
        @DisplayName("Should accept addedAtWeek of exactly 1")
        void acceptsMinimumAddedAtWeek() {
            // Act
            WatchlistEntry entry = new WatchlistEntry("AAPL", 1, "");
            // Assert
            assertEquals(1, entry.addedAtWeek());
        }

        @Test
        @DisplayName("Should normalise null note to empty string")
        void normalisesNullNoteToEmptyString() {
            // Act
            WatchlistEntry entry = new WatchlistEntry("AAPL", 1, null);
            // Assert
            assertEquals("", entry.note());
        }

        @Test
        @DisplayName("Should accept empty string note")
        void acceptsEmptyStringNote() {
            // Act
            WatchlistEntry entry = new WatchlistEntry("AAPL", 1, "");
            // Assert
            assertEquals("", entry.note());
        }

        @Test
        @DisplayName("Should throw NullPointerException when symbol is null")
        void throwsWhenSymbolIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                    new WatchlistEntry(null, 1, ""));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is blank")
        void throwsWhenSymbolIsBlank() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new WatchlistEntry("", 1, ""));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when symbol is whitespace only")
        void throwsWhenSymbolIsWhitespace() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new WatchlistEntry("   ", 1, ""));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when addedAtWeek is zero")
        void throwsWhenAddedAtWeekIsZero() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new WatchlistEntry("AAPL", 0, ""));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when addedAtWeek is negative")
        void throwsWhenAddedAtWeekIsNegative() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                    new WatchlistEntry("AAPL", -1, ""));
        }
    }

    @Nested
    @DisplayName("withNote()")
    class WithNote {

        @Test
        @DisplayName("Should return new entry with updated note")
        void returnsEntryWithUpdatedNote() {
            // Arrange
            WatchlistEntry original = new WatchlistEntry("AAPL", 2, "old note");
            // Act
            WatchlistEntry updated = original.withNote("new note");
            // Assert
            assertEquals("new note", updated.note());
        }

        @Test
        @DisplayName("Should preserve symbol and addedAtWeek")
        void preservesSymbolAndAddedAtWeek() {
            // Arrange
            WatchlistEntry original = new WatchlistEntry("TSLA", 5, "note");
            // Act
            WatchlistEntry updated = original.withNote("updated");
            // Assert
            assertEquals("TSLA", updated.symbol());
            assertEquals(5, updated.addedAtWeek());
        }

        @Test
        @DisplayName("Should normalise null note to empty string")
        void normalisesNullNoteToEmptyString() {
            // Arrange
            WatchlistEntry original = new WatchlistEntry("AAPL", 1, "some note");
            // Act
            WatchlistEntry updated = original.withNote(null);
            // Assert
            assertEquals("", updated.note());
        }

        @Test
        @DisplayName("Should not mutate original entry")
        void doesNotMutateOriginal() {
            // Arrange
            WatchlistEntry original = new WatchlistEntry("AAPL", 1, "original");
            // Act
            original.withNote("changed");
            // Assert
            assertEquals("original", original.note());
        }

        @Test
        @DisplayName("Should accept empty string as new note")
        void acceptsEmptyStringNote() {
            // Arrange
            WatchlistEntry original = new WatchlistEntry("AAPL", 1, "note");
            // Act
            WatchlistEntry updated = original.withNote("");
            // Assert
            assertEquals("", updated.note());
        }
    }
}
