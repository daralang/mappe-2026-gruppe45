package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.Language;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

/**
 * A reusable language picker component that displays available languages
 * as a dropdown with flag image and display name.
 * Changing the selected language updates the active language in LanguageManager.
 */
public class LanguagePicker extends ComboBox<Language> {

    public LanguagePicker() {
        getItems().addAll(Language.values());
        setValue(LanguageManager.getCurrentLanguage());

        setCellFactory(list -> createCell());
        setButtonCell(createCell());

        setOnAction(e -> LanguageManager.setLanguage(getValue()));
    }

    private ListCell<Language> createCell() {
        return new ListCell<>() {

            private final ImageView flag = new ImageView();

            @Override
            protected void updateItem(Language language, boolean empty) {
                super.updateItem(language, empty);

                if (empty || language == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    flag.setImage(language.flag);
                    flag.setFitHeight(16);
                    flag.setFitWidth(24);

                    setText(language.displayName);
                    setGraphic(flag);
                }
            }
        };
    }
}