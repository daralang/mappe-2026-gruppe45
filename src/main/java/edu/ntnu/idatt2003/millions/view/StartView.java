package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.AppTabPane;
import edu.ntnu.idatt2003.millions.view.component.LanguagePicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Start view for the application.
 * Contains a tab-based layout for creating a new game
 * or loading an existing saved game.
 */
public class StartView {

    private static final double SCENE_WIDTH = 900;
    private static final double SCENE_HEIGHT = 700;
    private static final double ROOT_SPACING = 24;
    private static final double FORM_SPACING = 12;
    private static final double FORM_MAX_WIDTH = 360;

    private final Scene scene;

    private final Text title;

    private final TabPane tabPane;
    private final Tab newGameTab;
    private final Tab loadGameTab;

    // New game tab
    private final Label nameLabel;
    private final Label capitalLabel;
    private final Label stockFileLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final TextField stockFileField;
    private final Button browseStockFileButton;
    private final Button startButton;

    // Load game tab
    private final Label saveFileLabel;
    private final TextField saveFileField;
    private final Button browseSaveFileButton;
    private final Button loadButton;

    /**
     * Creates the start view with two tabs:
     * one for creating a new game and one for loading a saved game.
     */
    public StartView() {
        LanguagePicker languagePicker = new LanguagePicker();

        title = new Text(LanguageManager.get("app.title"));

        nameLabel = new Label();
        capitalLabel = new Label();
        stockFileLabel = new Label();
        nameField = new TextField();
        capitalField = new TextField();
        stockFileField = new TextField();
        browseStockFileButton = new Button();
        startButton = new Button();

        saveFileLabel = new Label();
        saveFileField = new TextField();
        browseSaveFileButton = new Button();
        loadButton = new Button();

        configureTextFields();

        VBox newGameContent = createNewGameContent();
        VBox loadGameContent = createLoadGameContent();

        newGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.newGame"),
                newGameContent);
        loadGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.loadGame"),
                loadGameContent);

        tabPane = new AppTabPane();
        tabPane.getTabs().addAll(newGameTab, loadGameTab);
        tabPane.setMaxWidth(500);

        VBox root = new VBox(ROOT_SPACING, languagePicker, title, tabPane);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Configures shared text field behavior.
     */
    private void configureTextFields() {
        stockFileField.setEditable(false);
        saveFileField.setEditable(false);

        nameField.setMaxWidth(FORM_MAX_WIDTH);
        capitalField.setMaxWidth(FORM_MAX_WIDTH);
        stockFileField.setMaxWidth(FORM_MAX_WIDTH);
        saveFileField.setMaxWidth(FORM_MAX_WIDTH);
    }

    /**
     * Builds the content for the "new game" tab.
     *
     * @return the layout node for the tab
     */
    private VBox createNewGameContent() {
        VBox content = new VBox(
                FORM_SPACING,
                nameLabel, nameField,
                capitalLabel, capitalField,
                stockFileLabel, stockFileField,
                browseStockFileButton,
                startButton
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        return content;
    }

    /**
     * Builds the content for the "load game" tab.
     *
     * @return the layout node for the tab
     */
    private VBox createLoadGameContent() {
        VBox content = new VBox(
                FORM_SPACING,
                saveFileLabel, saveFileField,
                browseSaveFileButton,
                loadButton
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        return content;
    }

    /**
     * Updates all visible texts from the current resource bundle.
     */
    private void updateTexts() {
        title.setText(LanguageManager.get("app.title"));

        newGameTab.setText(LanguageManager.get("start.tab.newGame"));
        loadGameTab.setText(LanguageManager.get("start.tab.loadGame"));

        nameLabel.setText(LanguageManager.get("start.new.nameLabel"));
        capitalLabel.setText(LanguageManager.get("start.new.capitalLabel"));
        stockFileLabel.setText(LanguageManager.get("start.new.fileLabel"));
        browseStockFileButton.setText(LanguageManager.get("start.file.browse"));
        startButton.setText(LanguageManager.get("start.startButton"));

        saveFileLabel.setText(LanguageManager.get("start.resume.fileLabel"));
        browseSaveFileButton.setText(LanguageManager.get("start.file.browse"));
        loadButton.setText(LanguageManager.get("start.tab.loadGame"));

        nameField.setPromptText("");
        capitalField.setPromptText("");
        stockFileField.setPromptText("");
        saveFileField.setPromptText("");
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

    public String getStockFilePath() {
        return stockFileField.getText().trim();
    }

    public String getSaveFilePath() {
        return saveFileField.getText().trim();
    }

    public void setStockFilePath(String path) {
        stockFileField.setText(path == null ? "" : path);
    }

    public void setSaveFilePath(String path) {
        saveFileField.setText(path == null ? "" : path);
    }

    public Button getBrowseStockFileButton() {
        return browseStockFileButton;
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getBrowseSaveFileButton() {
        return browseSaveFileButton;
    }

    public Button getLoadButton() {
        return loadButton;
    }

    public TabPane getTabPane() {
        return tabPane;
    }
}