package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.StylesheetLoader;
import edu.ntnu.idatt2003.millions.view.component.AppTabPane;
import edu.ntnu.idatt2003.millions.view.component.CurrencySelector;
import edu.ntnu.idatt2003.millions.view.component.FileDropZone;
import edu.ntnu.idatt2003.millions.view.component.LanguagePicker;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.Currency;

/**
 * Start view for the application.
 *
 * <p>Contains a tab-based layout for either creating a new game
 * or loading an existing saved game. The new game tab shows a centered
 * card with inline label-field rows, a {@link FileDropZone} for stock CSV
 * upload, and a currency selector that activates once a file is chosen.
 * The resume game tab shows a {@link FileDropZone} for JSON save-file upload.</p>
 */
public class StartView implements StartScreenInputs {

    private static final double SCENE_WIDTH = 900;
    private static final double SCENE_HEIGHT = 700;
    private static final double ROOT_SPACING = 24;
    private static final double FORM_SPACING = 16;
    private static final double CARD_WIDTH = 460;
    private static final double LABEL_WIDTH = 120;

    private final Scene scene;
    private final StyledText title;

    private final TabPane tabPane;
    private final Tab newGameTab;
    private final Tab loadGameTab;

    // New game tab
    private final StyledText nameLabel;
    private final StyledText capitalLabel;
    private final StyledText fileLabel;
    private final StyledText currencyLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final CurrencySelector currencySelector;
    private final FileDropZone stockFileDropZone;
    private final Button startButton;
    private String stockFilePath = "";

    // Resume game tab
    private final StyledText saveFileLabel;
    private final FileDropZone saveFileDropZone;
    private final Button loadButton;
    private String saveFilePath = "";

    /**
     * Creates the start view with two tabs:
     * one for creating a new game and one for loading a saved game.
     */
    public StartView() {
        this(null);
    }

    /**
     * Creates the start view, optionally adding a platform title bar controls node
     * at the very top (above the language picker row).
     *
     * @param titleBarControls the title bar controls node, or {@code null} to omit
     */
    public StartView(Node titleBarControls) {
        LanguagePicker languagePicker = new LanguagePicker();
        title = StyledText.headingOne(LanguageManager.get("app.title"));

        nameLabel = StyledText.paragraphOne();
        capitalLabel = StyledText.paragraphOne();
        currencyLabel = StyledText.paragraphOne();
        fileLabel = StyledText.paragraphOne();
        nameField = new TextField();
        capitalField = new TextField();
        currencySelector = new CurrencySelector();
        currencySelector.setDisable(false);

        stockFileDropZone = new FileDropZone();
        stockFileDropZone.setMaxWidth(CARD_WIDTH);
        startButton = new Button();

        saveFileLabel = StyledText.paragraphOne();
        saveFileDropZone = new FileDropZone();
        saveFileDropZone.setMaxWidth(CARD_WIDTH);
        loadButton = new Button();

        VBox newGameContent = createNewGameContent();
        VBox loadGameContent = createLoadGameContent();

        newGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.newGame"), newGameContent);
        loadGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.loadGame"), loadGameContent);

        tabPane = new AppTabPane();
        tabPane.getTabs().addAll(newGameTab, loadGameTab);
        tabPane.setMaxWidth(540);

        HBox topBar = new HBox(languagePicker);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(16, 24, 0, 24));

