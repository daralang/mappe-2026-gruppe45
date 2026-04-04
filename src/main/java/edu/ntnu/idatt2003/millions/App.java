package edu.ntnu.idatt2003.millions;

import javafx.application.Application;
import javafx.stage.Stage;
import edu.ntnu.idatt2003.millions.controller.StartController;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        new StartController(stage).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
