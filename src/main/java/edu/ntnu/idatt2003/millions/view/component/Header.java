package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * The persistent navigation header displayed at the top of the main view.
 * Contains navigation links, save/exit game buttons, a language picker, and a notification bell.
 */
public class Header extends HBox {

    private final Button dashboardButton;
    private final Button exchangeButton;
    private final Button leaderboardButton;
    private final Button helpButton;
    private final Button bellButton;
    private final Button saveButton;
    private final Button exitButton;

    /**
     * Constructs a new Header with navigation and action buttons.
     */
    public Header() {
        getStyleClass().add("navbar");

        Label title = new Label(LanguageManager.get("app.title"));
        title.getStyleClass().add("navbar-title");

        dashboardButton = new Button(LanguageManager.get("nav.mySides"));
        exchangeButton = new Button(LanguageManager.get("nav.exchange"));
        leaderboardButton = new Button(LanguageManager.get("nav.leaderboard"));
        helpButton = new Button(LanguageManager.get("nav.help"));

        dashboardButton.getStyleClass().add("navbar-link");
        exchangeButton.getStyleClass().add("navbar-link");
        leaderboardButton.getStyleClass().add("navbar-link");
        helpButton.getStyleClass().add("navbar-link");

        HBox navLinks = new HBox(60, dashboardButton, exchangeButton, leaderboardButton, helpButton);
        navLinks.setAlignment(Pos.TOP_CENTER);

        saveButton = new Button(LanguageManager.get("nav.saveGame"));
        exitButton = new Button(LanguageManager.get("nav.exitGame"));
        saveButton.getStyleClass().add("navbar-action");
        exitButton.getStyleClass().add("navbar-action");

        HBox actions = new HBox(16, saveButton, exitButton);
        actions.setAlignment(Pos.CENTER_RIGHT);

        bellButton = new Button("🔔");
        bellButton.getStyleClass().add("navbar-icon");

        LanguagePicker languagePicker = new LanguagePicker();

        HBox bottomRight = new HBox(12, languagePicker, bellButton);
        bottomRight.setAlignment(Pos.CENTER_RIGHT);
        bottomRight.setPadding(new Insets(0, 0, 4, 0));

        VBox rightSide = new VBox();
        rightSide.setAlignment(Pos.TOP_RIGHT);
        rightSide.getChildren().addAll(actions, bottomRight);
        VBox.setVgrow(bottomRight, Priority.ALWAYS);

        Region leftSpacer = new Region();
        Region rightSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(title, leftSpacer, navLinks, rightSpacer, rightSide);
        setAlignment(Pos.TOP_LEFT);

        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Updates all text elements to the current language.
     * Called automatically when the language changes.
     */
    private void updateTexts() {
        dashboardButton.setText(LanguageManager.get("nav.mySides"));
        exchangeButton.setText(LanguageManager.get("nav.exchange"));
        leaderboardButton.setText(LanguageManager.get("nav.leaderboard"));
        helpButton.setText(LanguageManager.get("nav.help"));
        saveButton.setText(LanguageManager.get("nav.saveGame"));
        exitButton.setText(LanguageManager.get("nav.exitGame"));
    }

    /**
     * Returns the dashboard navigation button.
     *
     * @return the dashboard button
     */
    public Button getDashboardButton() {
        return dashboardButton;
    }

    /**
     * Returns the exchange navigation button.
     *
     * @return the exchange button
     */
    public Button getExchangeButton() {
        return exchangeButton;
    }

    /**
     * Returns the leaderboard navigation button.
     *
     * @return the leaderboard button
     */
    public Button getLeaderboardButton() {
        return leaderboardButton;
    }

    /**
     * Returns the help navigation button.
     *
     * @return the help button
     */
    public Button getHelpButton() {
        return helpButton;
    }

    /**
     * Returns the notification bell button.
     *
     * @return the bell button
     */
    public Button getBellButton() {
        return bellButton;
    }

    /**
     * Returns the save game button.
     *
     * @return the save button
     */
    public Button getSaveButton() {
        return saveButton;
    }

    /**
     * Returns the exit game button.
     *
     * @return the exit button
     */
    public Button getExitButton() {
        return exitButton;
    }
}