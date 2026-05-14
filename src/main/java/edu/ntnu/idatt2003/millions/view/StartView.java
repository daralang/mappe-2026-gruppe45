package edu.ntnu.idatt2003.millions.view;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.util.StylesheetLoader;
import edu.ntnu.idatt2003.millions.view.component.AppTabPane;
import edu.ntnu.idatt2003.millions.view.component.LanguagePicker;
import edu.ntnu.idatt2003.millions.view.start.LoadGameTab;
import edu.ntnu.idatt2003.millions.view.start.NewGameTab;
import edu.ntnu.idatt2003.millions.view.start.StartLayoutAnimator;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Currency;
import java.util.function.Consumer;

/**
 * Start view for the application.
 *
 * <p>Composes a tab-based layout from {@link NewGameTab} and {@link LoadGameTab},
 * each of which owns its own fields, layout, i18n updates, and event wiring.
 * This class is a thin shell: it builds the scene structure, delegates all
 * user input and callbacks to the tab components, and exposes the
 * {@link StartScreenInputs} contract to the controller. The title and start
 * card sit inside a responsive frame that is centered on the screen and
 * top-anchored internally. Responsive sizing and the animated {@link NewGameTab}
 * reveal are delegated to {@link StartLayoutAnimator}.</p>
 *
 * <p>User interactions are forwarded to the controller via callback setters such as
 * {@link #setOnStartGame(Runnable)} and {@link #setOnStockFileDrop(Consumer)},
 * which in turn delegate to the relevant tab component.</p>
 */
public class StartView implements StartScreenInputs {

    private static final double SCENE_WIDTH = 900;
    private static final double SCENE_HEIGHT = 700;
    private static final double ROOT_SPACING = 24;
    private static final double START_CARD_WIDTH = 540;

    private final Scene scene;
    private final StyledText title;
    private final Tab newGameTab;
    private final Tab loadGameTab;
    private final NewGameTab newGameTabContent;
    private final LoadGameTab loadGameContent;

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

        newGameTabContent = new NewGameTab();
        loadGameContent = new LoadGameTab();

        newGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.newGame"), newGameTabContent);
        loadGameTab = AppTabPane.createTab(
                LanguageManager.get("start.tab.loadGame"), loadGameContent);

        AppTabPane tabPane = new AppTabPane();
        tabPane.getTabs().addAll(newGameTab, loadGameTab);
        tabPane.setMaxWidth(START_CARD_WIDTH);
        tabPane.getStyleClass().add("start-tab-pane");

        HBox topBar = new HBox(languagePicker);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(16, 24, 0, 24));

        VBox startGroup = new VBox(ROOT_SPACING, title, tabPane);
        startGroup.setAlignment(Pos.TOP_CENTER);

        StackPane reservedStartGroup = new StackPane(startGroup);
        reservedStartGroup.setAlignment(Pos.TOP_CENTER);

        StackPane center = new StackPane(reservedStartGroup);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(0, 24, 24, 24));
        StartLayoutAnimator.bindStartCardLayout(
                tabPane,
                center,
                reservedStartGroup,
                newGameTabContent,
                loadGameContent,
                START_CARD_WIDTH,
                ROOT_SPACING);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("start-root");

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
     * Refreshes the title and tab labels from the current {@link LanguageManager} bundle.
     * Each tab component manages its own internal texts independently.
     */
    private void updateTexts() {
        title.setText(LanguageManager.get("app.title"));
        newGameTab.setText(LanguageManager.get("start.tab.newGame"));
        loadGameTab.setText(LanguageManager.get("start.tab.loadGame"));
    }

    /**
     * Returns the JavaFX scene for this view.
     *
     * @return the scene
     */
    public Scene getScene() {
        return scene;
    }


    @Override
    public String getName() {
        return newGameTabContent.getName();
    }

    @Override
    public String getCapital() {
        return newGameTabContent.getCapital();
    }

    @Override
    public Currency getSelectedCurrency() {
        return newGameTabContent.getSelectedCurrency();
    }

    @Override
    public String getStockFilePath() {
        return newGameTabContent.getFilePath();
    }

    @Override
    public void setStockFilePath(String path) {
        newGameTabContent.setFilePath(path);
    }

    @Override
    public String getSaveFilePath() {
        return loadGameContent.getFilePath();
    }

    @Override
    public void setSaveFilePath(String path) {
        loadGameContent.setFilePath(path);
    }

    /**
     * Registers the callback invoked when the user clicks the start button.
     *
     * @param callback the action to run on start; {@code null} disables the handler
     */
    public void setOnStartGame(Runnable callback) {
        newGameTabContent.setOnAction(callback);
    }

    /**
     * Registers the callback invoked when the user clicks the load button.
     *
     * @param callback the action to run on load; {@code null} disables the handler
     */
    public void setOnLoadGame(Runnable callback) {
        loadGameContent.setOnAction(callback);
    }

    /**
     * Registers the callback invoked when the user clicks the browse button
     * on the stock file drop zone.
     *
     * @param callback the action to run on browse; {@code null} disables the handler
     */
    public void setOnBrowseStockFile(Runnable callback) {
        newGameTabContent.setOnBrowse(callback);
    }

    /**
     * Registers the callback invoked when the user clicks the browse button
     * on the save file drop zone.
     *
     * @param callback the action to run on browse; {@code null} disables the handler
     */
    public void setOnBrowseSaveFile(Runnable callback) {
        loadGameContent.setOnBrowse(callback);
    }

    /**
     * Registers the callback invoked when the user drops a file onto the
     * stock file drop zone.
     *
     * @param callback the action to run with the dropped file; {@code null} disables the handler
     */
    public void setOnStockFileDrop(Consumer<File> callback) {
        newGameTabContent.setOnFileDrop(callback);
    }

    /**
     * Registers the callback invoked when the user drops a file onto the
     * save file drop zone.
     *
     * @param callback the action to run with the dropped file; {@code null} disables the handler
     */
    public void setOnSaveFileDrop(Consumer<File> callback) {
        loadGameContent.setOnFileDrop(callback);
    }
}
