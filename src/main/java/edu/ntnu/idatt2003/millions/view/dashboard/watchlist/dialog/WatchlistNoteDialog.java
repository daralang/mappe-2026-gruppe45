package edu.ntnu.idatt2003.millions.view.dashboard.watchlist.dialog;

import edu.ntnu.idatt2003.millions.model.currency.CurrencyConverter;
import edu.ntnu.idatt2003.millions.model.stock.Stock;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.Modal;
import edu.ntnu.idatt2003.millions.view.component.ModalActions;
import edu.ntnu.idatt2003.millions.view.dashboard.watchlist.card.StockInfoCard;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Modal dialog for adding or editing a free-text note on a watchlist entry.
 *
 * <p>Displays a {@link StockInfoCard} above the note field, showing the stock's
 * current price in its native currency and in NOK, weekly change percentage,
 * and a sparkline trend chart.</p>
 */
public class WatchlistNoteDialog extends Modal {

    private static final double CARD_WIDTH = 640;
    private static final double NOTE_AREA_HEIGHT = 220;

    private final Stock stock;
    private final CurrencyConverter converter;
    private final String existingNote;
    private Consumer<String> onSave;
    private boolean hasChanges = false;
    private TextArea noteArea;

    /**
     * Constructs a new {@link WatchlistNoteDialog} for the given stock.
     *
     * @param stock        the stock whose price data is shown above the note field
     * @param converter    the currency converter used to derive the NOK price
     * @param existingNote the current note text to pre-fill; {@code null} is treated as empty
     */
    public WatchlistNoteDialog(Stock stock, CurrencyConverter converter, String existingNote) {
        this.stock = stock;
        this.converter = converter;
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

    /**
     * Widens the modal card to accommodate the {@link StockInfoCard} meta row.
     * Uses inline style to override the {@code modal-card} CSS max-width constraint.
     *
     * @param card the modal card node
     */
    @Override
    protected void configureCard(VBox card) {
        card.setStyle("-fx-min-width: " + CARD_WIDTH + "; -fx-max-width: " + CARD_WIDTH + ";");
    }

    /**
     * Forces a layout and resize pass after the stage is shown so the wider
     * card dimensions are correctly reflected in the stage size.
     */
    @Override
    protected void onBeforeShow() {
        sizeToContent();
    }

    @Override
    protected javafx.scene.Node firstFocusTarget() {
        return noteArea;
    }

    @Override
    protected Region buildContent() {
        StockInfoCard infoCard = new StockInfoCard(stock, converter);
        noteArea = new TextArea(existingNote);
        noteArea.setPromptText(LanguageManager.get("watchlist.note.placeholder"));
        noteArea.setWrapText(true);
        noteArea.setMaxWidth(Double.MAX_VALUE);
        noteArea.setPrefHeight(NOTE_AREA_HEIGHT);
        noteArea.getStyleClass().add("modal-input");
        noteArea.getStyleClass().addAll("modal-input", "content-scroll");

        Button cancel = new Button(LanguageManager.get("dialog.button.cancel"));
        cancel.getStyleClass().addAll("modal-button", "modal-button-outlined");
        cancel.setOnAction(e -> close());

        Button save = new Button(LanguageManager.get("watchlist.note.save"));
        save.getStyleClass().addAll("modal-button", "modal-button-primary");
        save.setDisable(true);
        noteArea.textProperty().addListener((obs, oldText, newText) -> {
            hasChanges = !newText.equals(existingNote);
            save.setDisable(!hasChanges);
        });
        save.setOnAction(e -> {
            if (onSave != null) {
                onSave.accept(noteArea.getText());
            }
            forceClose();
        });

        VBox content = new VBox();
        content.getStyleClass().add("modal-watchlist-note");
        content.getChildren().addAll(
                buildStandardHeader(LanguageManager.get("watchlist.note.title")),
                buildBody(infoCard, noteArea, cancel, save)
        );
        return content;
    }

    @Override
    protected void showStage() {
        stage.showAndWait();
    }

    /**
     * Intercepts close requests. If there are unsaved changes, shows a
     * {@link DiscardChangesDialog} first. If there are no changes, closes immediately.
     */
    @Override
    public void close() {
        if (hasChanges) {
            new DiscardChangesDialog(
                    () -> {},
                    this::forceClose
            ).show();
        } else {
            forceClose();
        }
    }

    /**
     * Closes the dialog unconditionally, bypassing the unsaved-changes check.
     */
    private void forceClose() {
        stage.close();
    }

    private VBox buildBody(StockInfoCard infoCard, TextArea noteArea,
                           Button cancel, Button save) {
        VBox body = new VBox(12, infoCard, noteArea, ModalActions.row(cancel, save));
        body.getStyleClass().add("modal-body");
        return body;
    }
}
