package edu.ntnu.idatt2003.millions.view.component;

import edu.ntnu.idatt2003.millions.util.Language;
import edu.ntnu.idatt2003.millions.util.LanguageManager;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * A reusable language picker that renders one flag button per supported language.
 * The active language is shown at full opacity; others are dimmed.
 */
public class LanguagePicker extends HBox {

    public LanguagePicker() {
        getStyleClass().add("language-picker");

        Language[] languages = Language.values();
        for (Language lang : languages) {
            ImageView flag = new ImageView(lang.flag);
            flag.setFitWidth(22);
            flag.setFitHeight(15);
            flag.setPreserveRatio(false);

            Button btn = new Button();
            btn.setGraphic(flag);
            btn.getStyleClass().add("language-picker-btn");
            btn.setOnAction(e -> {
                LanguageManager.setLanguage(lang);
                Platform.runLater(() -> {
                    if (getScene() != null) {
                        getScene().getRoot().requestFocus();
                    }
                });
            });

            getChildren().add(btn);
        }

        updateSelection();
        LanguageManager.addObserver(this::updateSelection);
    }

    private void updateSelection() {
        Language current = LanguageManager.getCurrentLanguage();
        Language[] languages = Language.values();
        for (int i = 0; i < languages.length; i++) {
            Button btn = (Button) getChildren().get(i);
            if (languages[i] == current) {
                btn.getStyleClass().add("language-picker-btn-active");
            } else {
                btn.getStyleClass().remove("language-picker-btn-active");
            }
        }
    }
}
