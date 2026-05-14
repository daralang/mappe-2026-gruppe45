package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.CurrencySelector;
import edu.ntnu.idatt2003.millions.view.component.FileDropZone;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Currency;

/**
 * Tab panel for the "new game" flow on the start screen.
 *
 * <p>Extends {@link FileDropTab} with fields for player name, starting capital,
 * and currency selection. The optional stock CSV upload area opens automatically
 * from a collapsed state when the view is first shown, while the start button is
 * pushed downward by normal {@link VBox} layout. The tab content also animates
 * its own preferred height so the surrounding start card can follow the change.
 * The currency selector is disabled when no file is selected and re-enabled via
 * {@link #onFileSelected()} and {@link #onFileCleared()}.</p>
 */
public class NewGameTab extends FileDropTab {

    private static final double LABEL_WIDTH = 120;
    private static final double UPLOAD_DROP_ZONE_HEIGHT = 190;
    private static final double UPLOAD_SECTION_EXPANDED_HEIGHT = 250;
    private static final double CONTENT_WIDTH = CARD_WIDTH + 48;
    private static final Duration UPLOAD_REVEAL_DELAY = Duration.millis(380);
    private static final Duration UPLOAD_REVEAL_DURATION = Duration.millis(820);

    private final StyledText nameLabel;
    private final StyledText capitalLabel;
    private final StyledText fileLabel;
    private final StyledText currencyLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final CurrencySelector currencySelector;
    private final VBox uploadSection;

    /**
     * Creates the new game tab, builds its layout, and registers
     * an i18n observer so labels update on language changes.
     * The optional upload section starts collapsed and is revealed by
     * {@link #animateUploadIntro()} after the first layout pass.
     */
    public NewGameTab() {
        nameLabel = StyledText.paragraphOne();
        capitalLabel = StyledText.paragraphOne();
        currencyLabel = StyledText.paragraphOne();
        fileLabel = StyledText.paragraphOne();
        nameField = new TextField();
        capitalField = new TextField();
        currencySelector = new CurrencySelector();
        currencySelector.setDisable(false);

        HBox nameRow = buildFormRow(nameLabel, nameField);
        HBox capitalRow = buildFormRow(capitalLabel, capitalField);
        HBox currencyRow = buildFormRow(currencyLabel, currencySelector);
        getFileDropZone().setMinHeight(UPLOAD_DROP_ZONE_HEIGHT);
        getFileDropZone().setPrefHeight(UPLOAD_DROP_ZONE_HEIGHT);

        uploadSection = new VBox(16, getFileDropZone(), currencyRow);
        uploadSection.setMaxWidth(CARD_WIDTH);
        uploadSection.setMinHeight(0);
        uploadSection.setPrefHeight(0);
        uploadSection.setMaxHeight(0);
        uploadSection.setOpacity(0);

        Rectangle uploadClip = new Rectangle();
        uploadClip.widthProperty().bind(uploadSection.widthProperty());
        uploadClip.heightProperty().bind(uploadSection.heightProperty());
        uploadSection.setClip(uploadClip);

        VBox uploadArea = new VBox(10, fileLabel, uploadSection);
        uploadArea.setMaxWidth(CARD_WIDTH);

        getChildren().addAll(nameRow, capitalRow, uploadArea, getActionButton());

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
        Platform.runLater(this::animateUploadIntro);
    }

