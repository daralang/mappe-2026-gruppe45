package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.StylesheetLoader;
import edu.ntnu.idatt2003.millions.view.component.AppTabPane;
import edu.ntnu.idatt2003.millions.view.component.CurrencySelector;
import edu.ntnu.idatt2003.millions.view.component.LanguagePicker;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * Start view for the application.
 *
 * <p>Contains a tab-based layout for either creating a new game
 * or loading an existing saved game. The new game tab shows a centered
 * card with inline label-field rows, a drag-and-drop file zone,
 * and a currency selector that activates once a file is chosen.
 */
public class StartView {

    private static final double SCENE_WIDTH = 900;
    private static final double SCENE_HEIGHT = 700;
    private static final double ROOT_SPACING = 24;
    private static final double FORM_SPACING = 16;
    private static final double CARD_WIDTH = 460;
    private static final double LABEL_WIDTH = 120;
    private static final double DROP_ZONE_HEIGHT = 160;

    private final Scene scene;
    private final StyledText title;

    private final TabPane tabPane;
    private final Tab newGameTab;
    private final Tab loadGameTab;

    // New game tab
    private final StyledText nameLabel;
    private final StyledText capitalLabel;
    private final StyledText currencyLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final CurrencySelector currencySelector;
    private final Label dropZoneHint;
    private final Label dropZoneOr;
    private final Button browseStockFileButton;
    private final Label stockFileNameLabel;
    private final Button startButton;

    // Load game tab
    private final StyledText saveFileLabel;
    private final TextField saveFileField;
    private final Button browseSaveFileButton;
    private final Button loadButton;

    /**
     * Creates the start view with two tabs:
     * one for creating a new game and one for loading a saved game.
     */
    public StartView() {
        LanguagePicker languagePicker = new LanguagePicker();
        title = StyledText.HEADING_ONE(LanguageManager.get("app.title"));

        nameLabel = StyledText.PARAGRAPH_ONE();
        capitalLabel = StyledText.PARAGRAPH_ONE();
        currencyLabel = StyledText.PARAGRAPH_ONE();
        nameField = new TextField();
        capitalField = new TextField();
        currencySelector = new CurrencySelector();
        currencySelector.setDisable(false);

        dropZoneHint = new Label();
        dropZoneOr = new Label();
        browseStockFileButton = new Button();
        stockFileNameLabel = new Label();
        stockFileNameLabel.setVisible(false);
        startButton = new Button();

        saveFileLabel = StyledText.PARAGRAPH_ONE();
        saveFileField = new TextField();
        browseSaveFileButton = new Button();
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

        // Language picker øverst til høyre
        HBox topBar = new HBox(languagePicker);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(16, 24, 0, 24));

        // Sentralt innhold
        VBox center = new VBox(ROOT_SPACING, title, tabPane);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(0, 24, 24, 24));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);

        scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT);
        StylesheetLoader.load(scene,
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
    private HBox buildFormRow(Label label, javafx.scene.Node field) {
        label.setMinWidth(LABEL_WIDTH);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox row = new HBox(12, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(CARD_WIDTH);
        return row;
    }

    /**
     * Builds the dashed drop zone for stock file upload.
     * Supports both drag-and-drop and click-to-browse.
     *
     * @return a styled {@link VBox} acting as the drop zone
     */
    private VBox buildDropZone() {
        dropZoneHint.getStyleClass().add("drop-zone-hint");
        dropZoneOr.getStyleClass().add("drop-zone-or");
        browseStockFileButton.getStyleClass().add("browse-button");

        VBox zone =
                new VBox(10, dropZoneHint, dropZoneOr, browseStockFileButton, stockFileNameLabel);
        zone.setAlignment(Pos.CENTER);
        zone.setMaxWidth(CARD_WIDTH);
        zone.setPrefHeight(DROP_ZONE_HEIGHT);
        zone.setPadding(new Insets(20));
        zone.getStyleClass().add("drop-zone");

        // Highlight on drag over
        zone.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
                zone.getStyleClass().add("drop-zone-highlight");
            }
            e.consume();
        });

        // Reset style on drag exit
        zone.setOnDragExited(_ -> zone.getStyleClass().add("drop-zone"));

        return zone;
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
        VBox dropZone = buildDropZone();

        startButton.setMaxWidth(CARD_WIDTH);

        VBox content = new VBox(
                FORM_SPACING,
                nameRow,
                capitalRow,
                currencyRow,
                dropZone,
                startButton
        );
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        return content;
    }

    /**
     * Builds the layout for the "load game" tab.
     *
     * @return the assembled layout node
     */
    private VBox createLoadGameContent() {
        saveFileField.setEditable(false);
        saveFileField.setMaxWidth(CARD_WIDTH);
        browseSaveFileButton.setMaxWidth(CARD_WIDTH);
        loadButton.setMaxWidth(CARD_WIDTH);

        VBox content = new VBox(
                FORM_SPACING,
                saveFileLabel, saveFileField,
                browseSaveFileButton,
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

        dropZoneHint.setText(LanguageManager.get("start.new.dropZoneHint"));
        dropZoneOr.setText(LanguageManager.get("start.new.dropZoneOr"));
        browseStockFileButton.setText(LanguageManager.get("start.file.browse"));
        startButton.setText(LanguageManager.get("start.startButton"));

        saveFileLabel.setText(LanguageManager.get("start.resume.fileLabel"));
        browseSaveFileButton.setText(LanguageManager.get("start.file.browse"));
        loadButton.setText(LanguageManager.get("start.loadButton"));

        nameField.setPromptText("");
        capitalField.setPromptText("");
    }

    /**
     * Returns the path to the stock data file selected by the user.
     *
     * @return stock file path, or empty string
     */
    public String getStockFilePath() {
        return stockFileNameLabel.getText().trim();
    }

    /**
     * Returns the path to the save file selected by the user.
     * Sets the stock file path, shows the filename in the drop zone,
     * and enables the currency selector.
     *
     * @param path the absolute file path; {@code null} or blank resets the zone
     */
    public void setStockFilePath(String path) {
        if (path == null || path.isBlank()) {
            stockFileNameLabel.setText("");
            stockFileNameLabel.setVisible(false);
            currencySelector.setDisable(true);
        } else {
            stockFileNameLabel.setText(path);
            stockFileNameLabel.setVisible(true);
            currencySelector.setDisable(false);
        }
    }

    /**
     * Sets the save file path in the read-only field.
     *
     * @param path save file path to display
     */
    public void setSaveFilePath(String path) {
        saveFileField.setText(path == null ? "" : path);
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
     * @return trimmed player name
     */
    public String getName() {
        return nameField.getText().trim();
    }

    /**
     * Returns the trimmed starting capital entered by the user.
     *
     * @return trimmed starting capital
     */
    public String getCapital() {
        return capitalField.getText().trim();
    }

    /**
     * @return save file path, or empty string
     *
     */
    public String getSaveFilePath() {
        return saveFileField.getText().trim();
    }

    /**
     * @return the drop zone node for drag-and-drop binding in controller
     */
    public VBox getDropZone() {
        return (VBox) browseStockFileButton.getParent();
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
     * Returns the button that opens the stock file browser.
     *
     * @return the browse button for stock files
     */
    public Button getBrowseStockFileButton() {
        return browseStockFileButton;
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
     * Returns the tab pane containing the new and load game tabs.
     *
     * @return the tab pane
     */
    public TabPane getTabPane() {
        return tabPane;
    }
}