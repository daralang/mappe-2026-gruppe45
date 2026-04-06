package edu.ntnu.idatt2003.millions.view.component;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * A reusable view header component containing a title, a WeekBar,
 * and a tab bar for navigation.
 */
public class ViewHeader extends VBox {

    private Button activeButton;

    /**
     * Constructs a ViewHeader with the given title, tab labels and WeekBar.
     * The first tab is active by default.
     *
     * @param title   the title of the view
     * @param labels  the tab labels in order
     * @param weekBar the shared week bar component
     */
    public ViewHeader(String title, List<String> labels, WeekBar weekBar) {
        setSpacing(8);

        // Tittelrad
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("view-header-title");

        HBox titleRow = new HBox(12, titleLabel, weekBar);
        HBox.setHgrow(weekBar, javafx.scene.layout.Priority.ALWAYS);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        // Tabbar
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
     * Returns the currently active tab button.
     *
     * @return the active button
     */
    public Button getActiveButton() {
        return activeButton;
    }
}