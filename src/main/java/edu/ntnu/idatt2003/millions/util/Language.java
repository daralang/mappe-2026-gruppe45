package edu.ntnu.idatt2003.millions.util;

import javafx.scene.image.Image;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a supported language in the application.
 * Each language has a display name, a flag image, and a locale
 * used to load the correct resource bundle.
 *
 * <p>Note: requires JavaFX to be initialized since flag images are loaded at startup.</p>
 */
public enum Language {

    /** Norwegian language with Norwegian flag. */
    NORWEGIAN("Norsk", "/flags/no.png", Locale.of("no")),

    /** English language with British flag. */
    ENGLISH("English", "/flags/en.png", Locale.of("en"));

    /** The display name of the language, written in the language itself. */
    public final String displayName;

    /** The flag image associated with the language. */
    public final Image flag;

    /** The locale used to load the correct resource bundle. */
    public final Locale locale;

    /**
     * Constructs a Language enum constant.
     *
     * @param displayName the name of the language in its own language
     * @param flagPath    the classpath path to the flag image
     * @param locale      the locale used for resource bundle loading
     */
    Language(String displayName, String flagPath, Locale locale) {
        this.displayName = displayName;
        this.locale = locale;
        this.flag = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(flagPath))
        );
    }
}