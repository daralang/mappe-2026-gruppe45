package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * A reusable component displaying the current week and an advance week button.
 */
public class WeekBar extends HBox {

    private final Label weekLabel;
    private final Button advanceButton;

    public WeekBar() {
        setSpacing(24);
        setAlignment(Pos.CENTER_RIGHT);

        weekLabel = new Label(LanguageManager.get("app.week") + " 1");
        weekLabel.getStyleClass().add("week-label");

        advanceButton = new Button(LanguageManager.get("app.advanceWeek"));
        advanceButton.getStyleClass().add("advance-button");

        getChildren().addAll(weekLabel, advanceButton);
    }

    public void setWeek(int week) {
        weekLabel.setText(LanguageManager.get("app.week") + " " + week);
    }

    public Button getAdvanceButton() {
        return advanceButton;
    }
}
