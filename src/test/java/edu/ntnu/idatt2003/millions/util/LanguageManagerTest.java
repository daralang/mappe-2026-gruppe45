package edu.ntnu.idatt2003.millions.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link LanguageManager} class.
 * <p>
 * This test class verifies the behaviour of LanguageManager, including
 * retrieving localized strings, switching languages, and notifying observers
 * on language change.
 * </p>
 * <p>
 * All tests follow the AAA pattern.
 * </p>
 */
class LanguageManagerTest {

    @BeforeEach
    void resetToNorwegian() {
        LanguageManager.setLanguage(Language.NORWEGIAN);
    }

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("Returns a non-empty string for a known key")
        void knownKey_returnsNonEmpty() {
            assertFalse(LanguageManager.get("app.title").isEmpty());
        }

        @Test
        @DisplayName("Throws MissingResourceException for unknown key")
        void unknownKey_throwsMissingResourceException() {
            assertThrows(MissingResourceException.class,
                    () -> LanguageManager.get("this.key.does.not.exist"));
        }
    }

    @Nested
    @DisplayName("setLanguage()")
    class SetLanguage {

        @Test
        @DisplayName("Changes the active language")
        void toEnglish_changesCurrentLanguage() {
            // Act
            LanguageManager.setLanguage(Language.ENGLISH);
            // Assert
            assertEquals(Language.ENGLISH, LanguageManager.getCurrentLanguage());
        }

        @Test
        @DisplayName("Actually changes returned strings")
        void toEnglish_returnsEnglishString() {
            // Arrange
            LanguageManager.setLanguage(Language.NORWEGIAN);
            String norwegian = LanguageManager.get("start.startButton");
            // Act
            LanguageManager.setLanguage(Language.ENGLISH);
            String english = LanguageManager.get("start.startButton");
            // Assert
            assertNotEquals(norwegian, english);
        }

        @Test
        @DisplayName("Throws NullPointerException for null")
        void null_throwsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> LanguageManager.setLanguage(null));
        }
    }

    @Nested
    @DisplayName("addObserver()")
    class AddObserver {

        @Test
        @DisplayName("Observer is notified when language changes")
        void languageChanges_observerIsCalled() {
            // Arrange
            AtomicInteger callCount = new AtomicInteger(0);
            LanguageManager.addObserver(callCount::incrementAndGet);
            // Act
            LanguageManager.setLanguage(Language.ENGLISH);
            // Assert
            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("Observer is not notified before language changes")
        void noChange_observerIsNotCalled() {
            // Arrange
            AtomicInteger callCount = new AtomicInteger(0);
            LanguageManager.addObserver(callCount::incrementAndGet);
            // Assert
            assertEquals(0, callCount.get());
        }

        @Test
        @DisplayName("Multiple observers are all notified on language change")
        void multipleObservers_allAreCalled() {
            // Arrange
            AtomicInteger countA = new AtomicInteger(0);
            AtomicInteger countB = new AtomicInteger(0);
            LanguageManager.addObserver(countA::incrementAndGet);
            LanguageManager.addObserver(countB::incrementAndGet);
            // Act
            LanguageManager.setLanguage(Language.ENGLISH);
            // Assert
            assertEquals(1, countA.get());
            assertEquals(1, countB.get());
        }

        @Test
        @DisplayName("Throws NullPointerException for null")
        void null_throwsNullPointerException() {
            assertThrows(NullPointerException.class,
                    () -> LanguageManager.addObserver(null));
        }
    }
}