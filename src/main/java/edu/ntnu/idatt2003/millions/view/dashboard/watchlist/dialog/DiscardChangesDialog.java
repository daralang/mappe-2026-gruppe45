package edu.ntnu.idatt2003.millions.view.dashboard.watchlist.dialog;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation dialog shown when the player tries to close
 * {@link WatchlistNoteDialog} with unsaved changes.
 */
class DiscardChangesDialog extends Modal {

    private final Runnable onContinue;
    private final Runnable onDiscard;

    /**
     * Constructs a new {@link DiscardChangesDialog}.
     *
     * @param onContinue called when the player chooses to continue editing
     * @param onDiscard  called when the player confirms discarding changes
     */
    DiscardChangesDialog(Runnable onContinue, Runnable onDiscard) {
        this.onContinue = onContinue;
        this.onDiscard = onDiscard;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getStyleClass().add("modal-discard-changes");
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("watchlist.note.discard.title")),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        StyledText heading = StyledText.sectionTitle(
                LanguageManager.get("watchlist.note.discard.heading"));
        StyledText sub = StyledText.detailLabel(
                LanguageManager.get("watchlist.note.discard.sub"));

        Button continueBtn = new Button(LanguageManager.get("watchlist.note.discard.continue"));
        continueBtn.getStyleClass().addAll("modal-button", "modal-button-primary");
        continueBtn.setOnAction(e -> {
            close();
            onContinue.run();
        });

        Button discardBtn = new Button(LanguageManager.get("watchlist.note.discard.confirm"));
        discardBtn.getStyleClass().addAll("modal-button", "modal-button-outlined");
        discardBtn.setOnAction(e -> {
            close();
            onDiscard.run();
        });

        VBox body = new VBox(12, heading, sub, ModalActions.row(discardBtn, continueBtn));
        body.getStyleClass().add("modal-body");
        return body;
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }
}
