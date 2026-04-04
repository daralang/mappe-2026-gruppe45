package edu.ntnu.idatt2003.millions.util;

import java.util.Locale;

/**
 * Represents a supported language in the application.
 * Each language has a display name, a flag emoji, and a locale
 * used to load the correct resource bundle.
 */
public enum Language {
    NORWEGIAN("Norsk", "/flags/no.png", Locale.of("no")),
    ENGLISH("English", "/flags/en.png", Locale.of("en"));

    public final String displayName;
    public final String flagPath;
    public final Locale locale;

    Language(String displayName, String flagPath, Locale locale) {
        this.displayName = displayName;
        this.flagPath = flagPath;
        this.locale = locale;
    }
}
