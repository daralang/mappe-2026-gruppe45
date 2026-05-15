package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation dialog shown when the user tries to exit the game.
 *
 * <p>Three options are offered stacked vertically:
 * <ol>
 *   <li>"Save and exit" — saves to the current file (or opens a dialog if none
 *       exists) then closes the application. Default-focused (primary style).</li>
 *   <li>"Sell all and exit" — liquidates the portfolio at market price, records
 *       a RETIRED leaderboard entry, and closes the application.</li>
 *   <li>"Exit without saving" — closes the application immediately.
 *       Rendered as a text link to signal the destructive nature.</li>
 * </ol>
 *
 * <p>Up/down arrow keys cycle the selection. Enter fires the focused button.
 * ESC and the header × button cancel the exit.</p>
 */
public class ExitDialog extends Modal {

    private final Runnable onSaveAndExit;
    private final Runnable onSellAllAndExit;
    private final Runnable onExitWithoutSaving;

    private Button saveAndExitBtn;
    private Button sellAllAndExitBtn;
    private Button exitWithoutSavingBtn;
    private Button[] buttons;
    private int selectedIndex = 0;

    /**
     * @param onSaveAndExit       run when the user confirms save-and-exit
     * @param onSellAllAndExit    run when the user confirms sell-all-and-exit
     * @param onExitWithoutSaving run when the user confirms exit without saving
     */
    public ExitDialog(Runnable onSaveAndExit, Runnable onSellAllAndExit,
                      Runnable onExitWithoutSaving) {
        this.onSaveAndExit = onSaveAndExit;
        this.onSellAllAndExit = onSellAllAndExit;
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
        saveAndExitBtn.setMaxWidth(Double.MAX_VALUE);
        saveAndExitBtn.setOnAction(e -> { close(); onSaveAndExit.run(); });

        sellAllAndExitBtn = new Button(LanguageManager.get("exit.sellAllAndExit"));
        sellAllAndExitBtn.getStyleClass().add("modal-button");
        sellAllAndExitBtn.setMaxWidth(Double.MAX_VALUE);
        sellAllAndExitBtn.setOnAction(e -> { close(); onSellAllAndExit.run(); });

        exitWithoutSavingBtn = new Button(LanguageManager.get("exit.exitWithoutSaving"));
        exitWithoutSavingBtn.getStyleClass().addAll("modal-button", "modal-button-link");
        exitWithoutSavingBtn.setMaxWidth(Double.MAX_VALUE);
        exitWithoutSavingBtn.setOnAction(e -> { close(); onExitWithoutSaving.run(); });

        buttons = new Button[]{saveAndExitBtn, sellAllAndExitBtn, exitWithoutSavingBtn};
        selectIndex(0);

        VBox actionStack = new VBox(8, saveAndExitBtn, sellAllAndExitBtn, exitWithoutSavingBtn);
        VBox.setMargin(exitWithoutSavingBtn, new Insets(8, 0, 0, 0));

        VBox body = new VBox(12, heading, sub, actionStack);
        body.getStyleClass().add("modal-body");
        return body;
    }

    /**
     * Moves the selection indicator to the button at {@code index}. The first
     * two buttons use primary/outlined styles; the link button (index 2) uses
     * modal-button-link-active when selected, reverting to muted otherwise.
     */
    private void selectIndex(int index) {
        selectedIndex = index;
        saveAndExitBtn.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
        saveAndExitBtn.getStyleClass().add(selectedIndex == 0 ? "modal-button-primary" : "modal-button-outlined");

        sellAllAndExitBtn.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
        sellAllAndExitBtn.getStyleClass().add(selectedIndex == 1 ? "modal-button-primary" : "modal-button-outlined");

        exitWithoutSavingBtn.getStyleClass().removeAll("modal-button-link-active");
        if (selectedIndex == 2) exitWithoutSavingBtn.getStyleClass().add("modal-button-link-active");
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
                case UP   -> selectIndex((selectedIndex - 1 + buttons.length) % buttons.length);
                case DOWN -> selectIndex((selectedIndex + 1) % buttons.length);
                case ENTER -> {
                    buttons[selectedIndex].fire();
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
