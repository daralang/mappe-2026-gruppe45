package edu.ntnu.idatt2003.millions.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Manages the active language for the application.
 * Use {@link #get(String)} to retrieve localized strings,
 * and {@link #setLanguage(Language)} to change the active language.
 * Default language is Norwegian.
 */
public class LanguageManager {

    /** The currently active language. Defaults to Norwegian. */
    private static Language currentLanguage = Language.NORWEGIAN;

    /** The resource bundle loaded for the current language. Reloaded when language changes. */
    private static ResourceBundle bundle =
            ResourceBundle.getBundle("i18n/messages", currentLanguage.locale);

    /** List of observers notified when the language changes. */
    private static final List<Runnable> observers = new ArrayList<>();

    private LanguageManager() {
        // Utility class - should not be instantiated
    }

    /**
     * Returns the localized string for the given key.
     *
     * @param key the property key to look up
     * @return the localized string
     * @throws NullPointerException if key is null
     * @throws java.util.MissingResourceException if the key is not found
     */
    public static String get(String key) {
        return bundle.getString(key);
    }

    /**
     * Returns the currently active language.
     *
     * @return the current language
     */
    public static Language getCurrentLanguage() {
        return currentLanguage;
    }

    /**
     * Registers an observer to be notified when the language changes.
     * The observer's {@link Runnable#run()} method will be called
     * each time the language is updated.
     *
     * @param observer the observer to register
     * @throws NullPointerException if observer is null
     */
    public static void addObserver(Runnable observer) {
        Objects.requireNonNull(observer, "Observer cannot be null");
        observers.add(observer);
    }

    /**
     * Sets the active language, reloads the resource bundle,
     * and notifies all registered observers.
     *
     * @param language the language to switch to
     * @throws NullPointerException if language is null
     */
    public static void setLanguage(Language language) {
        Objects.requireNonNull(language, "Language cannot be null");
        currentLanguage = language;
        bundle = ResourceBundle.getBundle("i18n/messages", language.locale);
        observers.forEach(Runnable::run);
    }
}