    /**
     * Reveals the optional upload controls with a slow downward animation.
     * The section animates to a fixed expanded height sized for the
     * {@link FileDropZone} and currency row.
     * The surrounding {@link NewGameTab} height is animated in
     * parallel so parent containers can resize the card during the reveal.
     */
    private void animateUploadIntro() {
        applyCss();
        double collapsedHeight = prefHeight(CONTENT_WIDTH);
        double expandedHeight = collapsedHeight + UPLOAD_SECTION_EXPANDED_HEIGHT;

        setMinHeight(collapsedHeight);
        setPrefHeight(collapsedHeight);
        setMaxHeight(expandedHeight);

        PauseTransition delay = new PauseTransition(UPLOAD_REVEAL_DELAY);
        delay.setOnFinished(event -> {
            Timeline reveal = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(uploadSection.prefHeightProperty(), 0),
                            new KeyValue(uploadSection.maxHeightProperty(), 0),
                            new KeyValue(uploadSection.opacityProperty(), 0),
                            new KeyValue(minHeightProperty(), collapsedHeight),
                            new KeyValue(prefHeightProperty(), collapsedHeight)),
                    new KeyFrame(UPLOAD_REVEAL_DURATION,
                            new KeyValue(uploadSection.prefHeightProperty(),
                                    UPLOAD_SECTION_EXPANDED_HEIGHT, Interpolator.EASE_BOTH),
                            new KeyValue(uploadSection.maxHeightProperty(),
                                    UPLOAD_SECTION_EXPANDED_HEIGHT, Interpolator.EASE_BOTH),
                            new KeyValue(uploadSection.opacityProperty(), 1, Interpolator.EASE_OUT),
                            new KeyValue(minHeightProperty(), expandedHeight, Interpolator.EASE_BOTH),
                            new KeyValue(prefHeightProperty(), expandedHeight, Interpolator.EASE_BOTH))
            );
            reveal.setOnFinished(finished -> {
                uploadSection.setPrefHeight(UPLOAD_SECTION_EXPANDED_HEIGHT);
                uploadSection.setMaxHeight(UPLOAD_SECTION_EXPANDED_HEIGHT);
                uploadSection.setOpacity(1);
                setMinHeight(expandedHeight);
                setPrefHeight(expandedHeight);
                setMaxHeight(expandedHeight);
            });
            reveal.play();
        });
        delay.play();
    }

    /**
     * Builds an inline label + field row where the label has a fixed width.
     *
     * @param label the label node
     * @param field the input node
     * @return an {@link HBox} with label and field on the same line
     */
    private HBox buildFormRow(javafx.scene.control.Label label, Node field) {
        label.setMinWidth(LABEL_WIDTH);
        HBox.setHgrow(field, Priority.ALWAYS);
        HBox row = new HBox(12, label, field);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(CARD_WIDTH);
        return row;
    }

    /**
     * Disables the currency selector when no stock file is selected.
     */
    @Override
    protected void onFileCleared() {
        currencySelector.setDisable(true);
    }

    /**
     * Enables the currency selector once a stock file is selected.
     */
    @Override
    protected void onFileSelected() {
        currencySelector.setDisable(false);
    }

    /**
     * Refreshes all visible texts from the current {@link LanguageManager} bundle.
     */
    private void updateTexts() {
        nameLabel.setText(LanguageManager.get("start.new.nameLabel"));
        capitalLabel.setText(LanguageManager.get("start.new.capitalLabel"));
        currencyLabel.setText(LanguageManager.get("start.new.currencyLabel"));
        fileLabel.setText(LanguageManager.get("start.new.fileLabel"));
        getFileDropZone().setHintText(LanguageManager.get("start.new.dropZoneHint"));
        getFileDropZone().setOrText(LanguageManager.get("start.new.dropZoneOr"));
        getFileDropZone().setBrowseText(LanguageManager.get("start.file.browse"));
        setActionButtonText(LanguageManager.get("start.startButton"));
        nameField.setPromptText("");
        capitalField.setPromptText("");
    }

    /**
     * Returns the trimmed player name entered by the user.
     *
     * @return trimmed player name
     */
    public String getName() {
        return nameField.getText().trim();
    }

    /**
     * Returns the trimmed starting capital entered by the user.
     *
     * @return trimmed starting capital
     */
    public String getCapital() {
        return capitalField.getText().trim();
    }

    /**
     * Returns the currency currently selected in the currency selector.
     *
     * @return the selected {@link Currency}, or {@code null} if none is selected
     */
    public Currency getSelectedCurrency() {
        return currencySelector.getValue();
    }
}
