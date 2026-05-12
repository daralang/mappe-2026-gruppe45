package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation dialog shown when the user tries to exit the game.
 *
 * <p>The default selection is "Save and exit" (shown filled blue).
 * Left/right arrow keys move the selection; Enter fires it. ESC and
 * the header × button cancel the exit. The action callbacks are
 * supplied by the caller; this class owns only the visual structure.</p>
 */
public class ExitDialog extends Modal {

    private final Runnable onSaveAndExit;
    private final Runnable onExitWithoutSaving;

    private Button saveAndExitBtn;
    private Button exitWithoutSavingBtn;
    private Button selected;

    /**
     * @param onSaveAndExit       run when the user confirms save-and-exit
     * @param onExitWithoutSaving run when the user confirms exit without saving
     */
    public ExitDialog(Runnable onSaveAndExit, Runnable onExitWithoutSaving) {
        this.onSaveAndExit = onSaveAndExit;
        this.onExitWithoutSaving = onExitWithoutSaving;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("nav.exitGame")),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        StyledText heading = StyledText.sectionTitle(LanguageManager.get("exit.confirmHeader"));
        StyledText sub = StyledText.detailLabel(LanguageManager.get("exit.confirmContent"));

        saveAndExitBtn = new Button(LanguageManager.get("exit.saveAndExit"));
        saveAndExitBtn.getStyleClass().add("modal-button");
        saveAndExitBtn.setOnAction(e -> {
            close();
            onSaveAndExit.run();
        });

        exitWithoutSavingBtn = new Button(LanguageManager.get("exit.exitWithoutSaving"));
        exitWithoutSavingBtn.getStyleClass().add("modal-button");
        exitWithoutSavingBtn.setOnAction(e -> {
            close();
            onExitWithoutSaving.run();
        });

        select(saveAndExitBtn);

        VBox body = new VBox(12, heading, sub,
                ModalActions.row(saveAndExitBtn, exitWithoutSavingBtn));
        body.getStyleClass().add("modal-body");
        return body;
    }

    /**
     * Moves the blue selection indicator to {@code btn}. The newly
     * selected button gets the filled-primary style; the other gets
     * the outlined style.
     */
    private void select(Button btn) {
        selected = btn;
        for (Button b : new Button[]{saveAndExitBtn, exitWithoutSavingBtn}) {
            b.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
            b.getStyleClass().add(b == btn ? "modal-button-primary" : "modal-button-outlined");
        }
    }

    /**
     * Adds arrow/enter keyboard navigation. Uses addEventFilter so it
     * coexists with Modal's ESC handler (setOnKeyPressed) without
     * replacing it.
     */
    @Override
    protected void onBeforeShow() {
        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case LEFT  -> select(saveAndExitBtn);
                case RIGHT -> select(exitWithoutSavingBtn);
                case ENTER -> {
                    selected.fire();
                    e.consume();
                }
                default -> {}
            }
        });
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }
}
