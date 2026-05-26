package edu.ntnu.idatt2003.millions.view.dashboard.watchlist.dialog;

import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Confirmation dialog shown when the player clicks the remove button on a watchlist row.
 *
 * <p>Warns the player that any note written for the stock will be permanently lost.
 * The dialog blocks until dismissed ({@link #showStage()} uses {@code showAndWait})
 * and only calls {@code onConfirm} when the player explicitly confirms the removal.</p>
 */
public class WatchlistRemoveDialog extends Modal {

    private final Stock stock;
    private final Runnable onConfirm;

    /**
     * Constructs a new {@link WatchlistRemoveDialog}.
     *
     * @param stock     the stock the player is about to remove from the watchlist
     * @param onConfirm called when the player confirms the removal
     */
    public WatchlistRemoveDialog(Stock stock, Runnable onConfirm) {
        this.stock = stock;
        this.onConfirm = onConfirm;
    }

    @Override
    protected Region buildContent() {
        VBox content = new VBox();
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("watchlist.remove.heading")),
                buildBody()
        );
        return content;
    }

    private VBox buildBody() {
        StyledText sub = StyledText.paragraphOne(
                LanguageManager.get("watchlist.remove.sub"));

        Button removeBtn = new Button(LanguageManager.get("watchlist.remove.confirm"));
        removeBtn.getStyleClass().addAll("modal-button", "modal-button-danger");
        removeBtn.setOnAction(e -> {
            close();
            onConfirm.run();
        });

        Button cancelBtn = new Button(LanguageManager.get("watchlist.remove.cancel"));
        cancelBtn.getStyleClass().addAll("modal-button", "modal-button-outlined");
        cancelBtn.setOnAction(e -> close());

        VBox body = new VBox(12, sub, ModalActions.row(cancelBtn,removeBtn));
        body.getStyleClass().add("modal-body");
        return body;
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }
}
