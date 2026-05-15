package edu.ntnu.idatt2003.millions;

import javafx.application.Application;
import javafx.stage.Stage;
import edu.ntnu.idatt2003.millions.controller.StartController;

/**
 * Entry point for the Millions application.
 *
 * <p>Extends {@link Application} and delegates startup to
 * {@link StartController}, which builds and displays the start screen.</p>
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        new StartController(stage).show();
    }

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}