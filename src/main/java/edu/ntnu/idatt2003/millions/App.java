package edu.ntnu.idatt2003.millions;

import edu.ntnu.idatt2003.millions.controller.StartController;
import edu.ntnu.idatt2003.millions.util.OsDetector;
import edu.ntnu.idatt2003.millions.util.StylesheetLoader;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Entry point for the Millions application.
 *
 * <p>Creates a single app-lifetime {@link Scene} with transparent fill and all
 * shared stylesheets, then hands off to {@link StartController}. Both
 * {@code StartController} and {@code MainController} navigate by swapping the
 * scene root, so there is never more than one scene or one title bar on
 * screen at a time.</p>
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        if (OsDetector.isMac()) {
            stage.initStyle(StageStyle.UNIFIED);
        } else {
            stage.initStyle(StageStyle.TRANSPARENT);
        }

        Scene scene = new Scene(new StackPane(), 1920, 1080);
        if (!OsDetector.isMac()) {
            scene.setFill(Color.TRANSPARENT);
        }
        StylesheetLoader.load(scene,
                StylesheetLoader.Stylesheet.TOKENS,
                StylesheetLoader.Stylesheet.TITLE,
                StylesheetLoader.Stylesheet.DROP_ZONE,
                StylesheetLoader.Stylesheet.OTHER);
        stage.setScene(scene);
        applyWindowSize(stage);

        new StartController(stage).show();
        stage.show();
    }

    /**
     * Applies the standard window size to {@code stage}. On screens at or below
     * 1920×1080 the stage is maximised; on larger screens it is centred at a fixed
     * 1920×1080 cap so it does not expand absurdly on ultra-wide or 4K displays.
     */
    private static void applyWindowSize(Stage stage) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        if (screen.getWidth() <= 1920 && screen.getHeight() <= 1080) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(1920);
            stage.setHeight(1080);
            stage.setX(screen.getMinX() + (screen.getWidth() - 1920) / 2);
            stage.setY(screen.getMinY() + (screen.getHeight() - 1080) / 2);
        }
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        if (OsDetector.isMac()) {
            System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        }
        launch(args);
    }
}
