package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
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
    private final Text title;
    private final Label nameLabel;
    private final Label capitalLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final Button startButton;

    public StartView() {
        LanguagePicker languagePicker = new LanguagePicker();

        title = new Text(LanguageManager.get("app.title"));
        nameLabel = new Label(LanguageManager.get("start.new.nameLabel"));
        capitalLabel = new Label(LanguageManager.get("start.new.capitalLabel"));
        nameField = new TextField();
        capitalField = new TextField();
        startButton = new Button(LanguageManager.get("start.startButton"));

        VBox root = new VBox(20,
                languagePicker,
                title,
                nameLabel, nameField,
                capitalLabel, capitalField,
                startButton
        );
        root.setAlignment(Pos.CENTER);

        LanguageManager.addObserver(this::updateTexts);

        scene = new Scene(root, 900, 700);
    }

    private void updateTexts() {
        title.setText(LanguageManager.get("app.title"));
        nameLabel.setText(LanguageManager.get("start.new.nameLabel"));
        capitalLabel.setText(LanguageManager.get("start.new.capitalLabel"));
        startButton.setText(LanguageManager.get("start.startButton"));
    }

    public Scene getScene() { return scene; }
    public String getName() { return nameField.getText().trim(); }
    public String getCapital() { return capitalField.getText().trim(); }
    public Button getStartButton() { return startButton; }
}