package edu.ntnu.idatt2003.millions.util;

import javafx.scene.Scene;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for loading CSS stylesheets onto JavaFX scenes.
 *
 * <p>Centralizes stylesheet loading so views do not need to handle
 * classpath resolution themselves.
 *
 * <p>Example usage:
 * <pre>
 *   StylesheetLoader.load(scene, Stylesheet.TITLE, Stylesheet.DROP_ZONE);
 * </pre>
 */
public class StylesheetLoader {

    /**
     * Enum of all available stylesheets in the application.
     * Each constant maps to a CSS file in {@code src/main/resources/css/}.
     */
    public enum Stylesheet {
        TITLE("/css/title.css"),
        DROP_ZONE("/css/dropfile.css"),
        OTHER("/css/style.css");

        private final String path;

        Stylesheet(String path) {
            this.path = path;
        }

        /**
         * Returns the classpath path to this stylesheet.
         *
         * @return the resource path
         */
        public String getPath() {
            return path;
        }
    }

    private StylesheetLoader() {}

    /**
     * Loads the given stylesheets onto the provided scene.
     *
     * @param scene       the scene to apply the stylesheets to
     * @param stylesheets the stylesheets to load
     * @throws NullPointerException  if scene or any stylesheet is null
     * @throws IllegalStateException if a stylesheet file is not found on the classpath
     */
    public static void load(Scene scene, Stylesheet... stylesheets) {
        Objects.requireNonNull(scene, "Scene cannot be null");
        Arrays.stream(stylesheets).forEach(stylesheet -> {
            Objects.requireNonNull(stylesheet, "Stylesheet cannot be null");
            var url = StylesheetLoader.class.getResource(stylesheet.getPath());
            if (url == null) {
                throw new IllegalStateException(
                        "Stylesheet not found: " + stylesheet.getPath());
            }
            scene.getStylesheets().add(url.toExternalForm());
        });
    }
}