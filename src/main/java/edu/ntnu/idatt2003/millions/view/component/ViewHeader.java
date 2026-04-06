package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.util.List;

/**
 * A reusable tab bar component that highlights the active tab.
 */
public class ViewHeader extends HBox {

    private Button activeButton;

    /**
     * Constructs a TabBar with the given tab labels.
     * The first tab is active by default.
     *
     * @param labels the tab labels in order
     */
    public ViewHeader(List<String> labels) {
        setSpacing(24);

        for (String label : labels) {
            Button button = new Button(label);
            button.getStyleClass().add("tab-button");
            button.setOnAction(e -> setActive(button));
            getChildren().add(button);
        }

        if (!getChildren().isEmpty()) {
            setActive((Button) getChildren().getFirst());
        }
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
     * Returns the currently active button.
     *
     * @return the active button
     */
    public Button getActiveButton() {
        return activeButton;
    }
}