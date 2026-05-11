package edu.ntnu.idatt2003.millions;

import edu.ntnu.idatt2003.millions.controller.MainController;
import edu.ntnu.idatt2003.millions.manager.GameManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // TODO: fjernes når StartController er ferdig
        GameManager gameManager = new GameManager();
        gameManager.loadGame(new File(
                getClass().getResource("/data/testgame.json").toURI()
        ));

        Scene scene = new Scene(new BorderPane(), 900, 700);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/tokens.css")).toExternalForm()
        );
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        stage.setScene(scene);

        new MainController(stage, gameManager).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}