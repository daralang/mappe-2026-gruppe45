package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.util.OsDetector;
import javafx.stage.Stage;

/**
 * Selects and constructs the platform-appropriate {@link TitleBar} implementation.
 *
 * <p>The stage style ({@code TRANSPARENT} / {@code UNIFIED}) is configured once
 * at JVM startup in {@code App.start()} using {@link OsDetector}. This factory
 * only decides which visual variant to return — it never touches the stage style.</p>
 */
public final class TitleBarFactory {

    private TitleBarFactory() {}

    /**
     * Returns the platform-appropriate {@link TitleBar}.
     * May be called each time a new {@code MainController} is created.
     *
     * @param stage the primary stage, passed to {@link WindowsTitleBar} for
     *              drag / minimize / maximize / close wiring
     * @return a {@link MacTitleBar} on macOS, a {@link WindowsTitleBar} otherwise
     */
    public static TitleBar create(Stage stage) {
        if (OsDetector.isMac()) {
            return new MacTitleBar();
        }
        return new WindowsTitleBar(stage);
    }

    /**
     * Returns a title bar for the start screen.
     * On Windows/Linux: a transparent controls-only strip (no nav header).
     * On macOS: a no-op (the OS supplies native traffic-light controls).
     *
     * @param stage the primary stage
     * @return the platform-appropriate title bar for the start screen
     */
    public static TitleBar createForStartScreen(Stage stage) {
        if (OsDetector.isMac()) {
            return new NoOpTitleBar();
        }
        return new WindowsTitleBar(stage, "title-bar-light", false);
    }
}
