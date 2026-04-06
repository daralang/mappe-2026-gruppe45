package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A reusable view header component containing a title, week indicator,
 * advance week button, and a tab bar for navigation.
 */
public class ViewHeader extends VBox {

    private Button activeButton;
    private final Label weekLabel;

    /**
     * Constructs a ViewHeader with the given title and tab labels.
     * The first tab is active by default.
     *
     * @param title  the title of the view
     * @param labels the tab labels in order
     */
    public ViewHeader(String title, List<String> labels) {
        setSpacing(8);

        // Title + week + advance button
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("view-header-title");

        weekLabel = new Label(LanguageManager.get("app.week") + " 1");
        weekLabel.getStyleClass().add("week-label");

        Button advanceButton = new Button(LanguageManager.get("app.advanceWeek"));
        advanceButton.getStyleClass().add("advance-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox titleRow = new HBox(12, titleLabel, spacer, weekLabel, advanceButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Tab bar
        HBox tabBar = new HBox(54);
        for (String label : labels) {
            Button button = new Button(label);
            button.getStyleClass().add("tab-button");
            button.setOnAction(e -> setActive(button));
            tabBar.getChildren().add(button);
        }

        tabBar.getStyleClass().add("tab-bar");

        if (!tabBar.getChildren().isEmpty()) {
            setActive((Button) tabBar.getChildren().getFirst());
        }

        getChildren().addAll(titleRow, tabBar);
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
     * Updates the week number displayed in the header.
     *
     * @param week the current week number
     */
    public void setWeek(int week) {
        weekLabel.setText(LanguageManager.get("app.week") + " " + week);
    }

    /**
     * Returns the currently active tab button.
     *
     * @return the active button
     */
    public Button getActiveButton() {
        return activeButton;
    }
}