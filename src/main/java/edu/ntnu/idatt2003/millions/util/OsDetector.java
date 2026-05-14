package edu.ntnu.idatt2003.millions.util;

/**
 * Single source of truth for OS detection. Never check {@code os.name} directly
 * outside this class.
 */
public final class OsDetector {

    private OsDetector() {}

    public static boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }
}
