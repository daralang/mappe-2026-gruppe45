package edu.ntnu.idatt2003.millions.view.dialog;

import edu.ntnu.idatt2003.millions.util.CurrencyFormatter;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.function.BooleanSupplier;

/**
 * Modal shown when the player cannot cover their obligations even through
 * full portfolio liquidation. ESC is blocked; the player must choose to
 * review the final game state or start a new game.
 */
public class GameOverModal extends Modal {

    private final int currentWeek;
    private final BigDecimal totalObligations;
    private final BigDecimal totalLiquidationValue;
    private final Stage ownerStage;
    private final Runnable onNewGame;
    private final BooleanSupplier saveAction;
    private final Runnable sellAllAction;
    private final Runnable noSaveAction;

    public GameOverModal(int currentWeek,
                         BigDecimal totalObligations,
                         BigDecimal totalLiquidationValue,
                         Stage ownerStage,
                         Runnable onNewGame,
                         BooleanSupplier saveAction,
                         Runnable sellAllAction,
                         Runnable noSaveAction) {
        this.currentWeek = currentWeek;
        this.totalObligations = totalObligations;
        this.totalLiquidationValue = totalLiquidationValue;
        this.ownerStage = ownerStage;
        this.onNewGame = onNewGame;
        this.saveAction = saveAction;
        this.sellAllAction = sellAllAction;
        this.noSaveAction = noSaveAction;
    }

    /** ESC is blocked — the player must use one of the two buttons. */
    @Override
    public void close() {}

    @Override
    protected void showStage() {
        stage.show();
        Platform.runLater(() -> {
            stage.setX(ownerStage.getX() + (ownerStage.getWidth()  - stage.getWidth())  / 2);
            stage.setY(ownerStage.getY() + (ownerStage.getHeight() - stage.getHeight()) / 2);
        });
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox(0);
        content.getChildren().addAll(buildCloseRow(), buildBody());
        return content;
    }

    private HBox buildCloseRow() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.setGraphic(new FontIcon("fth-x"));
        closeBtn.getStyleClass().add("modal-close");
        closeBtn.setOnAction(e -> stage.close());

        HBox row = new HBox(spacer, closeBtn);
        row.getStyleClass().add("modal-header");
        row.setStyle("-fx-border-width: 0; -fx-padding: 12 24 0 24;");
        return row;
    }

    private VBox buildBody() {
        Label titleLabel = new Label(LanguageManager.get("gameOver.title"));
        titleLabel.getStyleClass().add("game-over-title");

        Region above = new Region();
        above.setPrefHeight(20);
        Region below = new Region();
        below.setPrefHeight(20);

        Label reasonLabel = StyledText.detailValue(LanguageManager.get("gameOver.reason"));
        reasonLabel.setWrapText(true);
        reasonLabel.setMaxWidth(380);

        String contextText = MessageFormat.format(
                LanguageManager.get("gameOver.context"),
                currentWeek,
                CurrencyFormatter.format(totalObligations),
                CurrencyFormatter.format(totalLiquidationValue));
        Label contextLabel = StyledText.detailLabel(contextText);
        contextLabel.setWrapText(true);
        contextLabel.setMaxWidth(380);

        VBox paragraphs = new VBox(12, reasonLabel, contextLabel);
        paragraphs.setAlignment(Pos.CENTER);

        Button reviewBtn = new Button(LanguageManager.get("gameOver.review"));
        reviewBtn.getStyleClass().addAll("modal-button", "modal-button-outlined");
        reviewBtn.setOnAction(e -> stage.close());

        Button newGameBtn = new Button(LanguageManager.get("gameOver.newGame"));
        newGameBtn.getStyleClass().addAll("modal-button", "modal-button-primary");
        newGameBtn.setOnAction(e -> {
            stage.close();
            new EndGameDialog(
                    "nav.newGame",
                    "gameOver.dialogHeader",
                    "gameOver.dialogContent",
                    "newGame.saveAndStartNew",
                    "newGame.sellAllAndStartNew",
                    "newGame.startNewWithoutSaving",
                    saveAction,
                    sellAllAction,
                    noSaveAction,
                    onNewGame
            ).show();
        });

        HBox buttons = ModalActions.row(reviewBtn, newGameBtn);

        VBox body = new VBox(16, above, titleLabel, below, paragraphs, buttons);
        body.getStyleClass().add("modal-body");
        body.setStyle("-fx-padding: 0 24 20 24;");
        body.setAlignment(Pos.CENTER);
        return body;
    }
}
