package edu.ntnu.idatt2003.millions.view.titlebar;

import edu.ntnu.idatt2003.millions.service.GameService;
import edu.ntnu.idatt2003.millions.util.OsDetector;
import javafx.stage.Stage;

public final class TitleBarFactory {

    private TitleBarFactory() {}

    public static TitleBar create(Stage stage, GameService gameService) {
        if (OsDetector.isMac()) {
            return new MacTitleBar(gameService);
        }
        return new WindowsTitleBar(stage, gameService);
    }

    public static TitleBar createForStartScreen(Stage stage) {
        if (OsDetector.isMac()) {
            return new NoOpTitleBar();
        }
        return new WindowsTitleBar(stage, "title-bar-light", false);
    }
}
