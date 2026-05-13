package edu.ntnu.idatt2003.millions.view.component;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Utility for building a full-width row of action buttons, matching the
 * modal-actions layout used in transaction dialogs and detail modals.
 */
public final class ModalActions {

    private ModalActions() {}

    /**
     * Returns an HBox with the modal-actions style class. Each button is
     * set to grow and fill the available width equally.
     *
     * @param buttons the buttons to place in the row
     * @return the styled action row
     */
    public static HBox row(Button... buttons) {
        HBox hbox = new HBox(buttons);
        hbox.getStyleClass().add("modal-actions");
        for (Button b : buttons) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.setPrefWidth(0);
            HBox.setHgrow(b, Priority.ALWAYS);
        }
        return hbox;
    }
}
