package edu.ntnu.idatt2003.millions.view.ingame.component.shell;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable view header with a title, a tab bar, and a {@code WeekBar}.
 *
 * <p>Tab switching is handled centrally via {@code Shift+1–4} shortcuts and by
 * mouse click; this component owns only the tab buttons and their active-state
 * styling. Language changes update all labels automatically.</p>
 */
public class ViewHeader extends VBox {

    private Button activeButton;
    private final String titleKey;
    private final List<String> labelKeys;
    private final List<Button> tabButtons = new ArrayList<>();
    private final StyledText titleLabel;
    private final HBox tabBar;

    /**
     * Constructs a ViewHeader with the given title key, tab label keys and WeekBar.
     * The first tab is active by default.
     *
     * @param titleKey  the i18n key for the view title
     * @param labelKeys the i18n keys for the tab labels in order
     * @param weekBar   the week bar owned exclusively by the enclosing view; each top-level view
     *                  receives its own instance since a JavaFX node can only belong to one parent at a time
     */
    public ViewHeader(String titleKey, List<String> labelKeys, WeekBar weekBar) {
        this.titleKey = titleKey;
        this.labelKeys = labelKeys;

        setSpacing(8);

        // Tittelrad
        titleLabel = StyledText.pageTitle(LanguageManager.get(titleKey));

        HBox titleRow = new HBox(12, titleLabel, weekBar);
        HBox.setHgrow(weekBar, Priority.ALWAYS);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Tabbar
        tabBar = new HBox(54);
        for (String key : labelKeys) {
            Button button = new Button(LanguageManager.get(key));
            button.getStyleClass().add("tab-button");
            button.setFocusTraversable(true);
            button.setOnAction(e -> setActive(button));
            tabBar.getChildren().add(button);
            tabButtons.add(button);
        }

        tabBar.getStyleClass().add("tab-bar");

        if (!tabButtons.isEmpty()) {
            setActive(tabButtons.getFirst());
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
     * Requests keyboard focus on the currently active tab button, if present.
     */
    public void focusActiveTab() {
        if (activeButton != null) {
            activeButton.requestFocus();
        }
    }

    /**
     * Updates all text elements to the current language.
     * Called automatically when the language changes.
     */
    private void updateTexts() {
        titleLabel.setText(LanguageManager.get(titleKey));
        for (int i = 0; i < tabButtons.size(); i++) {
            tabButtons.get(i).setText(LanguageManager.get(labelKeys.get(i)));
        }
    }

    /**
     * Returns the tab button at the given index.
     *
     * @param index the zero-based index of the tab button
     * @return the tab button at the given index
     */
    public Button getTabButton(int index) {
        return tabButtons.get(index);
    }

    /**
     * Sets an action for the tab at the given index.
     * The action runs in addition to the built-in active-tab styling,
     * so callers do not need to invoke {@link #setActive(Button)} themselves.
     *
     * @param index  the zero-based index of the tab button
     * @param action the action to run when the tab is clicked
     */
    public void setTabAction(int index, Runnable action) {
        Button button = getTabButton(index);
        button.setOnAction(e -> { setActive(button); action.run(); });
    }
}