        VBox center = new VBox(ROOT_SPACING, title, tabPane);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(0, 24, 24, 24));

        BorderPane root = new BorderPane();
        if (titleBarControls != null) {
            root.setTop(new VBox(titleBarControls, topBar));
        } else {
            root.setTop(topBar);
        }
        root.setCenter(center);

        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        StylesheetLoader.load(scene,
                StylesheetLoader.Stylesheet.TOKENS,
                StylesheetLoader.Stylesheet.TITLE,
                StylesheetLoader.Stylesheet.DROP_ZONE,
                StylesheetLoader.Stylesheet.OTHER);

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Builds an inline label + field row where the label has a fixed width.
     *
     * @param label the label node
     * @param field the input node
     * @return an {@link HBox} with label and field on the same line
     */
    private HBox buildFormRow(javafx.scene.control.Label label, Node field) {
        label.setMinWidth(LABEL_WIDTH);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox row = new HBox(12, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(CARD_WIDTH);
        return row;
    }

    /**
     * Builds the layout for the "new game" tab.
     *
     * @return the assembled layout node
     */
    private VBox createNewGameContent() {
        HBox nameRow = buildFormRow(nameLabel, nameField);
        HBox capitalRow = buildFormRow(capitalLabel, capitalField);
        HBox currencyRow = buildFormRow(currencyLabel, currencySelector);

        startButton.setMaxWidth(CARD_WIDTH);

        VBox content = new VBox(
                FORM_SPACING,
                nameRow,
                capitalRow,
                fileLabel,
                stockFileDropZone,
                currencyRow,
                startButton
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        return content;
    }

    /**
     * Builds the layout for the "resume game" tab.
     *
     * @return the assembled layout node
     */
    private VBox createLoadGameContent() {
        loadButton.setMaxWidth(CARD_WIDTH);

        VBox content = new VBox(
                FORM_SPACING,
                saveFileLabel,
                saveFileDropZone,
                loadButton
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        return content;
    }

    /**
     * Refreshes all visible UI texts from the current {@link LanguageManager} bundle.
     */
    private void updateTexts() {
        title.setText(LanguageManager.get("app.title"));

        newGameTab.setText(LanguageManager.get("start.tab.newGame"));
        loadGameTab.setText(LanguageManager.get("start.tab.loadGame"));

        nameLabel.setText(LanguageManager.get("start.new.nameLabel"));
        capitalLabel.setText(LanguageManager.get("start.new.capitalLabel"));
        currencyLabel.setText(LanguageManager.get("start.new.currencyLabel"));
        fileLabel.setText(LanguageManager.get("start.new.fileLabel"));

        stockFileDropZone.setHintText(LanguageManager.get("start.new.dropZoneHint"));
        stockFileDropZone.setOrText(LanguageManager.get("start.new.dropZoneOr"));
        stockFileDropZone.setBrowseText(LanguageManager.get("start.file.browse"));
        startButton.setText(LanguageManager.get("start.startButton"));

        saveFileLabel.setText(LanguageManager.get("start.resume.fileLabel"));
        saveFileDropZone.setHintText(LanguageManager.get("start.resume.dropZoneHint"));
        saveFileDropZone.setOrText(LanguageManager.get("start.resume.dropZoneOr"));
        saveFileDropZone.setBrowseText(LanguageManager.get("start.file.browse"));
        loadButton.setText(LanguageManager.get("start.loadButton"));

        nameField.setPromptText("");
        capitalField.setPromptText("");
    }

    /**
     * Returns the path to the stock data file selected by the user.
     * The path is stored independently of any UI label so changes to the
     * drop-zone presentation do not affect the controller's contract.
     *
     * @return stock file path, or empty string when none is selected
     */
    @Override
    public String getStockFilePath() {
        return stockFilePath;
    }

    /**
     * Sets the stock file path. Updates the internal data field, mirrors the
     * filename in the {@link #stockFileDropZone}, and enables or disables the
     * currency selector accordingly.
     *
     * @param path the absolute file path; {@code null} or blank resets the zone
     */
    @Override
    public void setStockFilePath(String path) {
        if (path == null || path.isBlank()) {
            this.stockFilePath = "";
            stockFileDropZone.setFileName("");
            currencySelector.setDisable(true);
        } else {
            this.stockFilePath = path;
            stockFileDropZone.setFileName(path);
            currencySelector.setDisable(false);
        }
    }

    /**
     * Returns the path to the save file selected by the user.
     *
     * @return save file path, or empty string when none is selected
     */
    @Override
    public String getSaveFilePath() {
        return saveFilePath;
    }

    /**
     * Sets the save file path. Updates the internal data field and mirrors the
     * filename in the {@link #saveFileDropZone}.
     *
     * @param path save file path to display; {@code null} clears the field
     */
    @Override
    public void setSaveFilePath(String path) {
        if (path == null || path.isBlank()) {
            this.saveFilePath = "";
            saveFileDropZone.setFileName("");
        } else {
            this.saveFilePath = path;
            saveFileDropZone.setFileName(path);
        }
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
     * Sets the player name input field.
     *
     * @param name the player name to display; {@code null} clears the field
     */
    public void setName(String name) {
        nameField.setText(name == null ? "" : name);
    }

    /**
     * Returns the trimmed player name entered by the user.
     *
     * @return trimmed player name
     */
    @Override
    public String getName() {
        return nameField.getText().trim();
    }

    /**
     * Sets the starting capital input field.
     *
     * @param capital the starting capital to display; {@code null} clears the field
     */
    public void setCapital(String capital) {
        capitalField.setText(capital == null ? "" : capital);
    }

    /**
     * Returns the trimmed starting capital entered by the user.
     *
     * @return trimmed starting capital
     */
    @Override
    public String getCapital() {
        return capitalField.getText().trim();
    }

    /**
     * Returns the currency currently selected in the currency selector.
     * Used when uploading custom stock data so the controller can pass the
     * chosen currency to the {@code GameService}.
     *
     * @return the selected currency, or {@code null} if none is selected
     */
    @Override
    public Currency getSelectedCurrency() {
        return currencySelector.getValue();
    }

    /**
     * Returns the stock file drop zone so the controller can bind drag-and-drop
     * and browse handlers without depending on the internal layout structure.
     *
     * @return the {@link FileDropZone} for stock CSV upload
     */
    public FileDropZone getStockFileDropZone() {
        return stockFileDropZone;
    }

    /**
     * Returns the save file drop zone so the controller can bind drag-and-drop
     * and browse handlers for the resume game tab.
     *
     * @return the {@link FileDropZone} for JSON save-file upload
     */
    public FileDropZone getSaveFileDropZone() {
        return saveFileDropZone;
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
     * Returns the button that loads a saved game.
     *
     * @return the load button
     */
    public Button getLoadButton() {
        return loadButton;
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
