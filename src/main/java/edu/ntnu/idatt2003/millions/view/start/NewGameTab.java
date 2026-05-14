package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.CurrencySelector;
import edu.ntnu.idatt2003.millions.view.component.StyledText;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Currency;

/**
 * Tab panel for the "new game" flow on the start screen.
 *
 * <p>Extends {@link FileDropTab} with fields for player name, starting capital,
 * and currency selection. The stock CSV drop zone and start button are inherited
 * from the base class. The currency selector is disabled when no file is selected
 * and re-enabled via {@link #onFileSelected()} and {@link #onFileCleared()}.</p>
 */
public class NewGameTab extends FileDropTab {

    private static final double LABEL_WIDTH = 120;

    private final StyledText nameLabel;
    private final StyledText capitalLabel;
    private final StyledText fileLabel;
    private final StyledText currencyLabel;
    private final TextField nameField;
    private final TextField capitalField;
    private final CurrencySelector currencySelector;

    /**
     * Creates the new game tab, builds its layout, and registers
     * an i18n observer so labels update on language changes.
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

        getChildren().addAll(nameRow, capitalRow, fileLabel, getFileDropZone(), currencyRow, getActionButton());

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
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
