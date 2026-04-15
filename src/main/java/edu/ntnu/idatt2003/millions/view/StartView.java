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
 *
 * <p>Contains a tab-based layout for either creating a new game
 * or loading an existing saved game. The new game tab includes a currency
 * selector that defaults to USD and becomes active once a stock file is chosen.
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
     *
     * <p>The currency selector in the new game tab defaults to USD and is
     * disabled until a stock file has been selected.
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
                LanguageManager.get("start.tab.newGame"), newGameContent);
        loadGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.loadGame"), loadGameContent);

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
     * Configures shared text field constraints.
     * Stock and save file fields are read-only — the user must use the browse button.
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
     * Builds the layout for the "new game" tab.
     *
     * <p>Includes name, capital, stock file input, a currency selector,
     * and a start button.
     *
     * @return the assembled layout node
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
     * Builds the layout for the "load game" tab.
     *
     * <p>Includes a save file input and a load button.
     *
     * @return the assembled layout node
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
     * Refreshes all visible UI texts from the current {@link LanguageManager} bundle.
     * Called on construction and whenever the language changes.
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
        loadButton.setText(LanguageManager.get("start.loadButton"));

        nameField.setPromptText("");
        capitalField.setPromptText("");
        stockFileField.setPromptText("");
        saveFileField.setPromptText("");
    }

    /**
     * Returns the JavaFX scene for this view.
     *
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Returns the trimmed player name entered by the user.
     *
     * @return the player name
     */
    public String getName() {
        return nameField.getText().trim();
    }

    /**
     * Returns the trimmed starting capital entered by the user.
     *
     * @return the capital as a string
     */
    public String getCapital() {
        return capitalField.getText().trim();
    }

    /**
     * Returns the path to the stock data file selected by the user.
     *
     * @return the stock file path, or an empty string if none selected
     */
    public String getStockFilePath() {
        return stockFileField.getText().trim();
    }

    /**
     * Returns the path to the save file selected by the user.
     *
     * @return the save file path, or an empty string if none selected
     */
    public String getSaveFilePath() {
        return saveFileField.getText().trim();
    }

    /**
     * Returns the button that opens the stock file browser.
     *
     * @return the browse button for stock files
     */
    public Button getBrowseStockFileButton() {
        return browseStockFileButton;
    }

    /**
     * Returns the button that starts a new game.
     *
     * @return the start button
     */
    public Button getStartButton() {
        return startButton;
    }

    /**
     * Returns the button that opens the save file browser.
     *
     * @return the browse button for save files
     */
    public Button getBrowseSaveFileButton() {
        return browseSaveFileButton;
    }

    /**
     * Returns the button that loads a saved game.
     *
     * @return the load button
     */
    public Button getLoadButton() {
        return loadButton;
    }

    /**
     * Sets the stock file path in the read-only field.
     *
     * @param path the file path to display; if {@code null} the field is cleared
     */
    public void setStockFilePath(String path) {
        stockFileField.setText(path == null ? "" : path);
    }

    /**
     * Sets the save file path in the read-only field.
     *
     * @param path the file path to display; if {@code null} the field is cleared
     */
    public void setSaveFilePath(String path) {
        saveFileField.setText(path == null ? "" : path);
    }

    /**
     * Returns the tab pane containing the new and load game tabs.
     *
     * @return the tab pane
     */
    public TabPane getTabPane() {
        return tabPane;
    }
}