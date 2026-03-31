package edu.ntnu.idatt2003.millions.controller;

import edu.ntnu.idatt2003.millions.view.StartView;
import javafx.stage.Stage;


public class StartController {

    private final Stage stage;
    private final StartView view;

    public StartController(Stage stage) {
        this.stage = stage;
        this.view = new StartView();
        bindEvents();
    }

    private void bindEvents() {
        view.getStartButton().setOnAction(e -> handleStartGame());
    }

    private void handleStartGame() {
        // TODO: opprett Player og Exchange, start MainController. Bruk getterne fra StartView.
    }

    public void show() {
        stage.setTitle("Millions");
        stage.setScene(view.getScene());
        stage.show();
    }
}