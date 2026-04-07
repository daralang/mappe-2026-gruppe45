package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A reusable view header component containing a title, a {@link WeekBar},
 * and a tab bar for navigation.
 * Automatically updates all text when the language changes.
 */
public class ViewHeader extends VBox {

    private Button activeButton;
    private final String titleKey;
    private final List<String> labelKeys;
    private final Label titleLabel;
    private final HBox tabBar;

    /**
     * Constructs a ViewHeader with the given title key, tab label keys and WeekBar.
     * The first tab is active by default.
     *
     * @param titleKey  the i18n key for the view title
     * @param labelKeys the i18n keys for the tab labels in order
     * @param weekBar   the shared week bar component
     */
    public ViewHeader(String titleKey, List<String> labelKeys, WeekBar weekBar) {
        this.titleKey = titleKey;
        this.labelKeys = labelKeys;

        setSpacing(8);

        // Tittelrad
        titleLabel = new Label(LanguageManager.get(titleKey));
        titleLabel.getStyleClass().add("view-header-title");

        HBox titleRow = new HBox(12, titleLabel, weekBar);
        HBox.setHgrow(weekBar, Priority.ALWAYS);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Tabbar
        tabBar = new HBox(54);
        for (String key : labelKeys) {
            Button button = new Button(LanguageManager.get(key));
            button.getStyleClass().add("tab-button");
            button.setOnAction(e -> setActive(button));
            tabBar.getChildren().add(button);
        }

        tabBar.getStyleClass().add("tab-bar");

        if (!tabBar.getChildren().isEmpty()) {
            setActive((Button) tabBar.getChildren().getFirst());
        }

        getChildren().addAll(titleRow, tabBar);

        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Sets the given button as the active tab.
     *
     * @param button the button to activate
     */
    public void setActive(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("tab-button-active");
        }
        activeButton = button;
        activeButton.getStyleClass().add("tab-button-active");
    }

    /**
     * Returns the currently active tab button.
     *
     * @return the active button
     */
    public Button getActiveButton() {
        return activeButton;
    }

    /**
     * Updates all text elements to the current language.
     * Called automatically when the language changes.
     */
    private void updateTexts() {
        titleLabel.setText(LanguageManager.get(titleKey));
        List<Node> buttons = tabBar.getChildren();
        for (int i = 0; i < buttons.size(); i++) {
            ((Button) buttons.get(i)).setText(LanguageManager.get(labelKeys.get(i)));
        }
    }
}