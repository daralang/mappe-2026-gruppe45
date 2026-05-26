package edu.ntnu.idatt2003.millions.view.ingame.game;

import edu.ntnu.idatt2003.millions.util.language.LanguageManager;
import edu.ntnu.idatt2003.millions.view.ingame.component.modal.Modal;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.BooleanSupplier;

/**
 * Parameterised end-game confirmation dialog used for both the exit flow and
 * the "start new game" flow.
 *
 * <p>Three options are offered stacked vertically:
 * <ol>
 *   <li>Save and continue — calls {@code saveAction}. On success closes the
 *       dialog and calls {@code onContinue}; on failure keeps the dialog open
 *       (the save helper is responsible for surfacing the error as a toast).</li>
 *   <li>Sell all and continue — calls {@code sellAllAction} (which handles its
 *       own leaderboard recording), then closes the dialog and calls
 *       {@code onContinue}.</li>
 *   <li>Continue without saving — calls {@code noSaveAction} (e.g. record
 *       leaderboard as active), then closes the dialog and calls
 *       {@code onContinue}.</li>
 * </ol>
 *
 * <p>Up/down arrow keys cycle the selection. Enter fires the focused button.
 * ESC and the header × button cancel without any action.</p>
 *
 * @param titleKey                   i18n key for the modal header title
 * @param contentKey                 i18n key for the body heading
 * @param saveAndContinueLabelKey    i18n key for the first (primary) button
 * @param sellAllAndContinueLabelKey i18n key for the second button
 * @param continueWithoutSavingKey   i18n key for the link-style third button
 * @param saveAction                 called when the player chooses save; returns
 *                                   {@code true} on success, {@code false} on failure
 * @param sellAllAction              game-level liquidation action; must not include
 *                                   navigation; responsible for its own leaderboard update
 * @param noSaveAction               pre-navigation action for the no-save path
 *                                   (e.g. {@code gameService::recordLeaderboardEntry})
 * @param onContinue                 pure navigation executed after any successful action
 *                                   (close the window, return to start screen, etc.)
 */
public class EndGameDialog extends Modal {

    private final String titleKey;
    private final String contentKey;
    private final String subtitleKey;
    private final String saveAndContinueLabelKey;
    private final String sellAllAndContinueLabelKey;
    private final String continueWithoutSavingKey;
    private final BooleanSupplier saveAction;
    private final Runnable sellAllAction;
    private final Runnable noSaveAction;
    private final Runnable onContinue;

    private Button saveBtn;
    private Button sellAllBtn;
    private Button noSaveBtn;
    private Button[] buttons;
    private int selectedIndex = 0;

    public EndGameDialog(
            String titleKey,
            String contentKey,
            String subtitleKey,
            String saveAndContinueLabelKey,
            String sellAllAndContinueLabelKey,
            String continueWithoutSavingKey,
            BooleanSupplier saveAction,
            Runnable sellAllAction,
            Runnable noSaveAction,
            Runnable onContinue) {
        this.titleKey = titleKey;
        this.contentKey = contentKey;
        this.subtitleKey = subtitleKey;
        this.saveAndContinueLabelKey = saveAndContinueLabelKey;
        this.sellAllAndContinueLabelKey = sellAllAndContinueLabelKey;
        this.continueWithoutSavingKey = continueWithoutSavingKey;
        this.saveAction = saveAction;
        this.sellAllAction = sellAllAction;
        this.noSaveAction = noSaveAction;
        this.onContinue = onContinue;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get(titleKey)),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        StyledText heading = StyledText.sectionTitle(LanguageManager.get(contentKey));
        StyledText sub = StyledText.detailLabel(LanguageManager.get(subtitleKey));

        saveBtn = new Button(LanguageManager.get(saveAndContinueLabelKey));
        saveBtn.getStyleClass().add("modal-button");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            if (saveAction.getAsBoolean()) {
                close();
                onContinue.run();
            }
        });

        sellAllBtn = new Button(LanguageManager.get(sellAllAndContinueLabelKey));
        sellAllBtn.getStyleClass().add("modal-button");
        sellAllBtn.setMaxWidth(Double.MAX_VALUE);
        sellAllBtn.setOnAction(e -> {
            close();
            sellAllAction.run();
            onContinue.run();
        });

        noSaveBtn = new Button(LanguageManager.get(continueWithoutSavingKey));
        noSaveBtn.getStyleClass().addAll("modal-button", "modal-button-link");
        noSaveBtn.setMaxWidth(Double.MAX_VALUE);
        noSaveBtn.setOnAction(e -> {
            close();
            noSaveAction.run();
            onContinue.run();
        });

        buttons = new Button[]{saveBtn, sellAllBtn, noSaveBtn};
        selectIndex(0);

        VBox actionStack = new VBox(8, saveBtn, sellAllBtn, noSaveBtn);
        VBox.setMargin(noSaveBtn, new Insets(8, 0, 0, 0));

        VBox body = new VBox(12, heading, sub, actionStack);
        body.getStyleClass().add("modal-body");
        return body;
    }

    private void selectIndex(int index) {
        selectedIndex = index;
        saveBtn.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
        saveBtn.getStyleClass().add(selectedIndex == 0 ? "modal-button-primary" : "modal-button-outlined");

        sellAllBtn.getStyleClass().removeAll("modal-button-primary", "modal-button-outlined");
        sellAllBtn.getStyleClass().add(selectedIndex == 1 ? "modal-button-primary" : "modal-button-outlined");

        noSaveBtn.getStyleClass().removeAll("modal-button-link-active");
        if (selectedIndex == 2) noSaveBtn.getStyleClass().add("modal-button-link-active");
    }

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
