package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.view.component.LanguagePicker;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


public class StartView {

    private final Scene scene;
    private final TextField nameField;
    private final TextField capitalField;
    private final Button startButton;

    public StartView() {
        Text title = new Text("Millions");

        LanguagePicker languagePicker = new LanguagePicker();
        nameField = new TextField();
        capitalField = new TextField();
        startButton = new Button("START SPILLET");

        VBox root = new VBox(20,
                languagePicker,
                new Label("Navn:"), nameField,
                new Label("Startkapital:"), capitalField,
                startButton
        );
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 900, 700);
    }

    public Scene getScene() {
        return scene;
    }

    public String getName() {
        return nameField.getText().trim();
    }

    public String getCapital() {
        return capitalField.getText().trim();
    }

    public Button getStartButton() {
        return startButton;
    }
}