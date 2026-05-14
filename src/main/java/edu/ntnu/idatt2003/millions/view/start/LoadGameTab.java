package edu.ntnu.idatt2003.millions.view.start;

import edu.ntnu.idatt2003.millions.util.LanguageManager;
import edu.ntnu.idatt2003.millions.view.component.StyledText;

/**
 * Tab panel for the "resume game" flow on the start screen.
 *
 * <p>Extends {@link FileDropTab} with a label describing the expected save file.
 * The save file drop zone and load button are inherited from the base class.</p>
 */
public class LoadGameTab extends FileDropTab {

    private final StyledText saveFileLabel;

    /**
     * Creates the load game tab, builds its layout, and registers
     * an i18n observer so labels update on language changes.
     */
    public LoadGameTab() {
        saveFileLabel = StyledText.paragraphOne();
        getChildren().addAll(saveFileLabel, getFileDropZone(), getActionButton());

        updateTexts();
        LanguageManager.addObserver(this::updateTexts);
    }

    /**
     * Refreshes all visible texts from the current {@link LanguageManager} bundle.
     */
    private void updateTexts() {
        saveFileLabel.setText(LanguageManager.get("start.resume.fileLabel"));
        getFileDropZone().setHintText(LanguageManager.get("start.resume.dropZoneHint"));
        getFileDropZone().setOrText(LanguageManager.get("start.resume.dropZoneOr"));
        getFileDropZone().setBrowseText(LanguageManager.get("start.file.browse"));
        setActionButtonText(LanguageManager.get("start.loadButton"));
    }
}
