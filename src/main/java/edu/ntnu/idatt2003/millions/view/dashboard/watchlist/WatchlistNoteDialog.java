package edu.ntnu.idatt2003.millions.view.dashboard.watchlist;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Modal dialog for adding or editing a free-text note on a watchlist entry.
 *
 * <p>Pre-fills the {@link TextArea} with the existing note (may be empty).
 * Calls the supplied {@link Consumer} with the new text when the player saves,
 * or closes without invoking it when the player cancels.</p>
 *
 * <p>Business logic lives entirely in the callback supplied by the controller.
 * The dialog never touches {@link GameService} directly.</p>
 */
public class WatchlistNoteDialog extends Modal {

    private static final double NOTE_AREA_WIDTH = 380;
    private static final double NOTE_AREA_HEIGHT = 120;

    private final String symbol;
    private final String existingNote;
    private Consumer<String> onSave;

    /**
     * @param symbol       the ticker symbol shown in the dialog header
     * @param existingNote the current note text to pre-fill; may be empty
     */
    public WatchlistNoteDialog(String symbol, String existingNote) {
        this.symbol = symbol;
        this.existingNote = existingNote == null ? "" : existingNote;
    }

    /**
     * Sets the callback invoked with the new note text when the player saves.
     *
     * @param callback receives the updated note string
     */
    public void setOnSave(Consumer<String> callback) {
        this.onSave = callback;
    }

    @Override
    protected Region buildContent() {
        String title = MessageFormat.format(
                LanguageManager.get("watchlist.note.title"), symbol);

        TextArea noteArea = new TextArea(existingNote);
        noteArea.setPromptText(LanguageManager.get("watchlist.note.placeholder"));
        noteArea.setWrapText(true);
        noteArea.setPrefWidth(NOTE_AREA_WIDTH);
        noteArea.setPrefHeight(NOTE_AREA_HEIGHT);
        noteArea.getStyleClass().add("modal-input");

        Button cancel = new Button(LanguageManager.get("dialog.button.cancel"));
        cancel.getStyleClass().add("modal-button");
        cancel.setOnAction(e -> close());

        Button save = new Button(LanguageManager.get("watchlist.note.save"));
        save.getStyleClass().addAll("modal-button", "modal-button-primary");
        save.setOnAction(e -> {
            if (onSave != null) {
                onSave.accept(noteArea.getText());
            }
            close();
        });

        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(title),
                buildBody(noteArea, cancel, save)
        );
        return content;
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }

    private VBox buildBody(TextArea noteArea, Button cancel, Button save) {
        VBox body = new VBox(12, noteArea, ModalActions.row(cancel, save));
        body.getStyleClass().add("modal-body");
        return body;
    }
}
